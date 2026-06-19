package com.lowaltitude.booking.controller;

import com.lowaltitude.booking.service.DashboardService;
import com.lowaltitude.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.ok(service.overview());
    }

    @GetMapping("/occupancy")
    public ApiResponse<Map<String, Object>> occupancy() {
        return ApiResponse.ok(service.occupancy());
    }

    @GetMapping("/conflicts")
    public ApiResponse<Map<String, Object>> conflicts() {
        return ApiResponse.ok(service.conflicts());
    }

    @GetMapping("/message-health")
    public ApiResponse<Map<String, Object>> messageHealth() {
        return ApiResponse.ok(service.messageHealth());
    }

    @GetMapping("/audit-summary")
    public ApiResponse<Map<String, Object>> auditSummary() {
        return ApiResponse.ok(service.auditSummary());
    }

    @GetMapping("/service-health")
    public ApiResponse<Map<String, Object>> serviceHealth() {
        return ApiResponse.ok(service.serviceHealth());
    }
}
