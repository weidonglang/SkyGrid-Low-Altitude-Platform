package com.lowaltitude.booking.controller;

import com.lowaltitude.booking.domain.OutboxMessage;
import com.lowaltitude.booking.service.OutboxService;
import com.lowaltitude.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings/outbox")
public class OutboxController {
    private final OutboxService service;

    public OutboxController(OutboxService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<OutboxMessage>> list(@RequestParam(name = "status", required = false) String status,
                                                 @RequestParam(name = "aggregateId", required = false) Long aggregateId,
                                                 @RequestParam(name = "eventType", required = false) String eventType,
                                                 @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return ApiResponse.ok(service.list(status, aggregateId, eventType, limit));
    }


    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(service.statusSummary());
    }

    @GetMapping("/{id}")
    public ApiResponse<OutboxMessage> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping("/dispatch")
    public ApiResponse<Map<String, Object>> dispatch(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        return ApiResponse.ok("outbox dispatch finished", service.dispatchReadyMessages(limit));
    }

    @PostMapping("/{id}/requeue")
    public ApiResponse<OutboxMessage> requeue(@PathVariable("id") Long id) {
        return ApiResponse.ok("outbox message requeued", service.requeue(id));
    }

    @PostMapping("/{id}/republish")
    public ApiResponse<OutboxMessage> republish(@PathVariable("id") Long id) {
        return ApiResponse.ok("outbox message republished for idempotency demo", service.republishForIdempotencyDemo(id));
    }
}
