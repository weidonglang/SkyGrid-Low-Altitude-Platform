package com.lowaltitude.conflict.metrics;

import com.lowaltitude.conflict.mapper.NotificationMapper;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetricsBinder {
    public NotificationMetricsBinder(MeterRegistry registry, NotificationMapper mapper) {
        Gauge.builder("low_altitude_notify_sent", mapper, value -> value.countNotifyByStatus("SENT"))
                .description("Successfully generated notification records")
                .register(registry);
        Gauge.builder("low_altitude_audit_total", mapper, NotificationMapper::countAuditAll)
                .description("Total audit log records")
                .register(registry);
        Gauge.builder("low_altitude_idempotent_processed", mapper, value -> value.countIdempotentByStatus("PROCESSED"))
                .description("Processed idempotent message keys")
                .register(registry);
    }
}
