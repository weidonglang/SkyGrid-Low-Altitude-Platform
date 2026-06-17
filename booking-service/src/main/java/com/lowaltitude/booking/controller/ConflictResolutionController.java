package com.lowaltitude.booking.controller;

import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.service.ConflictSuggestionService;
import com.lowaltitude.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/conflicts")
public class ConflictResolutionController {

    private final ConflictSuggestionService service;

    public ConflictResolutionController(ConflictSuggestionService service) {
        this.service = service;
    }

    @PostMapping("/resolve-suggestions")
    public ApiResponse<Map<String, Object>> suggestions(@RequestParam(name = "bookingId", required = false) Long bookingId) {
        return ApiResponse.ok(service.suggestions(bookingId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConflictRecord> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @GetMapping("/by-booking/{bookingId}")
    public ApiResponse<Map<String, Object>> byBooking(@PathVariable("bookingId") Long bookingId) {
        return ApiResponse.ok(service.suggestions(bookingId));
    }
}
