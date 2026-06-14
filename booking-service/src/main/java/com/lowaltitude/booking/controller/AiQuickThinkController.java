package com.lowaltitude.booking.controller;

import com.lowaltitude.booking.dto.AiQuickThinkRequest;
import com.lowaltitude.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings/ai")
public class AiQuickThinkController {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String ollamaGenerateUrl;
    private final String defaultModel;

    public AiQuickThinkController(@Value("${low-altitude.ai.ollama-url:http://127.0.0.1:11434/api/generate}") String ollamaGenerateUrl,
                                  @Value("${low-altitude.ai.default-model:qwen3:8b}") String defaultModel) {
        this.ollamaGenerateUrl = ollamaGenerateUrl;
        this.defaultModel = defaultModel;
    }

    @PostMapping(value = "/quick-think", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ApiResponse<Map<String, Object>> quickThink(@Valid @RequestBody AiQuickThinkRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.getModel() == null || request.getModel().isBlank() ? defaultModel : request.getModel());
        body.put("prompt", request.getPrompt());
        body.put("stream", false);
        body.put("think", Boolean.TRUE.equals(request.getThink()));
        body.put("options", Map.of("temperature", 0.2, "num_predict", 360));

        @SuppressWarnings("unchecked")
        Map<String, Object> ollama = restTemplate.postForObject(ollamaGenerateUrl, body, Map.class);
        String response = ollama == null ? "" : String.valueOf(ollama.getOrDefault("response", ""));
        String thinking = ollama == null ? "" : String.valueOf(ollama.getOrDefault("thinking", ""));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("model", body.get("model"));
        result.put("response", response.isBlank() ? thinking : response);
        result.put("thinking", thinking);
        result.put("source", "ollama");
        result.put("ollamaUrl", ollamaGenerateUrl);
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.ok(result);
    }
}
