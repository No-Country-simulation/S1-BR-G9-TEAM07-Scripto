package com.scripto.backend.user.dto;

import com.scripto.backend.user.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserViewDTO(Long id, String fullName, String email, String cpf) {
    public UserViewDTO(User user) {
        this(user.getId(), user.getFullName(), user.getEmail(), user.getCpf());
    }

    public UserViewDTO(Long id, String fullName, String email, String cpf) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.cpf = cpf;
    }
}