package com.scripto.backend.user.service;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.exception.ResourceNotFoundException;
import com.scripto.backend.security.JwtService;
import com.scripto.backend.user.dto.UserPasswordChangeDTO;
import com.scripto.backend.user.dto.UserProfileUpdateDTO;
import com.scripto.backend.user.dto.UserReactivateAccountDTO;
import com.scripto.backend.user.dto.UserViewDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
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

    public List<UserViewDTO> findAllUsers() {
        var users = userRepository.findAll();
        return users.stream().map(UserViewDTO::new).toList();
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    public UserViewDTO findUserById(Long id) {
        var user = findUserEntityById(id);
        return new UserViewDTO(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserViewDTO updateOwnProfile(User authenticatedUser, UserProfileUpdateDTO dto) {
        User user = findUserEntityById(authenticatedUser.getId());
        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new BusinessRuleException("Este e-mail já está em uso por outra conta.");
            }
        }
        user.updateProfile(dto.fullName(), dto.email());
        userRepository.save(user);
        return new UserViewDTO(user);
    }

    @Transactional
    public UserViewDTO updateUserProfileByAdmin(Long userId, UserProfileUpdateDTO dto) {
        User user = findUserEntityById(userId);
        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new BusinessRuleException("Este e-mail já está em uso por outra conta.");
            }
        }
        user.updateProfile(dto.fullName(), dto.email());
        userRepository.save(user);
        return new UserViewDTO(user);
    }

    @Transactional
    public void changeOwnPassword(User authenticatedUser, UserPasswordChangeDTO dto) {
        if (!dto.newPassword().equals(dto.confirmNewPassword())) {
            throw new IllegalArgumentException("A nova senha e a confirmação não coincidem.");
        }
        User user = findUserEntityById(authenticatedUser.getId());
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Senha atual inválida.");
        }
        user.updatePassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void changePasswordByAdmin(Long userId, String newPassword) {
        User user = findUserEntityById(userId);
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void registerUser(@Valid UserRegisterDTO userRegisterDTO) {
        if (userRepository.existsByEmail(userRegisterDTO.email())) {
            throw new BusinessRuleException("E-mail já cadastrado.");
        }
        if (userRepository.existsByCpf(userRegisterDTO.cpf())) {
            throw new BusinessRuleException("CPF já cadastrado.");
        }
        String encryptedPassword = passwordEncoder.encode(userRegisterDTO.password());
        User user = new User(userRegisterDTO.fullName(), userRegisterDTO.email(), userRegisterDTO.cpf(), encryptedPassword);
        this.userRepository.save(user);
    }

    @Transactional
    public String loginUser(@Valid LoginDTO loginDTO) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password());
        var authentication = authenticationManager.authenticate(authenticationToken);

        User user = (User) authentication.getPrincipal();
        user.registerSuccessfulLogin();
        return jwtService.generateToken((User) authentication.getPrincipal());
    }

    @Transactional
    public void softDeleteAccount(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        user.deactivate();
        userRepository.save(user);
    }

    @Transactional
    public void reactivateAccount(UserReactivateAccountDTO userReactivateAccountDTO) {
        var user = userRepository.findOptionalByEmail(userReactivateAccountDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.canBeReactivated()) {
            throw new BusinessRuleException("O prazo para reativação da conta expirou ou a conta não está elegível para reativação.");
        }

        if (!passwordEncoder.matches(userReactivateAccountDTO.password(), user.getPassword())) {
            throw new IllegalArgumentException("Senha inválida.");
        }

        user.reactivate();
    }
}