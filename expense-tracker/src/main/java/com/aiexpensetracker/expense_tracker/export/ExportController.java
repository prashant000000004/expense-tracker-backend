package com.aiexpensetracker.expense_tracker.export;

import com.aiexpensetracker.expense_tracker.expense.Expense;
import com.aiexpensetracker.expense_tracker.expense.ExpenseRepository;
import com.aiexpensetracker.expense_tracker.user.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {
    private final ExpenseRepository expenseRepository;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(defaultValue = "") String month) {
        try {
            User user = (User) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            YearMonth yearMonth = month.isEmpty()
                    ? YearMonth.now() : YearMonth.parse(month);

            LocalDate from = yearMonth.atDay(1);
            LocalDate to = yearMonth.atEndOfMonth();

            List<Expense> expenses = expenseRepository
                    .findByUserIdAndDateBetween(user.getId(), from, to);

            byte[] pdf = generatePdf(user, expenses, yearMonth);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=expenses-" + yearMonth + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF");
        }
    }

    private byte[] generatePdf(
            User user,
            List<Expense> expenses,
            YearMonth month) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        // ─── Title ────────────────────────────────────────
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20,
                Font.BOLD, BaseColor.BLACK);
        Paragraph title = new Paragraph(
                "Expense Report — " + month, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // ─── User Info ────────────────────────────────────
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 11);
        document.add(new Paragraph("Name: " + user.getName(), infoFont));
        document.add(new Paragraph("Email: " + user.getEmail(), infoFont));
        document.add(new Paragraph(
                "Generated: " + LocalDate.now(), infoFont));
        document.add(Chunk.NEWLINE);

        // ─── Table ────────────────────────────────────────
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f, 2f, 2f, 3f});

        // headers
        addTableHeader(table, "Title");
        addTableHeader(table, "Amount");
        addTableHeader(table, "Category");
        addTableHeader(table, "Date");
        addTableHeader(table, "Notes");

        // rows
        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : expenses) {
            table.addCell(e.getTitle());
            table.addCell("₹" + e.getAmount());
            table.addCell(e.getCategory().getName());
            table.addCell(e.getDate().toString());
            table.addCell(e.getNotes() != null ? e.getNotes() : "—");
            total = total.add(e.getAmount());
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        // ─── Total ────────────────────────────────────────
        Font totalFont = new Font(Font.FontFamily.HELVETICA,
                13, Font.BOLD);
        Paragraph totalPara = new Paragraph(
                "Total Spent: ₹" + total, totalFont);
        totalPara.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalPara);

        document.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String text) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA,
                11, Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new BaseColor(63, 81, 181));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
