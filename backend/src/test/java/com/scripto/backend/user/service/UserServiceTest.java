package com.scripto.backend.user.service;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.exception.ResourceNotFoundException;
import com.scripto.backend.security.JwtService;
import com.scripto.backend.user.dto.UserPasswordChangeDTO;
import com.scripto.backend.user.dto.UserProfileUpdateDTO;
import com.scripto.backend.user.dto.UserReactivateAccountDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
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
        UserRegisterDTO dto = new UserRegisterDTO("12345678901", "João da Silva", "joao@email.com", "123456");
        when(passwordEncoder.encode("123456")).thenReturn("senhaCriptografada");
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(userRepository.existsByCpf("12345678901")).thenReturn(false);

        userService.registerUser(dto);

        verify(passwordEncoder).encode("123456");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User usuarioSalvo = userCaptor.getValue();
        assertEquals("João da Silva", usuarioSalvo.getFullName());
        assertEquals("joao@email.com", usuarioSalvo.getEmail());
        assertEquals("senhaCriptografada", usuarioSalvo.getPassword());
    }

    @Test
    void deveLancarExcecaoQuandoEmailDuplicado() {
        UserRegisterDTO dto = new UserRegisterDTO("12345678901", "João da Silva", "joao@email.com", "123456");
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoCpfDuplicado() {
        UserRegisterDTO dto = new UserRegisterDTO("12345678901", "João da Silva", "joao@email.com", "123456");
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(userRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveRealizarLoginComSucesso() {
        LoginDTO loginDTO = new LoginDTO("joao@email.com", "123456");
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senhaCriptografada");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(user)).thenReturn("meu-token-jwt");

        String token = userService.loginUser(loginDTO);

        assertEquals("meu-token-jwt", token);
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(user);
    }

    @Test
    void deveAtualizarProprioPerfil() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        user.setId(1L);
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO("João Pedro", "joaopedro@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("joaopedro@email.com")).thenReturn(false);

        var result = userService.updateOwnProfile(user, dto);

        assertEquals("João Pedro", result.fullName());
        assertEquals("joaopedro@email.com", result.email());
        verify(userRepository).save(user);
    }

    @Test
    void deveAlterarSenha() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senhaAntiga");
        user.setId(1L);
        UserPasswordChangeDTO dto = new UserPasswordChangeDTO("senhaAtual", "Senha@123", "Senha@123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaAtual", "senhaAntiga")).thenReturn(true);
        when(passwordEncoder.encode("Senha@123")).thenReturn("senhaNovaCriptografada");

        userService.changeOwnPassword(user, dto);

        verify(userRepository).save(user);
        assertEquals("senhaNovaCriptografada", user.getPassword());
    }

    @Test
    void deveLancarExcecaoQuandoSenhasNaoConferem() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        user.setId(1L);
        UserPasswordChangeDTO dto = new UserPasswordChangeDTO("senhaAtual", "Senha@123", "Senha@456");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.changeOwnPassword(user, dto));
    }

    @Test
    void deveRealizarSoftDelete() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        user.setId(1L);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.softDeleteAccount(1L);

        assertEquals(false, user.getActive());
        assertNotNull(user.getDeletedAt());
        verify(userRepository).save(user);
    }

    @Test
    void deveReativarContaComSucesso() {
        UserReactivateAccountDTO dto = new UserReactivateAccountDTO("joao@email.com", "123456");
        User user = new User();
        user.setEmail("joao@email.com");
        user.setPasswordHash("senhaCriptografada");
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now().minusDays(10));
        user.setFailedLoginAttempts(3);
        when(userRepository.findOptionalByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "senhaCriptografada")).thenReturn(true);

        userService.reactivateAccount(dto);

        assertTrue(user.getActive());
        assertNull(user.getDeletedAt());
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoPuderSerReativada() {
        UserReactivateAccountDTO dto = new UserReactivateAccountDTO("joao@email.com", "123456");
        User user = new User();
        user.setEmail("joao@email.com");
        user.setPasswordHash("senhaCriptografada");
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now().minusDays(31));
        when(userRepository.findOptionalByEmail("joao@email.com")).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> userService.reactivateAccount(dto));
    }
}