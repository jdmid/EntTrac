package com.enttrac.backend.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-key-at-least-32-characters-long-for-hs256");
    }

    @Test
    void generateAndValidate_ShouldRoundTripUserId() {
        String token = jwtService.generateAccessToken("USER#google#123");

        var result = jwtService.validateAndGetUserId(token);

        assertTrue(result.isPresent());
        assertEquals("USER#google#123", result.get());
    }

    @Test
    void validateAndGetUserId_ShouldRejectGarbageToken() {
        var result = jwtService.validateAndGetUserId("not-a-real-token");

        assertTrue(result.isEmpty());
    }

    @Test
    void validateAndGetUserId_ShouldRejectTokenSignedWithDifferentSecret() {
        JwtService otherService = new JwtService("a-completely-different-secret-key-32-chars");
        String token = otherService.generateAccessToken("USER#google#123");

        var result = jwtService.validateAndGetUserId(token);

        assertTrue(result.isEmpty());
    }

    @Test
    void validateAndGetUserId_ShouldRejectEmptyToken() {
        var result = jwtService.validateAndGetUserId("");

        assertTrue(result.isEmpty());
    }
}
