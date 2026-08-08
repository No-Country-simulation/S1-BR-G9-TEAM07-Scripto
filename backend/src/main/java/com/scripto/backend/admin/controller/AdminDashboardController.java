package com.scripto.backend.admin.controller;

import com.scripto.backend.admin.dto.AdminDashboardDTO;
import com.scripto.backend.admin.service.AdminService;
import com.scripto.backend.config.SecurityConfigurations;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Administração - Visão geral")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<AdminDashboardDTO> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }
}
