package com.scripto.backend.user.controller;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.TokenJWTDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.user.dto.UserReactivateAccountDTO;
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

        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO(
                "12345678901",
                "João da Silva",
                "joao@email.com",
                "123456"
        );

        when(userService.findUserByEmail(dto.email()))
                .thenReturn(null);

        // Act
        var response = userController.registerUser(dto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(userService).findUserByEmail(dto.email());
        verify(userService).registerUser(dto);
    }

    @Test
    void naoDeveCadastrarUsuarioComEmailDuplicado() {

        // Arrange
        UserRegisterDTO dto = new UserRegisterDTO(
                "12345678901",
                "João da Silva",
                "joao@email.com",
                "123456"
        );

        User usuarioExistente = new User();

        when(userService.findUserByEmail(dto.email()))
                .thenReturn(usuarioExistente);

        // Act
        var response = userController.registerUser(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(userService).findUserByEmail(dto.email());
        verify(userService, never()).registerUser(any());
    }

    @Test
    void deveRealizarLogin() {

        // Arrange
        LoginDTO dto = new LoginDTO(
                "joao@email.com",
                "123456"
        );

        when(userService.loginUser(dto))
                .thenReturn("token-jwt");

        // Act
        var response = userController.loginUser(dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        TokenJWTDTO body = response.getBody();

        assertNotNull(body);
        assertEquals("token-jwt", body.token());

        verify(userService).loginUser(dto);
    }

    @Test
    void deveReativarConta() {

        // Arrange
        UserReactivateAccountDTO dto = new UserReactivateAccountDTO(
                "joao@email.com",
                "123456"
        );

        // Act
        var response = userController.reactivateAccount(dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(userService).reactivateAccount(dto);
    }
}