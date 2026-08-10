package com.scripto.backend.user.controller;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.TokenJWTDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.user.dto.UserPasswordChangeDTO;
import com.scripto.backend.user.dto.UserProfileUpdateDTO;
import com.scripto.backend.user.dto.UserReactivateAccountDTO;
import com.scripto.backend.user.dto.UserViewDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuários", description = "Cadastro, autenticação e gerenciamento de usuários")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Listar usuários", description = "Lista os usuários cadastrados. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserViewDTO>> getAllUsers() {
        var users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @Tag(name = "Autenticação")
    @Operation(summary = "Cadastrar usuário", description = "Cria uma nova conta na plataforma Scripto.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        userService.registerUser(userRegisterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Tag(name = "Autenticação")
    @Operation(summary = "Realizar login", description = "Autentica o usuário e retorna um token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenJWTDTO> loginUser(@RequestBody @Valid LoginDTO loginDTO) {
        var token = this.userService.loginUser(loginDTO);
        return ResponseEntity.ok(new TokenJWTDTO(token));
    }

    @Tag(name = "Autenticação")
    @Operation(summary = "Reativar conta", description = "Reativa uma conta que ainda está dentro do prazo de recuperação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta reativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou conta fora do prazo"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @PostMapping("/reactivate")
    public ResponseEntity<Void> reactivateAccount(@RequestBody @Valid UserReactivateAccountDTO userReactivateAccountDTO) {
        userService.reactivateAccount(userReactivateAccountDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Atualizar próprio perfil", description = "Atualiza nome e e-mail do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "409", description = "E-mail já utilizado")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @GetMapping("/me")
    public ResponseEntity<UserViewDTO> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(new UserViewDTO(user));
    }

    @Operation(summary = "Atualizar próprio perfil", description = "Atualiza nome e e-mail do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "409", description = "E-mail já utilizado")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @PatchMapping("/me/profile")
    public ResponseEntity<UserViewDTO> updateOwnProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestBody @Valid UserProfileUpdateDTO dto
    ) {
        return ResponseEntity.ok(userService.updateOwnProfile(user, dto));
    }

    @Operation(summary = "Alterar própria senha", description = "Altera a senha do usuário autenticado. Exige senha atual e confirmação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha atual incorreta"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeOwnPassword(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestBody @Valid UserPasswordChangeDTO dto
    ) {
        userService.changeOwnPassword(user, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Excluir a própria conta", description = "Realiza a exclusão lógica da conta autenticada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta marcada para exclusão"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteOwnAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        userService.softDeleteAccount(user.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar perfil de usuário (admin)", description = "Atualiza nome e e-mail de outro usuário. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail já utilizado")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/profile")
    public ResponseEntity<UserViewDTO> updateUserProfile(
            @Parameter(description = "ID do usuário", example = "12") @PathVariable Long id,
            @RequestBody @Valid UserProfileUpdateDTO dto
    ) {
        return ResponseEntity.ok(userService.updateUserProfileByAdmin(id, dto));
    }

    @Operation(summary = "Consultar usuário por ID", description = "Consulta os dados de um usuário. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserViewDTO> getUserById(
            @Parameter(description = "ID do usuário", example = "12") @PathVariable Long id
    ) {
        var user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Excluir usuário", description = "Exclui um usuário pelo ID. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User authenticatedUser) {
        userService.deleteUserByAdmin(id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}