package com.enttrac.backend.auth;

import jakarta.servlet.http.Cookie;

public class AuthTestSupport {

    public static final String TEST_USER_ID = "USER#google#test-user-123";

    public static Cookie accessTokenCookie(JwtService jwtService) {
        return accessTokenCookie(jwtService, TEST_USER_ID);
    }

    public static Cookie accessTokenCookie(JwtService jwtService, String userId) {
        return new Cookie("accessToken", jwtService.generateAccessToken(userId));
    }
}
