package com.lowaltitude.booking.metrics;

import com.lowaltitude.booking.mapper.BookingMapper;
import com.lowaltitude.booking.mapper.OutboxMapper;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BookingMetricsBinder {
    public BookingMetricsBinder(MeterRegistry registry, OutboxMapper outboxMapper, BookingMapper bookingMapper) {
        Gauge.builder("low_altitude_outbox_pending", outboxMapper, mapper -> mapper.countByStatus("PENDING"))
                .description("Pending outbox messages waiting to be dispatched")
                .register(registry);
        Gauge.builder("low_altitude_outbox_failed", outboxMapper, mapper -> mapper.countByStatus("FAILED"))
                .description("Failed outbox messages requiring compensation")
                .register(registry);
        Gauge.builder("low_altitude_resource_occupancy_occupied", bookingMapper, mapper -> mapper.countOccupancyByStatus("OCCUPIED"))
                .description("Currently occupied low-altitude resource slices")
                .register(registry);
        Gauge.builder("low_altitude_conflict_active", bookingMapper, mapper -> mapper.countConflictByStatus("ACTIVE"))
                .description("Active conflict records")
                .register(registry);
    }
}
