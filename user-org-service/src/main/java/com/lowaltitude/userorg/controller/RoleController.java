package com.lowaltitude.userorg.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.userorg.domain.Role;
import com.lowaltitude.userorg.dto.RoleRequest;
import com.lowaltitude.userorg.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-org/roles")
public class RoleController {
    private final RoleService service;

    public RoleController(RoleService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<Role>> list(@RequestParam(name = "keyword", required = false) String keyword) { return ApiResponse.ok(service.list(keyword)); }

    @GetMapping("/{id}")
    public ApiResponse<Role> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @PostMapping
    public ApiResponse<Role> create(@Valid @RequestBody RoleRequest request) { return ApiResponse.ok("role created", service.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<Role> update(@PathVariable("id") Long id, @Valid @RequestBody RoleRequest request) { return ApiResponse.ok("role updated", service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("role deleted", null);
    }
}
