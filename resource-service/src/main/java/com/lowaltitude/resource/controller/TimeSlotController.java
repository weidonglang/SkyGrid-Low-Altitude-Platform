package com.lowaltitude.resource.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.resource.domain.TimeSlot;
import com.lowaltitude.resource.dto.TimeSlotRequest;
import com.lowaltitude.resource.service.TimeSlotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources/time-slots")
public class TimeSlotController {
    private final TimeSlotService service;
    public TimeSlotController(TimeSlotService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<TimeSlot>> list(@RequestParam(name = "keyword", required = false) String keyword,
                                            @RequestParam(name = "enabled", required = false) Boolean enabled) {
        return ApiResponse.ok(service.list(keyword, enabled));
    }

    @GetMapping("/{id}")
    public ApiResponse<TimeSlot> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @PostMapping
    public ApiResponse<TimeSlot> create(@Valid @RequestBody TimeSlotRequest request) { return ApiResponse.ok("time slot created", service.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<TimeSlot> update(@PathVariable("id") Long id, @Valid @RequestBody TimeSlotRequest request) { return ApiResponse.ok("time slot updated", service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("time slot deleted", null);
    }
}
