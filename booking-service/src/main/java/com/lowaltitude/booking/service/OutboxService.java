package com.lowaltitude.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowaltitude.booking.domain.BookingRecord;
import com.lowaltitude.booking.domain.OutboxMessage;
import com.lowaltitude.booking.mapper.OutboxMapper;
import com.lowaltitude.booking.messaging.BookingRabbitNames;
import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OutboxService {
    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private static final String STATUS_PENDING = "PENDING";

    private final OutboxMapper mapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final boolean publisherEnabled;
    private final int batchSize;

    public OutboxService(OutboxMapper mapper,
                         RabbitTemplate rabbitTemplate,
                         ObjectMapper objectMapper,
                         @Value("${low-altitude.outbox.publisher-enabled:true}") boolean publisherEnabled,
                         @Value("${low-altitude.outbox.batch-size:20}") int batchSize) {
        this.mapper = mapper;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.publisherEnabled = publisherEnabled;
        this.batchSize = batchSize;
    }

    public List<OutboxMessage> list(String status, Long aggregateId, String eventType, int limit) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        return mapper.list(blankToNull(status), aggregateId, blankToNull(eventType), safeLimit);
    }

    public OutboxMessage get(Long id) {
        OutboxMessage message = mapper.findById(id);
        if (message == null) throw new BusinessException(ErrorCode.NOT_FOUND, "outbox message not found: " + id);
        return message;
    }

    @Transactional
    public OutboxMessage createBookingApprovedEvent(BookingRecord booking,
                                                    List<Long> timeSlotIds,
                                                    String operatorName,
                                                    String comment,
                                                    int occupancyCount) {
        Map<String, Object> payload = basePayload("BOOKING_APPROVED", booking);
        payload.put("status", "APPROVED");
        payload.put("operatorName", operatorName);
        payload.put("comment", comment);
        payload.put("timeSlotIds", timeSlotIds);
        payload.put("occupancyCount", occupancyCount);
        return insert("BOOKING_APPROVED", booking.getId(), BookingRabbitNames.APPROVED_ROUTING_KEY, payload);
    }

    @Transactional
    public OutboxMessage createBookingCancelledEvent(BookingRecord booking,
                                                     String fromStatus,
                                                     String operatorName,
                                                     String reason) {
        Map<String, Object> payload = basePayload("BOOKING_CANCELLED", booking);
        payload.put("status", "CANCELLED");
        payload.put("fromStatus", fromStatus);
        payload.put("operatorName", operatorName);
        payload.put("reason", reason);
        return insert("BOOKING_CANCELLED", booking.getId(), BookingRabbitNames.CANCELLED_ROUTING_KEY, payload);
    }

    @Transactional
    public Map<String, Object> dispatchOnce() {
        return dispatchReadyMessages(batchSize);
    }

    @Transactional
    public Map<String, Object> dispatchReadyMessages(int limit) {
        List<OutboxMessage> messages = mapper.listReadyForDispatch(limit <= 0 ? batchSize : limit);
        int sent = 0;
        int failed = 0;
        for (OutboxMessage message : messages) {
            try {
                publish(message);
                mapper.markSent(message.getId());
                sent++;
            } catch (Exception ex) {
                failed++;
                LocalDateTime nextRetryAt = LocalDateTime.now().plusSeconds(nextRetryDelaySeconds(message));
                mapper.markFailed(message.getId(), nextRetryAt, shorten(ex.getMessage()));
                log.warn("Outbox dispatch failed. id={}, key={}, error={}", message.getId(), message.getMessageKey(), ex.toString());
            }
        }
        return Map.of("picked", messages.size(), "sent", sent, "failed", failed);
    }

    @Transactional
    public OutboxMessage requeue(Long id) {
        mapper.requeueFailed(id);
        return get(id);
    }

    public OutboxMessage republishForIdempotencyDemo(Long id) {
        OutboxMessage message = get(id);
        publish(message);
        return message;
    }

    @Scheduled(fixedDelayString = "${low-altitude.outbox.fixed-delay-ms:5000}", initialDelayString = "${low-altitude.outbox.initial-delay-ms:5000}")
    public void scheduledDispatch() {
        if (!publisherEnabled) return;
        try {
            Map<String, Object> result = dispatchReadyMessages(batchSize);
            Object picked = result.get("picked");
            if (picked instanceof Integer count && count > 0) {
                log.info("Outbox scheduled dispatch result: {}", result);
            }
        } catch (Exception ex) {
            log.warn("Outbox scheduled dispatch batch failed: {}", ex.toString());
        }
    }

    private OutboxMessage insert(String eventType, Long aggregateId, String routingKey, Map<String, Object> payload) {
        String messageKey = eventType + ":" + payload.get("bookingNo");
        payload.put("messageKey", messageKey);
        payload.put("eventType", eventType);
        payload.put("routingKey", routingKey);
        payload.put("createdAt", LocalDateTime.now().toString());
        OutboxMessage message = new OutboxMessage();
        message.setMessageKey(messageKey);
        message.setEventType(eventType);
        message.setAggregateType("BOOKING");
        message.setAggregateId(aggregateId);
        message.setRoutingKey(routingKey);
        message.setStatus(STATUS_PENDING);
        message.setRetryCount(0);
        message.setMaxRetryCount(8);
        message.setNextRetryAt(LocalDateTime.now());
        try {
            message.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "failed to serialize outbox payload: " + ex.getMessage());
        }
        mapper.insert(message);
        return message;
    }

    private Map<String, Object> basePayload(String eventType, BookingRecord booking) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", eventType);
        payload.put("bookingId", booking.getId());
        payload.put("bookingNo", booking.getBookingNo());
        payload.put("taskName", booking.getTaskName());
        payload.put("orgId", booking.getOrgId());
        payload.put("applicantUserId", booking.getApplicantUserId());
        payload.put("applicantName", booking.getApplicantName());
        payload.put("routeTemplateId", booking.getRouteTemplateId());
        payload.put("routeTemplateName", booking.getRouteTemplateName());
        payload.put("levelId", booking.getLevelId());
        payload.put("bookingDate", booking.getBookingDate() == null ? null : booking.getBookingDate().toString());
        return payload;
    }

    private void publish(OutboxMessage message) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding(StandardCharsets.UTF_8.name());
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setMessageId(String.valueOf(message.getId()));
        props.setHeader("messageKey", message.getMessageKey());
        props.setHeader("eventType", message.getEventType());
        props.setHeader("aggregateId", message.getAggregateId());
        Message amqpMessage = new Message(message.getPayload().getBytes(StandardCharsets.UTF_8), props);
        rabbitTemplate.send(BookingRabbitNames.EXCHANGE, message.getRoutingKey(), amqpMessage);
    }

    private long nextRetryDelaySeconds(OutboxMessage message) {
        int retry = message.getRetryCount() == null ? 0 : message.getRetryCount();
        return Math.min(300, 5L * (retry + 1));
    }

    private String shorten(String value) {
        if (value == null) return null;
        return value.length() <= 500 ? value : value.substring(0, 500);
    }


    public Map<String, Object> statusSummary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", mapper.countAll());
        result.put("pending", mapper.countByStatus("PENDING"));
        result.put("sent", mapper.countByStatus("SENT"));
        result.put("failed", mapper.countByStatus("FAILED"));
        result.put("retrying", mapper.countByStatus("FAILED"));
        return result;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
