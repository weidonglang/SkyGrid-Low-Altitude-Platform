package com.lowaltitude.conflict.messaging;

public final class BookingRabbitNames {
    private BookingRabbitNames() {}

    public static final String EXCHANGE = "low-altitude.booking.exchange";
    public static final String APPROVED_ROUTING_KEY = "booking.approved";
    public static final String CANCELLED_ROUTING_KEY = "booking.cancelled";
    public static final String NOTIFY_QUEUE = "low-altitude.booking.notify.queue";
}
