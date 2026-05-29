package com.lowaltitude.booking.controller;

import com.lowaltitude.booking.mapper.BookingMapper;
import com.lowaltitude.booking.service.OutboxService;
import com.lowaltitude.common.api.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/bookings/governance")
public class GovernanceDemoController {
    private final BookingMapper bookingMapper;
    private final OutboxService outboxService;
    private final Counter rateLimitedCounter;
    private final Counter circuitFallbackCounter;
    private final Counter retrySuccessCounter;
    private final Map<String, AtomicInteger> retryAttempts = new ConcurrentHashMap<>();

    public GovernanceDemoController(BookingMapper bookingMapper,
                                    OutboxService outboxService,
                                    MeterRegistry meterRegistry) {
        this.bookingMapper = bookingMapper;
        this.outboxService = outboxService;
        this.rateLimitedCounter = Counter.builder("low_altitude_governance_rate_limited_total")
                .description("Number of demo requests rejected by Resilience4j RateLimiter")
                .register(meterRegistry);
        this.circuitFallbackCounter = Counter.builder("low_altitude_governance_circuit_fallback_total")
                .description("Number of demo requests handled by CircuitBreaker fallback")
                .register(meterRegistry);
        this.retrySuccessCounter = Counter.builder("low_altitude_governance_retry_success_total")
                .description("Number of demo requests that succeeded after Retry attempts")
                .register(meterRegistry);
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bookingPending", bookingMapper.countBookingByStatus("PENDING"));
        result.put("bookingApproved", bookingMapper.countBookingByStatus("APPROVED"));
        result.put("bookingCancelled", bookingMapper.countBookingByStatus("CANCELLED"));
        result.put("occupancyOccupied", bookingMapper.countOccupancyByStatus("OCCUPIED"));
        result.put("occupancyReleased", bookingMapper.countOccupancyByStatus("RELEASED"));
        result.put("activeConflicts", bookingMapper.countConflictByStatus("ACTIVE"));
        result.put("resolvedConflicts", bookingMapper.countConflictByStatus("RESOLVED"));
        result.put("outbox", outboxService.statusSummary());
        result.put("rateLimiterName", "hotGridPreCheck");
        result.put("circuitBreakerName", "notifyProbe");
        result.put("retryName", "remoteProbe");
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.ok(result);
    }

    @GetMapping("/rate-limited")
    @RateLimiter(name = "hotGridPreCheck", fallbackMethod = "rateLimitedFallback")
    public ApiResponse<Map<String, Object>> rateLimited(@RequestParam(name = "label", defaultValue = "hot-grid") String label) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("label", label);
        result.put("result", "PASSED");
        result.put("message", "RateLimiter allowed this request.");
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.ok(result);
    }

    public ApiResponse<Map<String, Object>> rateLimitedFallback(String label, RequestNotPermitted ex) {
        rateLimitedCounter.increment();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("label", label);
        result.put("result", "RATE_LIMITED");
        result.put("message", "RateLimiter rejected this request. This is expected during Phase 07 pressure checks.");
        result.put("exception", ex.getClass().getSimpleName());
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.fail("RATE_LIMITED", result.toString());
    }

    @GetMapping("/circuit")
    @CircuitBreaker(name = "notifyProbe", fallbackMethod = "circuitFallback")
    public ApiResponse<Map<String, Object>> circuit(@RequestParam(name = "fail", defaultValue = "false") boolean fail) {
        if (fail) {
            throw new IllegalStateException("Simulated downstream notification service failure");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", "UPSTREAM_OK");
        result.put("message", "CircuitBreaker protected call succeeded.");
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.ok(result);
    }

    public ApiResponse<Map<String, Object>> circuitFallback(boolean fail, Throwable ex) {
        circuitFallbackCounter.increment();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", "DEGRADED");
        result.put("fail", fail);
        result.put("message", "CircuitBreaker fallback returned a safe degraded response.");
        result.put("exception", ex.getClass().getSimpleName());
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.ok("degraded by circuit breaker", result);
    }

    @GetMapping("/retry")
    @Retry(name = "remoteProbe", fallbackMethod = "retryFallback")
    public ApiResponse<Map<String, Object>> retry(@RequestParam(name = "key", defaultValue = "default") String key,
                                                  @RequestParam(name = "failTimes", defaultValue = "2") int failTimes) {
        AtomicInteger counter = retryAttempts.computeIfAbsent(key, ignored -> new AtomicInteger(0));
        int attempt = counter.incrementAndGet();
        if (attempt <= failTimes) {
            throw new IllegalStateException("Simulated transient failure, attempt=" + attempt);
        }
        retryAttempts.remove(key);
        retrySuccessCounter.increment();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", "RETRY_SUCCESS");
        result.put("key", key);
        result.put("attempt", attempt);
        result.put("failTimes", failTimes);
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.ok(result);
    }

    public ApiResponse<Map<String, Object>> retryFallback(String key, int failTimes, Throwable ex) {
        retryAttempts.remove(key);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", "RETRY_FALLBACK");
        result.put("key", key);
        result.put("failTimes", failTimes);
        result.put("message", "Retry exhausted and fallback returned a controlled response.");
        result.put("exception", ex.getClass().getSimpleName());
        result.put("time", OffsetDateTime.now().toString());
        return ApiResponse.ok("retry fallback", result);
    }
}
