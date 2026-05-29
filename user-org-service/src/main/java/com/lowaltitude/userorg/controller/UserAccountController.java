package com.lowaltitude.userorg.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.userorg.domain.Role;
import com.lowaltitude.userorg.domain.UserAccount;
import com.lowaltitude.userorg.dto.UserAccountRequest;
import com.lowaltitude.userorg.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-org/users")
public class UserAccountController {
    private final UserAccountService service;

    public UserAccountController(UserAccountService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<UserAccount>> list(@RequestParam(name = "keyword", required = false) String keyword) { return ApiResponse.ok(service.list(keyword)); }

    @GetMapping("/{id}")
    public ApiResponse<UserAccount> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable("id") Long id) { return ApiResponse.ok(service.detail(id)); }

    @PostMapping
    public ApiResponse<UserAccount> create(@Valid @RequestBody UserAccountRequest request) { return ApiResponse.ok("user created", service.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<UserAccount> update(@PathVariable("id") Long id, @Valid @RequestBody UserAccountRequest request) { return ApiResponse.ok("user updated", service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("user deleted", null);
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ApiResponse<List<Role>> assignRole(@PathVariable("userId") Long userId, @PathVariable("roleId") Long roleId) {
        return ApiResponse.ok("role assigned", service.assignRole(userId, roleId));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<List<Role>> removeRole(@PathVariable("userId") Long userId, @PathVariable("roleId") Long roleId) {
        return ApiResponse.ok("role removed", service.removeRole(userId, roleId));
    }
}
