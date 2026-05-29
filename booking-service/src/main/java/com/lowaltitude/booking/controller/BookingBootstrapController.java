package com.lowaltitude.booking.controller;

import com.lowaltitude.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingBootstrapController {
    @GetMapping("/bootstrap")
    public ApiResponse<Map<String, Object>> bootstrap() {
        return ApiResponse.ok(Map.of(
                "service", "booking-service",
                "phase", "03",
                "implementedStates", List.of("PENDING", "APPROVED", "REJECTED", "CANCELLED"),
                "implementedApis", List.of(
                        "POST /api/bookings",
                        "GET /api/bookings",
                        "GET /api/bookings/{id}",
                        "POST /api/bookings/{id}/approve",
                        "POST /api/bookings/{id}/reject",
                        "POST /api/bookings/{id}/cancel",
                        "GET /api/bookings/{id}/flows",
                        "GET /api/bookings/{id}/occupancies"
                ),
                "transactionBoundary", "approve booking: booking_record status + resource_occupancy + booking_flow_record",
                "next", "Phase 04 will add hard/risk/no-fly conflict detection before approval."
        ));
    }
}
