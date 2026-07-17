package com.scripto.backend.user.service;

import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.user.dto.UserUpdateDTO;
import com.scripto.backend.user.dto.UserViewDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<UserViewDTO> findAllUsers() {
        var users =  userRepository.findAll();
        var usersDto = users.stream().map(user -> new UserViewDTO(user)).toList();
        return usersDto;

    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public UserViewDTO findUserById(Long id) {
        var user = findUserEntityById(id);
        return new UserViewDTO(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updateUserById(Long id, UserUpdateDTO userUpdateDTO) {
        var user = findUserEntityById(id);
        user.update(userUpdateDTO);
        userRepository.save(user);
    }

    public void registerUser(@Valid UserRegisterDTO userRegisterDTO) {
        String encryptedPassword = passwordEncoder.encode(userRegisterDTO.password());
        User user = new User(userRegisterDTO.fullName(), userRegisterDTO.email(), userRegisterDTO.cpf(), encryptedPassword);
        this.userRepository.save(user);
    }
}
