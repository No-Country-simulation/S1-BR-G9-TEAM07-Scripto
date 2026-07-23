package com.scripto.backend.user.service;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.security.JwtService;
import com.scripto.backend.user.dto.UserUpdateDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCadastrarUsuario() {

        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "123456"
        );

        when(passwordEncoder.encode("123456"))
                .thenReturn("senhaCriptografada");

        // Act
        userService.registerUser(dto);

        // Assert
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveRealizarLoginComSucesso() {

        // Arrange
        LoginDTO loginDTO = new LoginDTO(
                "joao@email.com",
                "123456"
        );

        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senhaCriptografada"
        );

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(user);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtService.generateToken(user))
                .thenReturn("meu-token-jwt");

        // Act
        String token = userService.loginUser(loginDTO);

        // Assert
        assertEquals("meu-token-jwt", token);

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(user);
    }

    @Test
    void deveBuscarUsuarioPorEmail() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senhaCriptografada"
        );

        when(userRepository.findByEmail("joao@email.com"))
                .thenReturn(user);

        // Act
        User resultado = userService.findUserByEmail("joao@email.com");

        // Assert
        assertEquals(user, resultado);

        verify(userRepository).findByEmail("joao@email.com");
    }

    @Test
    void deveBuscarUsuarioPorId() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senhaCriptografada"
        );

        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        // Act
        var resultado = userService.findUserById(1L);

        // Assert
        assertEquals("João da Silva", resultado.fullName());
        assertEquals("joao@email.com", resultado.email());

        verify(userRepository).findById(1L);
    }

    @Test
    void deveAtualizarUsuario() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senhaAntiga"
        );

        user.setId(1L);

        UserUpdateDTO dto = new UserUpdateDTO(
                "João Pedro",
                "joaopedro@email.com",
                "novaSenha"
        );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.encode("novaSenha"))
                .thenReturn("senhaCriptografada");

        // Act
        userService.updateUserById(1L, dto);

        // Assert
        verify(passwordEncoder).encode("novaSenha");
        verify(userRepository).save(user);

        assertEquals("João Pedro", user.getFullName());
        assertEquals("joaopedro@email.com", user.getEmail());
        assertEquals("senhaCriptografada", user.getPassword());
    }

    @Test
    void deveRealizarSoftDelete() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        // Act
        userService.softDeleteAccount(1L);

        // Assert
        assertEquals(false, user.getActive());
        assertNotNull(user.getDeletedAt());

        verify(userRepository).save(user);
    }


}