package com.lowaltitude.conflict.messaging;

import com.lowaltitude.conflict.service.NotificationService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class BookingEventListener {
    private final NotificationService service;

    public BookingEventListener(NotificationService service) {
        this.service = service;
    }

    @RabbitListener(queues = BookingRabbitNames.NOTIFY_QUEUE)
    public void handle(Message message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String messageId = message.getMessageProperties().getMessageId();
        service.consumeBookingEvent(body, routingKey, messageId);
    }
}
