package com.scripto.backend.user.service;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.exception.AccountBannedException;
import com.scripto.backend.exception.AccountDeletionExpiredException;
import com.scripto.backend.exception.AccountPendingReactivationException;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.exception.ResourceNotFoundException;
import com.scripto.backend.security.JwtService;
import com.scripto.backend.user.dto.PasswordResetDTO;
import com.scripto.backend.user.dto.PasswordResetVerificationDTO;
import com.scripto.backend.user.dto.SuspendedPasswordChangeDTO;
import com.scripto.backend.user.dto.UserPasswordChangeDTO;
import com.scripto.backend.user.dto.UserProfileUpdateDTO;
import com.scripto.backend.user.dto.UserReactivateAccountDTO;
import com.scripto.backend.user.dto.UserViewDTO;
import com.scripto.backend.user.entity.Role;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private static final String CURRENT_TERMS_VERSION = "2026-08-08";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public List<UserViewDTO> findAllUsers() {
        return userRepository.findAll().stream().map(UserViewDTO::new).toList();
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public UserViewDTO findUserById(Long id) {
        return new UserViewDTO(findUserEntityById(id));
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserViewDTO updateOwnProfile(User authenticatedUser, UserProfileUpdateDTO dto) {
        User user = findUserEntityById(authenticatedUser.getId());
        validateUniqueEmail(dto.email(), user);
        user.updateProfile(dto.fullName(), dto.email());
        userRepository.save(user);
        return new UserViewDTO(user);
    }

    @Transactional
    public UserViewDTO updateUserProfileByAdmin(Long userId, UserProfileUpdateDTO dto) {
        User user = findUserEntityById(userId);
        validateUniqueEmail(dto.email(), user);
        user.updateProfile(dto.fullName(), dto.email());
        userRepository.save(user);
        return new UserViewDTO(user);
    }

    private void validateUniqueEmail(String requestedEmail, User user) {
        if (requestedEmail != null && !requestedEmail.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(requestedEmail)) {
            throw new BusinessRuleException("Este e-mail já está em uso por outra conta.");
        }
    }

    @Transactional
    public void changeOwnPassword(User authenticatedUser, UserPasswordChangeDTO dto) {
        validatePasswordConfirmation(dto.newPassword(), dto.confirmNewPassword());
        User user = findUserEntityById(authenticatedUser.getId());
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Senha atual inválida.");
        }
        user.updatePassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void changeSuspendedPassword(SuspendedPasswordChangeDTO dto) {
        validatePasswordConfirmation(dto.newPassword(), dto.confirmNewPassword());

        User user = userRepository.findOptionalByEmail(dto.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));

        if (!matchesCpf(dto.cpf(), user) || !passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }
        if (Boolean.TRUE.equals(user.getBanned())) {
            throw new AccountBannedException("A conta está bloqueada administrativamente.");
        }
        if (!user.isPendingDeletion()) {
            throw new BusinessRuleException("A conta não está aguardando reativação.");
        }
        if (!user.canBeReactivated()) {
            throw new AccountDeletionExpiredException("O prazo de 30 dias para recuperação da conta expirou.");
        }

        user.updatePassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void verifyPasswordResetIdentity(PasswordResetVerificationDTO dto) {
        User user = userRepository.findOptionalByEmail(dto.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        validatePasswordResetEligibility(user, dto.cpf());
    }

    @Transactional
    public void resetPassword(PasswordResetDTO dto) {
        validatePasswordConfirmation(dto.newPassword(), dto.confirmNewPassword());

        User user = userRepository.findOptionalByEmail(dto.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        validatePasswordResetEligibility(user, dto.cpf());

        user.updatePassword(passwordEncoder.encode(dto.newPassword()));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    private void validatePasswordResetEligibility(User user, String cpf) {
        // Erro genérico: não revela se o e-mail existe ou se apenas o CPF não confere.
        if (!matchesCpf(cpf, user)) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }
        if (Boolean.TRUE.equals(user.getBanned())) {
            throw new AccountBannedException("A conta está bloqueada administrativamente.");
        }
        if (user.isPendingDeletion() && !user.canBeReactivated()) {
            throw new AccountDeletionExpiredException("O prazo de 30 dias para recuperação da conta expirou.");
        }
    }

    private void validatePasswordConfirmation(String password, String confirmation) {
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("A nova senha e a confirmação não coincidem.");
        }
    }

    @Transactional
    public void changePasswordByAdmin(Long userId, String newPassword) {
        User user = findUserEntityById(userId);
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changeRoleByAdmin(Long userId, Role role) {
        User user = findUserEntityById(userId);
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void setBannedByAdmin(Long userId, boolean banned) {
        User user = findUserEntityById(userId);
        if (banned) {
            user.ban();
        } else {
            user.unban();
        }
        userRepository.save(user);
    }

    @Transactional
    public void registerUser(@Valid UserRegisterDTO userRegisterDTO) {
        if (!Boolean.TRUE.equals(userRegisterDTO.termsAccepted())) {
            throw new BusinessRuleException("É necessário aceitar os Termos de Uso e a Política de Privacidade.");
        }
        if (userRepository.existsByEmail(userRegisterDTO.email())) {
            throw new BusinessRuleException("E-mail já cadastrado.");
        }
        if (userRepository.existsByCpf(userRegisterDTO.cpf())) {
            throw new BusinessRuleException("CPF já cadastrado.");
        }
        String encryptedPassword = passwordEncoder.encode(userRegisterDTO.password());
        User user = new User(userRegisterDTO.fullName(), userRegisterDTO.email(), userRegisterDTO.cpf(), encryptedPassword);
        user.acceptTerms(CURRENT_TERMS_VERSION);
        userRepository.save(user);
    }

    @Transactional
    public String loginUser(@Valid LoginDTO loginDTO) {
        User knownUser = userRepository.findOptionalByEmail(loginDTO.email()).orElse(null);
        if (knownUser != null && !Boolean.TRUE.equals(knownUser.getActive())) {
            if (!passwordEncoder.matches(loginDTO.password(), knownUser.getPassword())) {
                throw new BadCredentialsException("E-mail ou senha inválidos.");
            }
            if (Boolean.TRUE.equals(knownUser.getBanned())) {
                throw new AccountBannedException("A conta está bloqueada administrativamente.");
            }
            if (knownUser.isPendingDeletion()) {
                if (knownUser.canBeReactivated()) {
                    throw new AccountPendingReactivationException(
                            "Esta conta está desativada e ainda pode ser reativada dentro do prazo de 30 dias."
                    );
                }
                throw new AccountDeletionExpiredException(
                        "O prazo de 30 dias para reativação expirou e a conta aguarda exclusão definitiva."
                );
            }
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        var authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password());
        var authentication = authenticationManager.authenticate(authenticationToken);
        User user = (User) authentication.getPrincipal();
        user.registerSuccessfulLogin();
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Transactional
    public void softDeleteAccount(Long userId) {
        User user = findUserEntityById(userId);
        if (user.getRole() == Role.ADMIN && userRepository.countByRoleAndActiveTrueAndBannedFalse(Role.ADMIN) <= 1) {
            throw new BusinessRuleException("O último administrador do sistema não pode solicitar exclusão da própria conta.");
        }
        user.deactivate();
        userRepository.save(user);
    }

    @Transactional
    public void reactivateAccount(UserReactivateAccountDTO dto) {
        User user = userRepository.findOptionalByEmail(dto.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));

        if (Boolean.TRUE.equals(user.getBanned())) {
            throw new AccountBannedException("A conta está bloqueada administrativamente e não pode ser reativada por este fluxo.");
        }
        if (dto.cpf() != null && !matchesCpf(dto.cpf(), user)) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }
        if (!user.isPendingDeletion()) {
            throw new BusinessRuleException("A conta não está aguardando reativação.");
        }
        if (!user.canBeReactivated()) {
            throw new AccountDeletionExpiredException("O prazo de 30 dias para reativação da conta expirou.");
        }

        user.reactivate();
        userRepository.save(user);
    }

    private boolean matchesCpf(String cpf, User user) {
        return cpf != null && cpf.equals(user.getCpf());
    }
}
