package com.scripto.backend.admin.service;

import com.scripto.backend.admin.dto.*;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.exception.ResourceNotFoundException;
import com.scripto.backend.report.domain.ReportStatus;
import com.scripto.backend.report.repository.DocumentReportRepository;
import com.scripto.backend.user.entity.Role;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            DocumentRepository documentRepository,
            DocumentReportRepository reportRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO dashboard() {
        return new AdminDashboardDTO(
                userRepository.count(),
                userRepository.countByActiveFalse(),
                documentRepository.count(),
                reportRepository.countByStatus(ReportStatus.OPEN),
                reportRepository.countByStatusNot(ReportStatus.OPEN)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminUserDTO> users() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return List.of();
        }
        List<Long> ids = users.stream().map(User::getId).toList();
        Map<Long, Long> documents = toCountMap(documentRepository.countDocumentsGroupedByUserIds(ids));
        Map<Long, Long> reportsMade = toCountMap(reportRepository.countMadeGroupedByUserIds(ids));
        Map<Long, Long> reportsReceived = toCountMap(reportRepository.countReceivedGroupedByUserIds(ids));
        return users.stream()
                .map(user -> AdminUserDTO.from(
                        user,
                        documents.getOrDefault(user.getId(), 0L),
                        reportsMade.getOrDefault(user.getId(), 0L),
                        reportsReceived.getOrDefault(user.getId(), 0L)
                ))
                .toList();
    }

    @Transactional
    public AdminUserDTO updateUser(Long userId, AdminUserUpdateDTO request, User actor) {
        User user = requireUser(userId);

        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Este e-mail já está em uso por outra conta.");
        }

        if (request.role() != null && request.role() != user.getRole()) {
            preventRemovingLastAdmin(user, request.role());
            if (user.getId().equals(actor.getId()) && request.role() != Role.ADMIN) {
                throw new BusinessRuleException("O administrador autenticado não pode remover a própria permissão de ADMIN.");
            }
            user.setRole(request.role());
        }

        user.updateProfile(request.fullName(), request.email());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.updatePassword(passwordEncoder.encode(request.newPassword()));
        }
        userRepository.save(user);
        return toAdminUser(user);
    }

    @Transactional
    public AdminUserDTO setBanned(Long userId, boolean banned, User actor) {
        User user = requireUser(userId);
        if (user.getId().equals(actor.getId()) && banned) {
            throw new BusinessRuleException("O administrador autenticado não pode banir a própria conta.");
        }
        if (banned) {
            preventRemovingLastAdmin(user, Role.USER);
            user.ban();
        } else {
            user.unban();
        }
        userRepository.save(user);
        return toAdminUser(user);
    }

    @Transactional(readOnly = true)
    public List<AdminDocumentDTO> documents() {
        var documents = documentRepository.findAllForAdmin();
        if (documents.isEmpty()) {
            return List.of();
        }
        var ids = documents.stream().map(document -> document.getId()).toList();
        Map<Long, Long> reportCounts = toCountMap(reportRepository.countGroupedByDocumentIds(ids));
        return documents.stream()
                .map(document -> AdminDocumentDTO.from(document, reportCounts.getOrDefault(document.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminDocumentDetailDTO document(Long documentId) {
        var document = documentRepository.findDetailedById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado."));
        return AdminDocumentDetailDTO.from(document, reportRepository.countByDocumentId(documentId));
    }

    @Transactional
    public AdminDocumentDTO updateDocument(Long documentId, AdminDocumentUpdateDTO request) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado."));
        if (request.visibility() != null) {
            document.setVisibility(request.visibility());
        }
        if (request.blocked() != null) {
            document.setModerationStatus(Boolean.TRUE.equals(request.blocked())
                    ? ModerationStatus.BLOCKED
                    : ModerationStatus.APPROVED);
        }
        documentRepository.save(document);
        return AdminDocumentDTO.from(document, reportRepository.countByDocumentId(document.getId()));
    }

    @Transactional(readOnly = true)
    public List<AdminReportDTO> reports(ReportStatus status) {
        var reports = status == null
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByStatusOrderByCreatedAtAsc(status);
        return reports.stream().map(AdminReportDTO::from).toList();
    }

    private AdminUserDTO toAdminUser(User user) {
        return AdminUserDTO.from(
                user,
                documentRepository.countByUserId(user.getId()),
                reportRepository.countByReporterId(user.getId()),
                reportRepository.countReceivedByUserId(user.getId())
        );
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private void preventRemovingLastAdmin(User user, Role targetRole) {
        boolean removesAccessibleAdmin = user.getRole() == Role.ADMIN
                && Boolean.TRUE.equals(user.getActive())
                && !Boolean.TRUE.equals(user.getBanned())
                && targetRole != Role.ADMIN;
        if (removesAccessibleAdmin && userRepository.countByRoleAndActiveTrueAndBannedFalse(Role.ADMIN) <= 1) {
            throw new BusinessRuleException("Não é possível remover ou banir o último administrador ativo do sistema.");
        }
    }

    private Map<Long, Long> toCountMap(Collection<Object[]> rows) {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return result;
    }
}
