package com.scripto.backend.user.entity;

import com.scripto.backend.document.entity.Document;
import com.scripto.backend.dto.UserUpdateDTO;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode(of = "id")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 150, nullable = false)
    private String fullName;

    @Column(name = "cpf", length = 11, nullable = false, unique = true, columnDefinition = "CHAR(11)")
    private String cpf;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @OneToMany(mappedBy = "user")
    private List<Document> documents;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;


    public void update(@Valid UserUpdateDTO userUpdateDTO){
        this.fullName = userUpdateDTO.fullName();
        this.email = userUpdateDTO.email();
        this.passwordHash = userUpdateDTO.passwordHash();
    }


}