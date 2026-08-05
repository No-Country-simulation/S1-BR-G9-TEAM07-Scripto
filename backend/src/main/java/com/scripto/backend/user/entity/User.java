package com.scripto.backend.user.entity;

import com.scripto.backend.document.entity.Document;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "full_name", length = 150, nullable = false)
    private String fullName;

    @Column(name = "cpf", length = 11, nullable = false, unique = true, columnDefinition = "CHAR(11)")
    private String cpf;

    @Setter
    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Setter
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Setter
    @Column(nullable = false)
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private Role role = Role.USER;

    @Setter
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Setter
    @OneToMany(mappedBy = "user")
    private List<Document> documents;

    @Setter
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Setter
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public User(@NotBlank @Size(min = 3, max = 150) String fullName, @NotBlank @Email @Size(max = 255) String email, @NotBlank @Pattern(regexp = "\\d{11}", message = "\n" + "The CPF must contain exactly 11 numeric digits.") String cpf, String encryptedPassword) {
        this.fullName = fullName;
        this.email = email;
        this.cpf = cpf;
        this.passwordHash = encryptedPassword;
    }

    public void updateProfile(String fullName, String email) {
        if (fullName != null) {
            this.fullName = fullName.trim();
        }
        if (email != null) {
            this.email = email.trim().toLowerCase();
        }
    }

    public void updatePassword(String encryptedPassword) {
        this.passwordHash = encryptedPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }

    public void deactivate() {
        this.active = false;
        this.deletedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.active = true;
        this.deletedAt = null;
        this.failedLoginAttempts = 0;
    }

    public boolean canBeReactivated() {
        if (deletedAt == null) {
            return false;
        }
        return deletedAt.plusDays(30).isAfter(LocalDateTime.now());
    }

    public void registerSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }
}