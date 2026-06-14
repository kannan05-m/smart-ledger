package com.smartledger.util;

import com.smartledger.model.Transaction;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ExcelParserUtil {

    public List<Transaction> parse(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() == 0) {
                return List.of();
            }

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> header = CsvParserUtil.headerIndex(toValues(headerRow, formatter, evaluator));
            List<Transaction> transactions = new ArrayList<>();
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String[] values = toValues(row, formatter, evaluator);
                if (!isBlankRow(values)) {
                    transactions.add(CsvParserUtil.parseRow(values, header, i));
                }
            }
            return transactions;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse Excel file: " + ex.getMessage(), ex);
        }
    }

    private String[] toValues(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        int lastCell = Math.max(row.getLastCellNum(), 0);
        String[] values = new String[lastCell];
        for (int i = 0; i < lastCell; i++) {
            values[i] = formatter.formatCellValue(row.getCell(i), evaluator);
        }
        return values;
    }

    private boolean isBlankRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
