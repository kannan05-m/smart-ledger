package com.smartledger.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.smartledger.model.AnomalyFlag;
import com.smartledger.model.AuditReport;
import com.smartledger.model.CategorizedTransaction;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ReportExportService {

    public byte[] exportPdf(AuditReport report) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
        try (Document document = new Document(pdfDocument)) {
            document.add(new Paragraph("SmartLedger Audit Report").setBold().setFontSize(18));
            document.add(new Paragraph("Session: " + report.getSessionId()));
            document.add(new Paragraph("Summary").setBold());
            if (report.getSummary() != null) {
                document.add(new Paragraph(report.getSummary().getHeadline()));
                document.add(new Paragraph("Findings").setBold());
                report.getSummary().getTopFindings().forEach(finding -> document.add(new Paragraph("- " + finding)));
                document.add(new Paragraph("Savings Suggestions").setBold());
                report.getSummary().getSavingsSuggestions().forEach(suggestion -> document.add(new Paragraph("- " + suggestion)));
                document.add(new Paragraph(report.getSummary().getAnomalySummary()));
                document.add(new Paragraph("Score: %d/100 - %s".formatted(report.getSummary().getOverallScore(), report.getSummary().getScoreReason())));
            }
            document.add(new Paragraph("Totals").setBold());
            document.add(new Paragraph("Credits: %s | Debits: %s | Net: %s"
                    .formatted(report.getTotalCredits(), report.getTotalDebits(), report.getNetAmount())));

            document.add(new Paragraph("Anomalies").setBold());
            Table anomalyTable = new Table(UnitValue.createPercentArray(new float[]{20, 15, 20, 45})).useAllAvailableWidth();
            addHeader(anomalyTable, "Type", "Severity", "Txn ID", "Description");
            for (AnomalyFlag flag : report.getAnomalies()) {
                anomalyTable.addCell(flag.getType());
                anomalyTable.addCell(flag.getSeverity().name());
                anomalyTable.addCell(flag.getTxnId());
                anomalyTable.addCell(flag.getDescription());
            }
            document.add(anomalyTable);

            document.add(new Paragraph("Transactions").setBold());
            Table transactionTable = new Table(UnitValue.createPercentArray(new float[]{16, 34, 14, 10, 16, 10})).useAllAvailableWidth();
            addHeader(transactionTable, "Date", "Description", "Amount", "Type", "Category", "Flagged");
            for (CategorizedTransaction transaction : report.getTransactions()) {
                transactionTable.addCell(String.valueOf(transaction.getDate()));
                transactionTable.addCell(transaction.getDescription());
                transactionTable.addCell(String.valueOf(transaction.getAmount()));
                transactionTable.addCell(transaction.getType().name());
                transactionTable.addCell(transaction.getCategory());
                transactionTable.addCell(transaction.isFlagged() ? "Yes" : "No");
            }
            document.add(transactionTable);
        }
        return outputStream.toByteArray();
    }

    private void addHeader(Table table, String... labels) {
        for (String label : labels) {
            table.addHeaderCell(new Cell().add(new Paragraph(label).setBold()));
        }
    }
}
