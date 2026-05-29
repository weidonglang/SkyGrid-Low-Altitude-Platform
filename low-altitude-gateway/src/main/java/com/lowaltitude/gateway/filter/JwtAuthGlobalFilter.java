package com.lowaltitude.gateway.filter;

import com.lowaltitude.common.security.JwtPayload;
import com.lowaltitude.common.security.JwtTokenProvider;
import com.lowaltitude.common.web.RequestHeaderNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthGlobalFilter(@Value("${app.security.jwt-secret:low-altitude-dev-secret-change-me}") String secret,
                               @Value("${app.security.jwt-ttl-seconds:86400}") long ttlSeconds) {
        this.jwtTokenProvider = new JwtTokenProvider(secret, ttlSeconds);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS || isPublicPath(path)) {
            return chain.filter(exchange);
        }
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }
        try {
            JwtPayload payload = jwtTokenProvider.verify(authorization.substring("Bearer ".length()));
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(RequestHeaderNames.USER_ID, payload.subject())
                    .header(RequestHeaderNames.USER_NAME, payload.username())
                    .header(RequestHeaderNames.USER_ROLE, payload.role().name())
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception ex) {
            return unauthorized(exchange, ex.getMessage());
        }
    }

    @Override
    public int getOrder() { return -100; }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") || path.startsWith("/actuator") || path.startsWith("/internal/");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"success\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"" + escape(message) + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
