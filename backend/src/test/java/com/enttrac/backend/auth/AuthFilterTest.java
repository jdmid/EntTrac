package com.enttrac.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    private AuthFilter authFilter;

    @Test
    void doFilter_ShouldSetUserIdAndContinueChain_WhenTokenValid() throws Exception {
        authFilter = new AuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/anime/library");
        request.setCookies(new Cookie("accessToken", "valid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateAndGetUserId("valid-token")).thenReturn(java.util.Optional.of("USER#google#123"));

        authFilter.doFilter(request, response, filterChain);

        assertEquals("USER#google#123", request.getAttribute("userId"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_ShouldReturn401_WhenNoCookiePresent() throws Exception {
        authFilter = new AuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/anime/library");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFilter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_ShouldReturn401_WhenTokenInvalid() throws Exception {
        authFilter = new AuthFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/anime/library");
        request.setCookies(new Cookie("accessToken", "bad-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateAndGetUserId("bad-token")).thenReturn(java.util.Optional.empty());

        authFilter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldNotFilter_ShouldSkipAuthEndpoints() {
        authFilter = new AuthFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/google");

        assertTrue(authFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldSkipNonApiPaths() {
        authFilter = new AuthFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");

        assertTrue(authFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldNotSkipProtectedApiPaths() {
        authFilter = new AuthFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/anime/library");

        assertFalse(authFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ShouldSkipOptionsPreflightRequests() {
        authFilter = new AuthFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/anime/library");

        assertTrue(authFilter.shouldNotFilter(request));
    }
}
