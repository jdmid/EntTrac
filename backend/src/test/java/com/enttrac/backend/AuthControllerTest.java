package com.enttrac.backend;

import com.enttrac.backend.auth.GoogleTokenVerifierService;
import com.enttrac.backend.auth.JwtService;
import com.enttrac.backend.controller.AuthController;
import com.enttrac.backend.model.item.RefreshTokenItem;
import com.enttrac.backend.model.item.UserProfileItem;
import com.enttrac.backend.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(JwtService.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GoogleTokenVerifierService googleTokenVerifierService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void googleLogin_ShouldCreateProfileAndSetCookies_WhenNewUser() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-123");
        payload.setEmail("test@example.com");

        when(googleTokenVerifierService.verify("valid-google-token")).thenReturn(Optional.of(payload));
        when(userRepository.findProfile("USER#google#google-sub-123")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", "valid-google-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.onboarded").value(false))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().path("refreshToken", "/api/auth/refresh"));

        verify(userRepository).saveProfile(any());
        verify(userRepository).saveRefreshToken(any());
    }

    @Test
    void googleLogin_ShouldReturn401_WhenGoogleTokenInvalid() throws Exception {
        when(googleTokenVerifierService.verify("bad-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", "bad-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void googleLogin_ShouldReturn403_WhenUserDisabled() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-123");
        payload.setEmail("test@example.com");

        UserProfileItem disabledProfile = new UserProfileItem();
        disabledProfile.setDisabled(true);

        when(googleTokenVerifierService.verify("valid-token")).thenReturn(Optional.of(payload));
        when(userRepository.findProfile("USER#google#google-sub-123")).thenReturn(Optional.of(disabledProfile));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", "valid-token"))))
                .andExpect(status().isForbidden());

        verify(userRepository, never()).saveProfile(any());
    }

    @Test
    void refresh_ShouldRotateTokenAndSetNewCookies_WhenValid() throws Exception {
        RefreshTokenItem stored = new RefreshTokenItem();
        stored.setExpiresAt(Instant.now().plusSeconds(3600).getEpochSecond());

        UserProfileItem profile = new UserProfileItem();
        profile.setDisabled(false);

        when(userRepository.findRefreshToken("USER#google#123", "old-token-id")).thenReturn(Optional.of(stored));
        when(userRepository.findProfile("USER#google#123")).thenReturn(Optional.of(profile));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "USER#google#123.old-token-id")))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));

        verify(userRepository).deleteRefreshToken("USER#google#123", "old-token-id");
        verify(userRepository).saveRefreshToken(any());
    }

    @Test
    void refresh_ShouldReturn401_WhenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_ShouldReturn401_WhenTokenExpired() throws Exception {
        RefreshTokenItem stored = new RefreshTokenItem();
        stored.setExpiresAt(Instant.now().minusSeconds(3600).getEpochSecond());

        when(userRepository.findRefreshToken("USER#google#123", "old-token-id")).thenReturn(Optional.of(stored));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "USER#google#123.old-token-id")))
                .andExpect(status().isUnauthorized());

        verify(userRepository, never()).deleteRefreshToken(any(), any());
    }

    @Test
    void refresh_ShouldReturn401_WhenTokenUnknown() throws Exception {
        when(userRepository.findRefreshToken("USER#google#123", "fake-id")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "USER#google#123.fake-id")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_ShouldDeleteRefreshTokenAndClearCookies() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", "USER#google#123.token-id")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(userRepository).deleteRefreshToken("USER#google#123", "token-id");
    }
// --- /me ---

    @Test
    void me_ShouldReturnProfile_WhenUserExists() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        UserProfileItem profile = new UserProfileItem();
        profile.setEmail("test@example.com");
        profile.setDisplayName("Joseph");
        profile.setOnboarded(true);

        when(userRepository.findProfile(userId)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.displayName").value("Joseph"))
                .andExpect(jsonPath("$.onboarded").value(true));
    }

    @Test
    void me_ShouldReturnEmptyDisplayName_WhenDisplayNameIsNull() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        UserProfileItem profile = new UserProfileItem();
        profile.setEmail("test@example.com");
        profile.setDisplayName(null);
        profile.setOnboarded(false);

        when(userRepository.findProfile(userId)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value(""));
    }

    @Test
    void me_ShouldReturn404_WhenUserNotFound() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        when(userRepository.findProfile(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isNotFound());
    }

// --- /onboarded ---

    @Test
    void markOnboarded_ShouldSetOnboardedTrueAndSave_WhenUserExists() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        UserProfileItem profile = new UserProfileItem();
        profile.setOnboarded(false);

        when(userRepository.findProfile(userId)).thenReturn(Optional.of(profile));

        mockMvc.perform(patch("/api/auth/onboarded")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk());

        verify(userRepository).saveProfile(argThat(p -> p.isOnboarded()));
    }

    @Test
    void markOnboarded_ShouldReturn200_WhenUserNotFound() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        when(userRepository.findProfile(userId)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/auth/onboarded")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk());

        verify(userRepository, never()).saveProfile(any());
    }

// --- /profile ---

    @Test
    void updateProfile_ShouldUpdateDisplayNameAndSave_WhenUserExists() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        UserProfileItem profile = new UserProfileItem();
        profile.setDisplayName("Old Name");

        when(userRepository.findProfile(userId)).thenReturn(Optional.of(profile));

        mockMvc.perform(patch("/api/auth/profile")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("displayName", "New Name"))))
                .andExpect(status().isOk());

        verify(userRepository).saveProfile(argThat(p -> "New Name".equals(p.getDisplayName())));
    }

    @Test
    void updateProfile_ShouldNotChangeDisplayName_WhenKeyAbsentFromBody() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        UserProfileItem profile = new UserProfileItem();
        profile.setDisplayName("Unchanged");

        when(userRepository.findProfile(userId)).thenReturn(Optional.of(profile));

        mockMvc.perform(patch("/api/auth/profile")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isOk());

        verify(userRepository).saveProfile(argThat(p -> "Unchanged".equals(p.getDisplayName())));
    }

    @Test
    void updateProfile_ShouldReturn200_WhenUserNotFound() throws Exception {
        String userId = "USER#google#google-sub-123";
        String accessToken = jwtService.generateAccessToken(userId);

        when(userRepository.findProfile(userId)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/auth/profile")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("displayName", "Whatever"))))
                .andExpect(status().isOk());

        verify(userRepository, never()).saveProfile(any());
    }
}