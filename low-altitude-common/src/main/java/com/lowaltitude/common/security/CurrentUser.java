package com.lowaltitude.common.security;

public record CurrentUser(String userId, String username, UserRole role) {
    public static CurrentUser anonymous() {
        return new CurrentUser("anonymous", "anonymous", UserRole.APPLICANT);
    }
}
