package com.scripto.backend.security;

import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
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
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);

        UserDetails resultado = authorizationService.loadUserByUsername("joao@email.com");

        assertEquals(user, resultado);
        verify(userRepository).findByEmail("joao@email.com");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(userRepository.findByEmail("inexistente@email.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> authorizationService.loadUserByUsername("inexistente@email.com"));
    }

    @Test
    void deveNormalizarEmail() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);

        UserDetails resultado = authorizationService.loadUserByUsername("  JOAO@email.com  ");

        assertEquals(user, resultado);
        verify(userRepository).findByEmail("joao@email.com");
    }
}