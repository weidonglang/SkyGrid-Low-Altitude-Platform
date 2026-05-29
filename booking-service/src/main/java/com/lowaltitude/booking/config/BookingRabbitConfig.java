package com.lowaltitude.booking.config;

import com.lowaltitude.booking.messaging.BookingRabbitNames;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookingRabbitConfig {
    @Bean
    public DirectExchange bookingEventExchange() {
        return new DirectExchange(BookingRabbitNames.EXCHANGE, true, false);
    }

    @Bean
    public Queue bookingEventQueue() {
        return QueueBuilder.durable(BookingRabbitNames.NOTIFY_QUEUE).build();
    }

    @Bean
    public Binding approvedBinding(Queue bookingEventQueue, DirectExchange bookingEventExchange) {
        return BindingBuilder.bind(bookingEventQueue).to(bookingEventExchange).with(BookingRabbitNames.APPROVED_ROUTING_KEY);
    }

    @Bean
    public Binding cancelledBinding(Queue bookingEventQueue, DirectExchange bookingEventExchange) {
        return BindingBuilder.bind(bookingEventQueue).to(bookingEventExchange).with(BookingRabbitNames.CANCELLED_ROUTING_KEY);
    }
}
