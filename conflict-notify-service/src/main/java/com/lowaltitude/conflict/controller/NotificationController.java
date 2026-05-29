package com.lowaltitude.conflict.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.conflict.domain.AuditLog;
import com.lowaltitude.conflict.domain.IdempotentRecord;
import com.lowaltitude.conflict.domain.NotifyRecord;
import com.lowaltitude.conflict.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }


    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(service.summary());
    }

    @GetMapping("/records")
    public ApiResponse<List<NotifyRecord>> records(@RequestParam(name = "bookingNo", required = false) String bookingNo,
                                                   @RequestParam(name = "messageKey", required = false) String messageKey,
                                                   @RequestParam(name = "eventType", required = false) String eventType,
                                                   @RequestParam(name = "status", required = false) String status,
                                                   @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return ApiResponse.ok(service.listNotify(bookingNo, messageKey, eventType, status, limit));
    }

    @GetMapping("/audits")
    public ApiResponse<List<AuditLog>> audits(@RequestParam(name = "bookingNo", required = false) String bookingNo,
                                              @RequestParam(name = "messageKey", required = false) String messageKey,
                                              @RequestParam(name = "eventType", required = false) String eventType,
                                              @RequestParam(name = "action", required = false) String action,
                                              @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return ApiResponse.ok(service.listAudit(bookingNo, messageKey, eventType, action, limit));
    }

    @GetMapping("/idempotent")
    public ApiResponse<List<IdempotentRecord>> idempotent(@RequestParam(name = "messageKey", required = false) String messageKey,
                                                          @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return ApiResponse.ok(service.listIdempotent(messageKey, limit));
    }
}
