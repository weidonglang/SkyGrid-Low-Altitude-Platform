package com.lowaltitude.userorg.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.common.web.RequestHeaderNames;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserBootstrapController {
    @GetMapping("/bootstrap")
    public ApiResponse<Map<String, Object>> bootstrap(
            @RequestHeader(value = RequestHeaderNames.USER_ID, required = false) String userId,
            @RequestHeader(value = RequestHeaderNames.USER_NAME, required = false) String username,
            @RequestHeader(value = RequestHeaderNames.USER_ROLE, required = false) String role) {
        return ApiResponse.ok(Map.of(
                "service", "user-org-service",
                "phase", "01",
                "currentUserId", userId == null ? "direct-call" : userId,
                "currentUsername", username == null ? "direct-call" : username,
                "currentRole", role == null ? "direct-call" : role,
                "next", "Phase 02 will implement user, role, organization and approver configuration tables."
        ));
    }
}
