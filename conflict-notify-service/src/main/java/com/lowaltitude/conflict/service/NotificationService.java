package com.lowaltitude.conflict.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowaltitude.conflict.domain.AuditLog;
import com.lowaltitude.conflict.domain.IdempotentRecord;
import com.lowaltitude.conflict.domain.NotifyRecord;
import com.lowaltitude.conflict.mapper.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String CONSUMER_NAME = "conflict-notify-service";

    private final NotificationMapper mapper;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void consumeBookingEvent(String body, String routingKey, String messageId) {
        try {
            Map<String, Object> payload = objectMapper.readValue(body, new TypeReference<>() {});
            String messageKey = string(payload.get("messageKey"));
            String eventType = string(payload.get("eventType"));
            Long bookingId = longValue(payload.get("bookingId"));
            String bookingNo = string(payload.get("bookingNo"));
            if (messageKey == null || eventType == null || bookingNo == null) {
                throw new IllegalArgumentException("Invalid booking event payload, messageKey/eventType/bookingNo required");
            }

            int inserted = mapper.insertIdempotent(messageKey, eventType, CONSUMER_NAME, "PROCESSED");
            if (inserted == 0) {
                insertAudit(messageKey, eventType, bookingId, bookingNo, "DUPLICATE_SKIPPED",
                        "Duplicate message skipped. routingKey=" + routingKey + ", messageId=" + messageId);
                log.info("Duplicate booking event skipped. messageKey={}", messageKey);
                return;
            }

            NotifyRecord record = new NotifyRecord();
            record.setMessageKey(messageKey);
            record.setEventType(eventType);
            record.setBookingId(bookingId);
            record.setBookingNo(bookingNo);
            record.setRecipientType("BOOKING_APPLICANT_AND_APPROVER");
            record.setRecipient(string(payload.get("applicantName")) == null ? "demo" : string(payload.get("applicantName")) + ",admin");
            record.setChannel("IN_APP");
            record.setSubject(subject(eventType, bookingNo));
            record.setContent(content(eventType, payload));
            record.setStatus("SENT");
            record.setSentAt(LocalDateTime.now());
            mapper.insertNotify(record);

            insertAudit(messageKey, eventType, bookingId, bookingNo, "NOTIFY_SENT",
                    "Notification record created from RabbitMQ. routingKey=" + routingKey + ", messageId=" + messageId);
            log.info("Booking event consumed. messageKey={}, eventType={}, bookingNo={}", messageKey, eventType, bookingNo);
        } catch (Exception ex) {
            log.error("Failed to consume booking event. messageId={}, body={}", messageId, body, ex);
            throw new IllegalStateException("Failed to consume booking event: " + ex.getMessage(), ex);
        }
    }

    public List<NotifyRecord> listNotify(String bookingNo, String messageKey, String eventType, String status, int limit) {
        return mapper.listNotify(blankToNull(bookingNo), blankToNull(messageKey), blankToNull(eventType), blankToNull(status), safeLimit(limit));
    }

    public List<AuditLog> listAudit(String bookingNo, String messageKey, String eventType, String action, int limit) {
        return mapper.listAudit(blankToNull(bookingNo), blankToNull(messageKey), blankToNull(eventType), blankToNull(action), safeLimit(limit));
    }

    public List<IdempotentRecord> listIdempotent(String messageKey, int limit) {
        return mapper.listIdempotent(blankToNull(messageKey), safeLimit(limit));
    }


    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notifySent", mapper.countNotifyByStatus("SENT"));
        result.put("auditTotal", mapper.countAuditAll());
        result.put("idempotentProcessed", mapper.countIdempotentByStatus("PROCESSED"));
        return result;
    }

    private void insertAudit(String messageKey, String eventType, Long bookingId, String bookingNo, String action, String detail) {
        AuditLog audit = new AuditLog();
        audit.setMessageKey(messageKey);
        audit.setEventType(eventType);
        audit.setBookingId(bookingId);
        audit.setBookingNo(bookingNo);
        audit.setAction(action);
        audit.setDetail(detail);
        mapper.insertAudit(audit);
    }

    private String subject(String eventType, String bookingNo) {
        if ("BOOKING_APPROVED".equals(eventType)) return "Booking approved: " + bookingNo;
        if ("BOOKING_CANCELLED".equals(eventType)) return "Booking cancelled: " + bookingNo;
        return "Booking event: " + bookingNo;
    }

    private String content(String eventType, Map<String, Object> payload) {
        String taskName = string(payload.get("taskName"));
        String bookingNo = string(payload.get("bookingNo"));
        if ("BOOKING_APPROVED".equals(eventType)) {
            return "Booking " + bookingNo + " has been approved. task=" + taskName
                    + ", occupancyCount=" + payload.get("occupancyCount");
        }
        if ("BOOKING_CANCELLED".equals(eventType)) {
            return "Booking " + bookingNo + " has been cancelled. reason=" + string(payload.get("reason"));
        }
        return "Booking event received: " + bookingNo;
    }

    private Long longValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int safeLimit(int limit) {
        return limit <= 0 ? 50 : Math.min(limit, 200);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
