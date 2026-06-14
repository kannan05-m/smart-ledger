package com.smartledger.controller;

import com.smartledger.dto.UploadResponseDto;
import com.smartledger.model.AuditReport;
import com.smartledger.model.Transaction;
import com.smartledger.service.AuditService;
import com.smartledger.service.FileParserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final FileParserService fileParserService;
    private final AuditService auditService;

    public UploadController(FileParserService fileParserService, AuditService auditService) {
        this.fileParserService = fileParserService;
        this.auditService = auditService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponseDto upload(@RequestParam("file") MultipartFile file) {
        List<Transaction> transactions = fileParserService.parse(file);
        AuditReport report = auditService.startAudit(transactions);
        return new UploadResponseDto(
                report.getSessionId(),
                report.getStatus().name(),
                transactions.size(),
                0,
                "Upload parsed. Audit processing has started."
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
