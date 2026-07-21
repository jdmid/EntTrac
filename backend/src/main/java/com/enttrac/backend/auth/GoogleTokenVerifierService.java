package com.enttrac.backend.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
public class GoogleTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;

    @Autowired
    public GoogleTokenVerifierService(@Value("${google.oauth.client-id}") String clientId)
            throws GeneralSecurityException, IOException {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    GoogleTokenVerifierService(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    public Optional<GoogleIdToken.Payload> verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            return idToken != null ? Optional.of(idToken.getPayload()) : Optional.empty();
        } catch (GeneralSecurityException | IOException e) {
            return Optional.empty();
        }
    }
}