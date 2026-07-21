package com.scripto.backend.user.controller;

import com.scripto.backend.auth.dto.LoginDTO;
import com.scripto.backend.auth.dto.TokenJWTDTO;
import com.scripto.backend.auth.dto.UserRegisterDTO;
import com.scripto.backend.user.dto.UserUpdateDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.token.TokenService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity getAllUsers() {
        var users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/")
    public ResponseEntity getUserById(@PathVariable Long id) {
        var user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/")
    @Transactional
    public ResponseEntity updateUserById(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUserById(id, userUpdateDTO);
        var user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register/")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        if (userService.findUserByEmail(userRegisterDTO.email()) != null) {
            return ResponseEntity.badRequest().build();
        }
        this.userService.registerUser(userRegisterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login/")
    public ResponseEntity<TokenJWTDTO> loginUser(@RequestBody @Valid LoginDTO loginDTO) {
        var token = this.userService.loginUser(loginDTO);
        return ResponseEntity.ok(new TokenJWTDTO(token));
    }

    @DeleteMapping("/me/")
    public ResponseEntity<Void> deleteOwnAccount(@AuthenticationPrincipal User user) {
        userService.softDeleteAccount(user.getId());
        return ResponseEntity.noContent().build();
    }
}