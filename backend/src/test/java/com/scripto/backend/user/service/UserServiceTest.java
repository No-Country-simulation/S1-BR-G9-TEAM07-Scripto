package com.scripto.backend.user.service;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.security.JwtService;
import com.scripto.backend.user.dto.UserReactivateAccountDTO;
import com.scripto.backend.user.dto.UserUpdateDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
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
                "12345678901",
                "João da Silva",
                "joao@email.com",
                "123456"
        );

        when(passwordEncoder.encode("123456"))
                .thenReturn("senhaCriptografada");

        // Act
        userService.registerUser(dto);

        // Assert
        verify(passwordEncoder).encode("123456");


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User usuarioSalvo = userCaptor.getValue();
        assertEquals("João da Silva", usuarioSalvo.getFullName());
        assertEquals("joao@email.com", usuarioSalvo.getEmail());
        assertEquals("senhaCriptografada", usuarioSalvo.getPassword());
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
    @Test
    void deveReativarContaComSucesso() {

        // Arrange
        UserReactivateAccountDTO dto = new UserReactivateAccountDTO(
                "joao@email.com",
                "123456"
        );

        User user = new User();
        user.setEmail("joao@email.com");
        user.setPasswordHash("senhaCriptografada");
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now().minusDays(10));
        user.setFailedLoginAttempts(3);

        when(userRepository.findOptionalByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "senhaCriptografada"))
                .thenReturn(true);

        // Act
        userService.reactivateAccount(dto);

        // Assert
        assertTrue(user.getActive());
        assertNull(user.getDeletedAt());
        assertEquals(0, user.getFailedLoginAttempts());

        verify(userRepository).findOptionalByEmail("joao@email.com");
        verify(passwordEncoder)
                .matches("123456", "senhaCriptografada");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {

        // Arrange
        UserReactivateAccountDTO dto = new UserReactivateAccountDTO(
                "joao@email.com",
                "123456"
        );

        when(userRepository.findOptionalByEmail("joao@email.com"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> userService.reactivateAccount(dto)
        );

        verify(userRepository).findOptionalByEmail("joao@email.com");
        verify(passwordEncoder, never()).matches(any(), any());

    }

    @Test
    void deveLancarExcecaoQuandoSenhaForIncorreta() {

        // Arrange
        UserReactivateAccountDTO dto = new UserReactivateAccountDTO(
                "joao@email.com",
                "123456"
        );

        User user = new User();
        user.setDeletedAt(LocalDateTime.now().minusDays(5));
        user.setActive(false);
        user.setPasswordHash("senhaCriptografada");

        when(userRepository.findOptionalByEmail("joao@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "senhaCriptografada"))
                .thenReturn(false);

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.reactivateAccount(dto)
        );
    }




}