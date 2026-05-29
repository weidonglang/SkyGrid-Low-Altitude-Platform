package com.lowaltitude.common.security;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lightweight HS256 JWT provider for Phase 01 development. */
public class JwtTokenProvider {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final Pattern SUB_PATTERN = Pattern.compile("\\\"sub\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern USER_PATTERN = Pattern.compile("\\\"username\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\\\"role\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern IAT_PATTERN = Pattern.compile("\\\"iat\\\"\\s*:\\s*(\\d+)");
    private static final Pattern EXP_PATTERN = Pattern.compile("\\\"exp\\\"\\s*:\\s*(\\d+)");

    private final String secret;
    private final long ttlSeconds;

    public JwtTokenProvider(String secret, long ttlSeconds) {
        if (secret == null || secret.length() < 16) {
            throw new IllegalArgumentException("JWT secret must contain at least 16 characters");
        }
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(String subject, String username, UserRole role) {
        long now = Instant.now().getEpochSecond();
        long exp = now + ttlSeconds;
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{"
                + "\"sub\":\"" + escape(subject) + "\","
                + "\"username\":\"" + escape(username) + "\","
                + "\"role\":\"" + role.name() + "\","
                + "\"iat\":" + now + ","
                + "\"exp\":" + exp
                + "}";
        String unsignedToken = base64Url(header) + "." + base64Url(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtPayload verify(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Missing token");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token format");
        }
        String unsigned = parts[0] + "." + parts[1];
        String expected = sign(unsigned);
        if (!constantTimeEquals(expected, parts[2])) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token signature");
        }
        String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        String sub = requireString(payloadJson, SUB_PATTERN, "sub");
        String username = requireString(payloadJson, USER_PATTERN, "username");
        UserRole role = UserRole.valueOf(requireString(payloadJson, ROLE_PATTERN, "role"));
        long iat = requireLong(payloadJson, IAT_PATTERN, "iat");
        long exp = requireLong(payloadJson, EXP_PATTERN, "exp");
        if (Instant.now().getEpochSecond() >= exp) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token expired");
        }
        return new JwtPayload(sub, username, role, iat, exp);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private static String base64Url(String text) {
        return URL_ENCODER.encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String requireString(String json, Pattern pattern, String field) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token missing field: " + field);
        }
        return matcher.group(1);
    }

    private static long requireLong(String json, Pattern pattern, String field) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token missing field: " + field);
        }
        return Long.parseLong(matcher.group(1));
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        int diff = a.length() ^ b.length();
        for (int i = 0; i < Math.min(a.length(), b.length()); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
