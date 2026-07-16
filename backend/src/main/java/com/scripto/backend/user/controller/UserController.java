package com.scripto.backend.user.controller;

import com.scripto.backend.dto.UserUpdateDTO;
import com.scripto.backend.dto.UserViewDTO;
import com.scripto.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity getAllUsers(){
        var users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/{id}")
    public ResponseEntity getUserById(@PathVariable Long id){
        var user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }


    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity updateUserById(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO){
        userService.updateUserById(id, userUpdateDTO);
        var user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

}

