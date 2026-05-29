package com.lowaltitude.userorg.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.userorg.domain.Organization;
import com.lowaltitude.userorg.dto.OrganizationRequest;
import com.lowaltitude.userorg.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-org/organizations")
public class OrganizationController {
    private final OrganizationService service;

    public OrganizationController(OrganizationService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<Organization>> list(@RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(service.list(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<Organization> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @PostMapping
    public ApiResponse<Organization> create(@Valid @RequestBody OrganizationRequest request) {
        return ApiResponse.ok("organization created", service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Organization> update(@PathVariable("id") Long id, @Valid @RequestBody OrganizationRequest request) {
        return ApiResponse.ok("organization updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("organization deleted", null);
    }
}
