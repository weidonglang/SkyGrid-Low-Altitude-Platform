package com.lowaltitude.booking.service;

import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.domain.ResourceOccupancy;
import com.lowaltitude.booking.mapper.BookingMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final BookingMapper bookingMapper;
    private final OutboxService outboxService;

    public DashboardService(BookingMapper bookingMapper, OutboxService outboxService) {
        this.bookingMapper = bookingMapper;
        this.outboxService = outboxService;
    }

    public Map<String, Object> overview() {
        LocalDate today = LocalDate.now();
        int totalOccupancy = bookingMapper.countOccupancyAll();
        int activeOccupancy = bookingMapper.countOccupancyByStatus(BookingService.OCCUPIED);
        Map<String, Object> outbox = outboxService.statusSummary();
        int outboxTotal = intValue(outbox.get("total"));
        int outboxSent = intValue(outbox.get("sent"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayTaskCount", bookingMapper.countBookingByDate(today));
        result.put("pendingApprovalCount", bookingMapper.countBookingByStatus(BookingService.STATUS_PENDING));
        result.put("approvedTaskCount", bookingMapper.countBookingByStatus(BookingService.STATUS_APPROVED));
        result.put("hardConflictCount", bookingMapper.countConflictByStatusAndType("ACTIVE", "HARD"));
        result.put("riskConflictCount", bookingMapper.countConflictByStatusAndType("ACTIVE", "RISK"));
        result.put("resourceOccupancyRate", totalOccupancy == 0 ? 0.0 : round((double) activeOccupancy / totalOccupancy));
        result.put("outboxPendingCount", intValue(outbox.get("pending")));
        result.put("outboxRetryingCount", intValue(outbox.get("retrying")));
        result.put("outboxFailedCount", intValue(outbox.get("failed")));
        result.put("notificationSuccessRate", outboxTotal == 0 ? 0.0 : round((double) outboxSent / outboxTotal));
        result.put("auditLogCount", outboxSent);
        return result;
    }

    public Map<String, Object> occupancy() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeCount", bookingMapper.countOccupancyByStatus(BookingService.OCCUPIED));
        result.put("releasedCount", bookingMapper.countOccupancyByStatus("RELEASED"));
        result.put("recent", bookingMapper.listRecentOccupancies(100));
        return result;
    }

    public Map<String, Object> conflicts() {
        List<ConflictRecord> active = bookingMapper.listConflicts(null, null, "ACTIVE");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeCount", active.size());
        result.put("hardConflictCount", active.stream().filter(item -> "HARD".equalsIgnoreCase(item.getConflictType())).count());
        result.put("riskConflictCount", active.stream().filter(item -> "RISK".equalsIgnoreCase(item.getConflictType())).count());
        result.put("items", active);
        return result;
    }

    public Map<String, Object> messageHealth() {
        return outboxService.statusSummary();
    }

    public Map<String, Object> auditSummary() {
        Map<String, Object> outbox = outboxService.statusSummary();
        return Map.of(
                "auditSource", "outbox-dispatch",
                "sentEventCount", intValue(outbox.get("sent")),
                "pendingEventCount", intValue(outbox.get("pending")),
                "failedEventCount", intValue(outbox.get("failed"))
        );
    }

    public Map<String, Object> serviceHealth() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gateway", "CHECK_GATEWAY_ACTUATOR");
        result.put("bookingService", "UP");
        result.put("resourceService", "CHECK_RESOURCE_ACTUATOR");
        result.put("notifyService", "CHECK_NOTIFY_ACTUATOR");
        result.put("rabbitmq", "CHECK_RABBITMQ_PORT");
        result.put("redis", "CHECK_REDIS_PORT");
        result.put("nacos", "CHECK_NACOS_PORT");
        return result;
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
