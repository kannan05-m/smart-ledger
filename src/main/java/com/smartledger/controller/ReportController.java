package com.smartledger.controller;

import com.smartledger.model.AuditStatus;
import com.smartledger.service.AuditService;
import com.smartledger.service.ReportExportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final AuditService auditService;
    private final ReportExportService reportExportService;

    public ReportController(AuditService auditService, ReportExportService reportExportService) {
        this.auditService = auditService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/{sessionId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String sessionId) {
        return auditService.findReport(sessionId)
                .filter(report -> report.getStatus() == AuditStatus.READY)
                .map(report -> {
                    byte[] pdf = reportExportService.exportPdf(report);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                                    .filename("smartledger-audit-%s.pdf".formatted(sessionId))
                                    .build()
                                    .toString())
                            .body(pdf);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
