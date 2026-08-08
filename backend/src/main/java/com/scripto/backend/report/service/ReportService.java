package com.scripto.backend.report.service;

import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.exception.ResourceNotFoundException;
import com.scripto.backend.report.domain.ReportStatus;
import com.scripto.backend.report.dto.ReportRequestDTO;
import com.scripto.backend.report.dto.ReportResponseDTO;
import com.scripto.backend.report.dto.ReportReviewDTO;
import com.scripto.backend.report.entity.DocumentReport;
import com.scripto.backend.report.repository.DocumentReportRepository;
import com.scripto.backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
    private final DocumentRepository documentRepository;
    private final DocumentReportRepository reportRepository;

    public ReportService(DocumentRepository documentRepository, DocumentReportRepository reportRepository) {
        this.documentRepository = documentRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public ReportResponseDTO report(Long documentId, ReportRequestDTO request, User reporter) {
        Document document = documentRepository
                .findByIdAndVisibilityAndModerationStatusAndStatus(
                        documentId, Visibility.PUBLIC, ModerationStatus.APPROVED, Status.PROCESSED
                )
                .orElseThrow(() -> new ResourceNotFoundException("Documento público não encontrado."));
        if (document.getUser().getId().equals(reporter.getId())) {
            throw new BusinessRuleException("Você não pode denunciar o próprio documento.");
        }
        if (reportRepository.existsByDocumentIdAndReporterId(documentId, reporter.getId())) {
            throw new BusinessRuleException("Você já denunciou este documento.");
        }
        DocumentReport report = new DocumentReport();
        report.setDocument(document);
        report.setReporter(reporter);
        report.setReason(request.reason());
        report.setDetails(request.details());
        report.setStatus(ReportStatus.OPEN);
        return toResponse(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> listOpen() {
        return reportRepository.findByStatusOrderByCreatedAtAsc(ReportStatus.OPEN).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReportResponseDTO review(Long reportId, ReportReviewDTO request, User admin) {
        DocumentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada."));
        if (report.getStatus() != ReportStatus.OPEN) {
            throw new BusinessRuleException("Esta denúncia já foi encerrada.");
        }
        if (request.status() == ReportStatus.OPEN) {
            throw new BusinessRuleException("A revisão deve encerrar a denúncia.");
        }
        report.setStatus(request.status());
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        if (request.status() == ReportStatus.ACTIONED) {
            report.getDocument().setModerationStatus(ModerationStatus.BLOCKED);
            documentRepository.save(report.getDocument());
        }
        return toResponse(reportRepository.save(report));
    }

    private ReportResponseDTO toResponse(DocumentReport report) {
        return new ReportResponseDTO(
                report.getId(),
                report.getDocument().getId(),
                report.getReporter() == null ? null : report.getReporter().getId(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }
}
