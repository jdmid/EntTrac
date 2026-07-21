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
}