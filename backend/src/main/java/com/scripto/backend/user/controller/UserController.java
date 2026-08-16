package com.scripto.backend.user.controller;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.TokenJWTDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.user.dto.PasswordResetDTO;
import com.scripto.backend.user.dto.PasswordResetVerificationDTO;
import com.scripto.backend.user.dto.SuspendedPasswordChangeDTO;
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
            @ApiResponse(responseCode = "400", description = "JSON inválido"),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado"),
            @ApiResponse(responseCode = "422", description = "Campos inválidos ou termos não aceitos")
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
            @ApiResponse(responseCode = "400", description = "JSON inválido"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos"),
            @ApiResponse(responseCode = "403", description = "Conta em soft-delete ainda dentro do prazo de reativação"),
            @ApiResponse(responseCode = "410", description = "Prazo de reativação expirado"),
            @ApiResponse(responseCode = "422", description = "Formato de e-mail ou campos inválidos"),
            @ApiResponse(responseCode = "423", description = "Conta banida administrativamente"),
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
            @ApiResponse(responseCode = "401", description = "CPF, e-mail ou senha inválidos"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conta não está aguardando reativação"),
            @ApiResponse(responseCode = "410", description = "Prazo de reativação expirado"),
            @ApiResponse(responseCode = "422", description = "Campos inválidos"),
            @ApiResponse(responseCode = "423", description = "Conta banida administrativamente"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de autenticação")
    })
    @PostMapping("/reactivate")
    public ResponseEntity<Void> reactivateAccount(@RequestBody @Valid UserReactivateAccountDTO userReactivateAccountDTO) {
        userService.reactivateAccount(userReactivateAccountDTO);
        return ResponseEntity.ok().build();
    }

    @Tag(name = "Autenticação")
    @Operation(summary = "Alterar senha de conta em soft-delete", description = "Permite trocar a senha sem reativar a conta, desde que CPF, e-mail e senha atual sejam confirmados e o prazo de 30 dias ainda esteja vigente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada; conta permanece desativada"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "409", description = "Conta não está aguardando reativação"),
            @ApiResponse(responseCode = "410", description = "Prazo de reativação expirado"),
            @ApiResponse(responseCode = "422", description = "Campos inválidos"),
            @ApiResponse(responseCode = "423", description = "Conta banida administrativamente"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de autenticação")
    })
    @PatchMapping("/suspended/password")
    public ResponseEntity<Void> changeSuspendedPassword(@RequestBody @Valid SuspendedPasswordChangeDTO dto) {
        userService.changeSuspendedPassword(dto);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "Autenticação")
    @Operation(summary = "Verificar identidade para redefinição de senha", description = "Confirma CPF e e-mail antes de permitir a redefinição de senha. Não revela se a conta existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Identidade confirmada"),
            @ApiResponse(responseCode = "401", description = "CPF ou e-mail inválidos"),
            @ApiResponse(responseCode = "410", description = "Prazo de recuperação expirado"),
            @ApiResponse(responseCode = "422", description = "Campos inválidos"),
            @ApiResponse(responseCode = "423", description = "Conta banida administrativamente"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de autenticação")
    })
    @PostMapping("/password-reset/verify")
    public ResponseEntity<Void> verifyPasswordResetIdentity(@RequestBody @Valid PasswordResetVerificationDTO dto) {
        userService.verifyPasswordResetIdentity(dto);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "Autenticação")
    @Operation(summary = "Redefinir senha", description = "Define uma nova senha após confirmação de CPF e e-mail.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "401", description = "CPF ou e-mail inválidos"),
            @ApiResponse(responseCode = "410", description = "Prazo de recuperação expirado"),
            @ApiResponse(responseCode = "422", description = "Campos inválidos ou nova senha fora das regras"),
            @ApiResponse(responseCode = "423", description = "Conta banida administrativamente"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de autenticação")
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid PasswordResetDTO dto) {
        userService.resetPassword(dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar próprio perfil", description = "Retorna nome completo, e-mail e CPF do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil consultado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
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
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "409", description = "E-mail já utilizado"),
            @ApiResponse(responseCode = "422", description = "Campos inválidos")
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
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou confirmação divergente"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "422", description = "Nova senha não atende às regras de validação")
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
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "409", description = "Operação impediria a existência de um administrador ativo")
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
}