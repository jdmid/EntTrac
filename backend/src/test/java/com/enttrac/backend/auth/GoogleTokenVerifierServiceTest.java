package com.enttrac.backend.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierServiceTest {

    @Mock
    private GoogleIdTokenVerifier verifier;

    @Mock
    private GoogleIdToken googleIdToken;

    @Mock
    private GoogleIdToken.Payload payload;

    @Test
    void verify_ShouldReturnPayloadForValidToken() throws GeneralSecurityException, IOException {
        when(verifier.verify("valid-token")).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);

        GoogleTokenVerifierService service = new GoogleTokenVerifierService(verifier);
        var result = service.verify("valid-token");

        assertTrue(result.isPresent());
        assertEquals(payload, result.get());
    }

    @Test
    void verify_ShouldReturnEmptyWhenTokenInvalid() throws GeneralSecurityException, IOException {
        when(verifier.verify("bad-token")).thenReturn(null);

        GoogleTokenVerifierService service = new GoogleTokenVerifierService(verifier);
        var result = service.verify("bad-token");

        assertTrue(result.isEmpty());
    }

    @Test
    void verify_ShouldReturnEmptyWhenVerifierThrows() throws GeneralSecurityException, IOException {
        when(verifier.verify("bad-token")).thenThrow(new GeneralSecurityException("bad sig"));

        GoogleTokenVerifierService service = new GoogleTokenVerifierService(verifier);
        var result = service.verify("bad-token");

        assertTrue(result.isEmpty());
    }
}