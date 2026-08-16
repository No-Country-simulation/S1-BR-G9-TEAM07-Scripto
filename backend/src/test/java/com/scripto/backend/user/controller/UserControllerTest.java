package com.scripto.backend.user.controller;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.TokenJWTDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.user.dto.PasswordResetDTO;
import com.scripto.backend.user.dto.PasswordResetVerificationDTO;
import com.scripto.backend.user.dto.UserPasswordChangeDTO;
import com.scripto.backend.user.dto.UserProfileUpdateDTO;
import com.scripto.backend.user.dto.UserReactivateAccountDTO;
import com.scripto.backend.user.dto.UserViewDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCadastrarUsuario() {
        UserRegisterDTO dto = new UserRegisterDTO("12345678901", "João da Silva", "joao@email.com", "123456");
        var response = userController.registerUser(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService).registerUser(dto);
    }

    @Test
    void deveRetornar409QuandoEmailDuplicado() {
        UserRegisterDTO dto = new UserRegisterDTO("12345678901", "João da Silva", "joao@email.com", "123456");
        doThrow(new BusinessRuleException("E-mail já cadastrado.")).when(userService).registerUser(dto);
        assertThrows(BusinessRuleException.class, () -> userController.registerUser(dto));
        verify(userService).registerUser(dto);
    }

    @Test
    void deveRealizarLogin() {
        LoginDTO dto = new LoginDTO("joao@email.com", "123456");
        when(userService.loginUser(dto)).thenReturn("token-jwt");
        var response = userController.loginUser(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TokenJWTDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("token-jwt", body.token());
        verify(userService).loginUser(dto);
    }

    @Test
    void deveAtualizarProprioPerfil() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO("João Pedro", "joaopedro@email.com");
        UserViewDTO view = new UserViewDTO(1L, "João Pedro", "joaopedro@email.com", "12345678901");
        when(userService.updateOwnProfile(user, dto)).thenReturn(view);

        var response = userController.updateOwnProfile(user, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("João Pedro", response.getBody().fullName());
    }

    @Test
    void deveAlterarPropriaSenha() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        UserPasswordChangeDTO dto = new UserPasswordChangeDTO("atual", "Senha@123", "Senha@123");

        var response = userController.changeOwnPassword(user, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).changeOwnPassword(user, dto);
    }

    @Test
    void deveReativarConta() {
        UserReactivateAccountDTO dto = new UserReactivateAccountDTO("joao@email.com", "123456");
        var response = userController.reactivateAccount(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).reactivateAccount(dto);
    }

    @Test
    void deveVerificarIdentidadeParaRedefinicaoDeSenha() {
        PasswordResetVerificationDTO dto = new PasswordResetVerificationDTO("123.456.789-01", "joao@email.com");
        var response = userController.verifyPasswordResetIdentity(dto);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).verifyPasswordResetIdentity(dto);
    }

    @Test
    void deveRedefinirSenha() {
        PasswordResetDTO dto = new PasswordResetDTO("123.456.789-01", "joao@email.com", "Senha@123", "Senha@123");
        var response = userController.resetPassword(dto);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).resetPassword(dto);
    }
}