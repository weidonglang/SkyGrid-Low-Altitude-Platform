package com.lowaltitude.userorg.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.userorg.domain.ApproverConfig;
import com.lowaltitude.userorg.dto.ApproverConfigRequest;
import com.lowaltitude.userorg.service.ApproverConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-org/approver-configs")
public class ApproverConfigController {
    private final ApproverConfigService service;

    public ApproverConfigController(ApproverConfigService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<ApproverConfig>> list(@RequestParam(name = "orgId", required = false) Long orgId) { return ApiResponse.ok(service.list(orgId)); }

    @GetMapping("/{id}")
    public ApiResponse<ApproverConfig> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @PostMapping
    public ApiResponse<ApproverConfig> create(@Valid @RequestBody ApproverConfigRequest request) { return ApiResponse.ok("approver config created", service.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<ApproverConfig> update(@PathVariable("id") Long id, @Valid @RequestBody ApproverConfigRequest request) { return ApiResponse.ok("approver config updated", service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("approver config deleted", null);
    }
}
