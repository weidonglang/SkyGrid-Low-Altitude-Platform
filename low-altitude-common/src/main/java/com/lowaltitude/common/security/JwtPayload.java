package com.lowaltitude.common.security;

public record JwtPayload(String subject, String username, UserRole role, long issuedAtEpochSeconds, long expiresAtEpochSeconds) {
}
