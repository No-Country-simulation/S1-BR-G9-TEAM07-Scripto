package com.scripto.backend.security;

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

import static org.mockito.Mockito.*;

class SecurityFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void devePermitirRequisicaoSemToken() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void deveAutenticarUsuarioComTokenValido() throws Exception {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        user.setActive(true);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-valido");

        when(jwtService.validateToken("token-valido"))
                .thenReturn("joao@email.com");

        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(user);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken("token-valido");

        verify(userRepository)
                .findByEmail("joao@email.com");

        verify(filterChain)
                .doFilter(request, response);
    }
    @Test
    void naoDeveAutenticarUsuarioInativo() throws Exception {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        user.setActive(false);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-valido");

        when(jwtService.validateToken("token-valido"))
                .thenReturn("joao@email.com");

        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(user);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain)
                .doFilter(request, response);
    }
}