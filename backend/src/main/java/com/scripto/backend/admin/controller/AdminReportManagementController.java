package com.scripto.backend.admin.controller;

import com.scripto.backend.admin.dto.AdminReportDTO;
import com.scripto.backend.admin.service.AdminService;
import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.report.domain.ReportStatus;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Administração - Denúncias")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/reports/details")
public class AdminReportManagementController {
    private final AdminService adminService;

    public AdminReportManagementController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminReportDTO>> list(
            @RequestParam(required = false) ReportStatus status
    ) {
        return ResponseEntity.ok(adminService.reports(status));
    }
}
