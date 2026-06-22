package com.aiexpensetracker.expense_tracker.expense;

import com.aiexpensetracker.expense_tracker.ai.GeminiService;
import com.aiexpensetracker.expense_tracker.ai.dto.ReceiptData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseAiController {
    private final GeminiService geminiService;

    // Flutter sends image as multipart file
    // We convert to base64 and send to Gemini
    @PostMapping("/scan-receipt")
    public ResponseEntity<ReceiptData> scanReceipt(
            @RequestParam("image") MultipartFile image) {
        try {
            // convert image to base64
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = image.getContentType();

            ReceiptData result = geminiService.scanReceipt(base64Image, mimeType);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.ok(
                    ReceiptData.builder()
                            .success(false)
                            .errorMessage("Failed to process image")
                            .build()
            );
        }
    }
}
