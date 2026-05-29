package com.lowaltitude.userorg.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.common.security.JwtTokenProvider;
import com.lowaltitude.common.security.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(@Value("${app.security.jwt-secret:low-altitude-dev-secret-change-me}") String secret,
                          @Value("${app.security.jwt-ttl-seconds:86400}") long ttlSeconds) {
        this.jwtTokenProvider = new JwtTokenProvider(secret, ttlSeconds);
    }

    /** Phase 01 development endpoint. Phase 02 should replace this with real username/password login. */
    @PostMapping("/dev-token")
    public ApiResponse<Map<String, Object>> devToken(@RequestParam(name = "username", defaultValue = "demo") String username,
                                                     @RequestParam(name = "role", defaultValue = "APPLICANT") UserRole role) {
        String userId = UUID.nameUUIDFromBytes((username + ":" + role.name()).getBytes()).toString();
        String token = jwtTokenProvider.issue(userId, username, role);
        return ApiResponse.ok(Map.of(
                "tokenType", "Bearer",
                "accessToken", token,
                "username", username,
                "role", role.name()
        ));
    }
}
