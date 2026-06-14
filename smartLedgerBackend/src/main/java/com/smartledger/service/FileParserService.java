package com.smartledger.service;

import com.smartledger.model.Transaction;
import com.smartledger.util.CsvParserUtil;
import com.smartledger.util.ExcelParserUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@Service
public class FileParserService {

    private final CsvParserUtil csvParserUtil;
    private final ExcelParserUtil excelParserUtil;

    public FileParserService(CsvParserUtil csvParserUtil, ExcelParserUtil excelParserUtil) {
        this.csvParserUtil = csvParserUtil;
        this.excelParserUtil = excelParserUtil;
    }

    public List<Transaction> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty CSV or XLSX file.");
        }

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.US);
        if (filename.endsWith(".csv")) {
            return csvParserUtil.parse(file);
        }
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return excelParserUtil.parse(file);
        }
        throw new IllegalArgumentException("Unsupported file type. Upload a .csv, .xlsx, or .xls file.");
    }
}
