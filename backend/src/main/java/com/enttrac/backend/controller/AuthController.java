package com.enttrac.backend.controller;

import com.enttrac.backend.auth.CurrentUserId;
import com.enttrac.backend.auth.GoogleTokenVerifierService;
import com.enttrac.backend.auth.JwtService;
import com.enttrac.backend.model.item.RefreshTokenItem;
import com.enttrac.backend.model.item.UserProfileItem;
import com.enttrac.backend.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private static final long REFRESH_TOKEN_TTL_SECONDS = 14L * 24 * 60 * 60;

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(GoogleTokenVerifierService googleTokenVerifierService,
                          JwtService jwtService,
                          UserRepository userRepository) {
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, String> body,
                                                           HttpServletResponse response) {
        Optional<GoogleIdToken.Payload> payload = googleTokenVerifierService.verify(body.get("idToken"));
        if (payload.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        String providerId = payload.get().getSubject();
        String userId = UserRepository.pkFor("google", providerId);

        UserProfileItem profile = userRepository.findProfile(userId).orElseGet(() -> {
            UserProfileItem p = new UserProfileItem();
            p.setPk(userId);
            p.setSk("PROFILE");
            p.setProvider("google");
            p.setProviderId(providerId);
            p.setEmail(payload.get().getEmail());
            p.setDisabled(false);
            p.setOnboarded(false);
            p.setCreatedAt(Instant.now().toString());
            return p;
        });

        if (profile.isDisabled()) {
            return ResponseEntity.status(403).build();
        }

        userRepository.saveProfile(profile);
        issueTokens(userId, response);

        return ResponseEntity.ok(Map.of(
                "email", profile.getEmail(),
                "displayName", profile.getDisplayName() == null ? "" : profile.getDisplayName(),
                "onboarded", profile.isOnboarded()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshCookie = extractCookie(request, "refreshToken");
        if (refreshCookie.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        String[] parts = refreshCookie.get().split("\\.", 2);
        if (parts.length != 2) {
            return ResponseEntity.status(401).build();
        }
        String userId = parts[0];
        String tokenId = parts[1];

        Optional<RefreshTokenItem> stored = userRepository.findRefreshToken(userId, tokenId);
        if (stored.isEmpty() || stored.get().getExpiresAt() < Instant.now().getEpochSecond()) {
            return ResponseEntity.status(401).build();
        }

        userRepository.deleteRefreshToken(userId, tokenId); // rotation: old token is now dead

        Optional<UserProfileItem> profile = userRepository.findProfile(userId);
        if (profile.isEmpty() || profile.get().isDisabled()) {
            return ResponseEntity.status(401).build();
        }

        issueTokens(userId, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        extractCookie(request, "refreshToken").ifPresent(value -> {
            String[] parts = value.split("\\.", 2);
            if (parts.length == 2) {
                userRepository.deleteRefreshToken(parts[0], parts[1]);
            }
        });

        clearCookie(response, "accessToken", "/");
        clearCookie(response, "refreshToken", "/api/auth/refresh");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@CurrentUserId String userId) {
        return userRepository.findProfile(userId)
                .<ResponseEntity<Map<String, Object>>>map(profile -> ResponseEntity.ok(Map.of(
                        "email", profile.getEmail(),
                        "displayName", profile.getDisplayName() == null ? "" : profile.getDisplayName(),
                        "onboarded", profile.isOnboarded()
                )))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    @PatchMapping("/onboarded")
    public ResponseEntity<Void> markOnboarded(@CurrentUserId String userId) {
        userRepository.findProfile(userId).ifPresent(profile -> {
            profile.setOnboarded(true);
            userRepository.saveProfile(profile);
        });
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@CurrentUserId String userId, @RequestBody Map<String, String> body) {
        userRepository.findProfile(userId).ifPresent(profile -> {
            String displayName = body.get("displayName");
            if (displayName != null) {
                profile.setDisplayName(displayName);
            }
            userRepository.saveProfile(profile);
        });
        return ResponseEntity.ok().build();
    }

    private void issueTokens(String userId, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(userId);

        String tokenId = UUID.randomUUID().toString();
        RefreshTokenItem refreshItem = new RefreshTokenItem();
        refreshItem.setPk(userId);
        refreshItem.setSk("REFRESH#" + tokenId);
        refreshItem.setTokenId(tokenId);
        refreshItem.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS).getEpochSecond());
        refreshItem.setCreatedAt(Instant.now().toString());
        userRepository.saveRefreshToken(refreshItem);

        setCookie(response, "accessToken", accessToken, "/", 15 * 60);
        setCookie(response, "refreshToken", userId + "." + tokenId, "/api/auth/refresh", (int) REFRESH_TOKEN_TTL_SECONDS);
    }

    private void setCookie(HttpServletResponse response, String name, String value, String path, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Optional<String> extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}