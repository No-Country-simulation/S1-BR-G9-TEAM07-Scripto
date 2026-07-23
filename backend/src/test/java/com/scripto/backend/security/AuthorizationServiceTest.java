package com.scripto.backend.security;

import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCarregarUsuarioPorEmail() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(user);

        // Act
        UserDetails resultado =
                authorizationService.loadUserByUsername("joao@email.com");

        // Assert
        assertEquals(user, resultado);

        verify(userRepository)
                .findByEmail("joao@email.com");
    }
}