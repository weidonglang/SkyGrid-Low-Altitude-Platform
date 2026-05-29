package com.lowaltitude.resource.controller;

import com.lowaltitude.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceBootstrapController {
    @GetMapping("/bootstrap")
    public ApiResponse<Map<String, Object>> bootstrap() {
        return ApiResponse.ok(Map.of(
                "service", "resource-service",
                "phase", "01",
                "domainModel", List.of("Grid", "Level", "TimeSlot", "RouteTemplate"),
                "coreUnit", "GridId + LevelId + TimeSlotId",
                "next", "Phase 02 will implement CRUD APIs for low-altitude resource master data."
        ));
    }
}
