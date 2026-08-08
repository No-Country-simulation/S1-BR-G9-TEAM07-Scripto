package com.scripto.backend.admin.controller;

import com.scripto.backend.admin.dto.AdminBanDTO;
import com.scripto.backend.admin.dto.AdminUserDTO;
import com.scripto.backend.admin.dto.AdminUserUpdateDTO;
import com.scripto.backend.admin.service.AdminService;
import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administração - Usuários")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> list() {
        return ResponseEntity.ok(adminService.users());
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> update(
            @PathVariable Long userId,
            @RequestBody @Valid AdminUserUpdateDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User admin
    ) {
        return ResponseEntity.ok(adminService.updateUser(userId, request, admin));
    }

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<AdminUserDTO> ban(
            @PathVariable Long userId,
            @RequestBody @Valid AdminBanDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User admin
    ) {
        return ResponseEntity.ok(adminService.setBanned(userId, request.banned(), admin));
    }
}
