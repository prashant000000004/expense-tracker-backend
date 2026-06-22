package com.aiexpensetracker.expense_tracker.ai;


import com.aiexpensetracker.expense_tracker.ai.dto.InsightResponse;
import com.aiexpensetracker.expense_tracker.ai.dto.ReceiptData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    // ─── Receipt Scanning ─────────────────────────────────

    public ReceiptData scanReceipt(String base64Image, String mimeType) {
        String prompt = """
                Analyze this receipt image and extract the following information.
                Respond ONLY with a valid JSON object, no explanation, no markdown.
                
                {
                  "title": "merchant or store name",
                  "amount": 0.00,
                  "date": "YYYY-MM-DD",
                  "suggestedCategory": "one of: Food, Transport, Bills, Shopping, Health, Entertainment, Education, Other",
                  "notes": "brief description of purchase"
                }
                
                If you cannot read the receipt clearly, respond with:
                {"success": false, "errorMessage": "reason why"}
                """;

        String responseText = callGeminiWithImage(prompt, base64Image, mimeType);

        return parseReceiptResponse(responseText);
    }

    // ─── Spending Insights ────────────────────────────────

    public InsightResponse generateInsights(String spendingData) {
        String prompt = """
                You are a personal finance advisor analyzing spending data.
                Based on this expense data, provide insights and suggestions.
                
                Expense Data:
                """ + spendingData + """
                
                Respond ONLY with a valid JSON object:
                {
                  "summary": "one sentence summary of spending",
                  "insights": ["insight 1", "insight 2", "insight 3"],
                  "suggestions": ["suggestion 1", "suggestion 2"],
                  "savingsAdvice": "one personalized savings tip"
                }
                """;

        String responseText = callGeminiTextOnly(prompt);
        return parseInsightResponse(responseText);
    }

    // ─── Gemini API Calls ─────────────────────────────────

    private String callGeminiWithImage(
            String prompt, String base64Image, String mimeType) {
        Map<String, Object>  requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64Image
                                ))
                        ))
                )
        );
        return callGemini(requestBody);
    }

    private String callGeminiTextOnly(String prompt) {
        Map<String, Object>  requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );
        return callGemini(requestBody);
    }

    private String callGemini(Map<String, Object> requestBody) {
        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // extract text from Gemini response structure
            JsonNode root = objectMapper.readTree(response);
            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable");
        }
    }

    // ─── Response Parsers ─────────────────────────────────

    private ReceiptData parseReceiptResponse(String responseText) {
        try {
            // clean markdown code blocks if Gemini adds them
            String cleaned = responseText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode json = objectMapper.readTree(cleaned);

            // check if AI returned failure response
            if (json.has("success") &&
                    !json.get("success").asBoolean()) {
                return ReceiptData.builder()
                        .success(false)
                        .errorMessage(json.get("errorMessage").asText())
                        .build();
            }

            return ReceiptData.builder()
                    .title(json.get("title").asText())
                    .amount(new BigDecimal(json.get("amount").asText()))
                    .date(LocalDate.parse(json.get("date").asText()))
                    .suggestedCategory(json.get("suggestedCategory").asText())
                    .notes(json.get("notes").asText())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse receipt response: {}", e.getMessage());
            return ReceiptData.builder()
                    .success(false)
                    .errorMessage("Could not read receipt. Please enter manually.")
                    .build();
        }
    }

    private InsightResponse parseInsightResponse(String responseText) {
        try {
            String cleaned = responseText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode json = objectMapper.readTree(cleaned);

            List<String> insights = objectMapper.convertValue(
                    json.get("insights"),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, String.class));

            List<String> suggestions = objectMapper.convertValue(
                    json.get("suggestions"),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, String.class));

            return InsightResponse.builder()
                    .summary(json.get("summary").asText())
                    .insights(insights)
                    .suggestions(suggestions)
                    .savingsAdvice(json.get("savingsAdvice").asText())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse insight response: {}", e.getMessage());
            return InsightResponse.builder()
                    .summary("Unable to generate insights at this time.")
                    .insights(List.of())
                    .suggestions(List.of())
                    .savingsAdvice("")
                    .build();
        }
    }
}