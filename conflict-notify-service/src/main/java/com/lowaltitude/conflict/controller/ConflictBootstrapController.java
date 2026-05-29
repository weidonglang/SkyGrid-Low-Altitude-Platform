package com.lowaltitude.conflict.controller;

import com.lowaltitude.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conflicts")
public class ConflictBootstrapController {
    @GetMapping("/bootstrap")
    public ApiResponse<Map<String, Object>> bootstrap() {
        return ApiResponse.ok(Map.of(
                "service", "conflict-notify-service",
                "phase", "06",
                "plannedRules", List.of(
                        "HardConflict: same Grid + same Level + overlapped TimeSlot",
                        "RiskConflict: adjacent Grid",
                        "RiskConflict: unsafe Level distance",
                        "Reject: no-fly Grid or no-fly TimeSlot"
                ),
                "messageChain", "RabbitMQ + outbox_message + idempotent_record + compensation task",
                "phase06Status", "RabbitMQ consumer + idempotent notification + audit log endpoints are implemented."
        ));
    }
}
