package com.lowaltitude.booking.service;

import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.domain.ResourceOccupancy;
import com.lowaltitude.booking.mapper.BookingMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private final BookingMapper bookingMapper = mock(BookingMapper.class);
    private final OutboxService outboxService = mock(OutboxService.class);
    private final DashboardService service = new DashboardService(bookingMapper, outboxService);

    @Test
    void overviewAggregatesBookingOccupancyConflictAndOutboxMetrics() {
        when(bookingMapper.countOccupancyAll()).thenReturn(10);
        when(bookingMapper.countOccupancyByStatus(BookingService.OCCUPIED)).thenReturn(4);
        when(bookingMapper.countBookingByDate(any(LocalDate.class))).thenReturn(7);
        when(bookingMapper.countBookingByStatus(BookingService.STATUS_PENDING)).thenReturn(2);
        when(bookingMapper.countBookingByStatus(BookingService.STATUS_APPROVED)).thenReturn(5);
        when(bookingMapper.countConflictByStatusAndType("ACTIVE", "HARD")).thenReturn(1);
        when(bookingMapper.countConflictByStatusAndType("ACTIVE", "RISK")).thenReturn(3);
        when(outboxService.statusSummary()).thenReturn(Map.of(
                "total", 8,
                "pending", 2,
                "sent", 6,
                "failed", 1,
                "retrying", 1
        ));

        Map<String, Object> overview = service.overview();

        assertThat(overview)
                .containsEntry("todayTaskCount", 7)
                .containsEntry("pendingApprovalCount", 2)
                .containsEntry("approvedTaskCount", 5)
                .containsEntry("hardConflictCount", 1)
                .containsEntry("riskConflictCount", 3)
                .containsEntry("resourceOccupancyRate", 0.4)
                .containsEntry("outboxPendingCount", 2)
                .containsEntry("outboxRetryingCount", 1)
                .containsEntry("outboxFailedCount", 1)
                .containsEntry("notificationSuccessRate", 0.75)
                .containsEntry("auditLogCount", 6);
    }

    @Test
    void occupancyReturnsCountsAndRecentItems() {
        ResourceOccupancy occupancy = new ResourceOccupancy();
        occupancy.setGridCode("G-08-12");
        when(bookingMapper.countOccupancyByStatus(BookingService.OCCUPIED)).thenReturn(3);
        when(bookingMapper.countOccupancyByStatus("RELEASED")).thenReturn(2);
        when(bookingMapper.listRecentOccupancies(100)).thenReturn(List.of(occupancy));

        Map<String, Object> result = service.occupancy();

        assertThat(result)
                .containsEntry("activeCount", 3)
                .containsEntry("releasedCount", 2);
        assertThat((List<?>) result.get("recent")).hasSize(1);
    }

    @Test
    void conflictsClassifiesActiveConflictTypes() {
        ConflictRecord hard = conflict("HARD");
        ConflictRecord risk = conflict("RISK");
        when(bookingMapper.listConflicts(null, null, "ACTIVE")).thenReturn(List.of(hard, risk));

        Map<String, Object> result = service.conflicts();

        assertThat(result)
                .containsEntry("activeCount", 2)
                .containsEntry("hardConflictCount", 1L)
                .containsEntry("riskConflictCount", 1L);
    }

    private ConflictRecord conflict(String type) {
        ConflictRecord record = new ConflictRecord();
        record.setConflictType(type);
        record.setStatus("ACTIVE");
        return record;
    }
}
