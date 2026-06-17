package com.lowaltitude.booking.service;

import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.mapper.BookingMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConflictSuggestionServiceTest {

    private final BookingMapper bookingMapper = mock(BookingMapper.class);
    private final ConflictSuggestionService service = new ConflictSuggestionService(bookingMapper);

    @Test
    void suggestionsRecommendDelayOrLevelChangeForHardConflicts() {
        ConflictRecord conflict = conflict(7L, "HARD", "SAME_GRID_TIME_SLOT", "G-08-12");
        when(bookingMapper.listConflicts(42L, null, "ACTIVE")).thenReturn(List.of(conflict));

        Map<String, Object> result = service.suggestions(42L);

        assertThat(result).containsEntry("bookingId", 42L).containsEntry("conflictCount", 1);
        List<?> suggestions = (List<?>) result.get("suggestions");
        assertThat(suggestions).hasSize(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) suggestions.get(0);
        assertThat(first)
                .containsEntry("conflictId", 7L)
                .containsEntry("suggestedAction", "DELAY_OR_CHANGE_LEVEL");
    }

    @Test
    void suggestionsRecommendRouteReplanForNoFlyGrid() {
        ConflictRecord conflict = conflict(9L, "HARD", "NO_FLY_GRID", "G-10-10");
        when(bookingMapper.listConflicts(43L, null, "ACTIVE")).thenReturn(List.of(conflict));

        Map<String, Object> result = service.suggestions(43L);

        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) ((List<?>) result.get("suggestions")).get(0);
        assertThat(first)
                .containsEntry("suggestedAction", "REPLAN_ROUTE")
                .extracting("suggestion")
                .asString()
                .contains("G-10-10");
    }

    private ConflictRecord conflict(Long id, String type, String rule, String gridCode) {
        ConflictRecord record = new ConflictRecord();
        record.setId(id);
        record.setConflictType(type);
        record.setRuleCode(rule);
        record.setGridCode(gridCode);
        record.setLevelId(2L);
        record.setTimeSlotId(1L);
        record.setMessage("demo conflict");
        record.setStatus("ACTIVE");
        return record;
    }
}
