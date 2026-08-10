package com.scripto.backend.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SecurityFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAllowRequestWithoutToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldAuthenticateUserWithValidToken() throws Exception {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        user.setActive(true);

        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(jwtService.validateToken("token-valido")).thenReturn("joao@email.com");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);

        MockHttpServletResponse response = new MockHttpServletResponse();
        securityFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).validateToken("token-valido");
        verify(userRepository).findByEmail("joao@email.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateInactiveUser() throws Exception {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        user.setActive(false);

        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(jwtService.validateToken("token-valido")).thenReturn("joao@email.com");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);

        MockHttpServletResponse response = new MockHttpServletResponse();
        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(jwtService.validateToken("token-invalido")).thenThrow(new JWTVerificationException("Invalid token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        securityFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldIgnoreHeaderWithoutBearerPrefix() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        MockHttpServletResponse response = new MockHttpServletResponse();
        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }
}