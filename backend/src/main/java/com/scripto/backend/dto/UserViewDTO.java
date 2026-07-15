package com.scripto.backend.dto;

import com.scripto.backend.user.entity.User;

public record UserViewDTO(String fullName, String email, String cpf) {
    public UserViewDTO(User user){
        this(user.getFullName(), user.getEmail(), user.getCpf());
    }
}
