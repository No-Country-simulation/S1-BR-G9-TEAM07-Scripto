package com.scripto.backend.user.controller;

import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UserDeletController {

    private final UserService userService;

    public UserDeletController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteOwnAccount(@AuthenticationPrincipal User user) {
        userService.softDeleteAccount(user.getId());
        return ResponseEntity.noContent().build();
    }
}