package com.lowaltitude.booking.service;

import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.mapper.BookingMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConflictSuggestionService {

    private final BookingMapper bookingMapper;

    public ConflictSuggestionService(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    public Map<String, Object> suggestions(Long bookingId) {
        List<ConflictRecord> conflicts = bookingMapper.listConflicts(bookingId, null, "ACTIVE");
        List<Map<String, Object>> items = conflicts.stream().map(this::suggestion).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bookingId", bookingId);
        result.put("conflictCount", conflicts.size());
        result.put("suggestions", items);
        return result;
    }

    public ConflictRecord get(Long id) {
        return bookingMapper.listConflicts(null, null, null)
                .stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> suggestion(ConflictRecord conflict) {
        String type = conflict.getConflictType();
        String rule = conflict.getRuleCode();
        String action;
        if ("NO_FLY_GRID".equalsIgnoreCase(rule)) {
            action = "REPLAN_ROUTE";
        } else if ("HARD".equalsIgnoreCase(type)) {
            action = "DELAY_OR_CHANGE_LEVEL";
        } else if ("RISK".equalsIgnoreCase(type)) {
            action = "MANUAL_REVIEW_OR_ADJACENT_GRID";
        } else {
            action = "MANUAL_REVIEW";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conflictId", conflict.getId());
        result.put("bookingId", conflict.getBookingId());
        result.put("conflictType", type);
        result.put("ruleCode", rule);
        result.put("gridCode", conflict.getGridCode());
        result.put("levelId", conflict.getLevelId());
        result.put("timeSlotId", conflict.getTimeSlotId());
        result.put("reason", conflict.getMessage());
        result.put("suggestedAction", action);
        result.put("suggestion", buildText(action, conflict));
        return result;
    }

    private String buildText(String action, ConflictRecord conflict) {
        return switch (action) {
            case "REPLAN_ROUTE" -> "Route crosses a no-fly grid. Replan the route around " + conflict.getGridCode() + ".";
            case "DELAY_OR_CHANGE_LEVEL" -> "Hard conflict at grid " + conflict.getGridCode() + ". Delay by one TimeSlot or change to a safe adjacent altitude level.";
            case "MANUAL_REVIEW_OR_ADJACENT_GRID" -> "Risk conflict near " + conflict.getGridCode() + ". Consider adjacent grid routing or manual review.";
            default -> "Manual review is required for this conflict.";
        };
    }
}
