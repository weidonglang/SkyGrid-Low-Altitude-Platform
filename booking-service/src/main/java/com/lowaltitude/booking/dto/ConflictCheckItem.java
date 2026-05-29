package com.lowaltitude.booking.dto;

import java.time.LocalDate;

public class ConflictCheckItem {
    private String conflictType;
    private String conflictLevel;
    private String ruleCode;
    private Long gridId;
    private String gridCode;
    private String gridName;
    private Long levelId;
    private Long timeSlotId;
    private LocalDate bookingDate;
    private Long relatedBookingId;
    private String relatedBookingNo;
    private String message;

    public ConflictCheckItem() {}

    public ConflictCheckItem(String conflictType, String conflictLevel, String ruleCode,
                             Long gridId, String gridCode, String gridName,
                             Long levelId, Long timeSlotId, LocalDate bookingDate,
                             Long relatedBookingId, String relatedBookingNo, String message) {
        this.conflictType = conflictType;
        this.conflictLevel = conflictLevel;
        this.ruleCode = ruleCode;
        this.gridId = gridId;
        this.gridCode = gridCode;
        this.gridName = gridName;
        this.levelId = levelId;
        this.timeSlotId = timeSlotId;
        this.bookingDate = bookingDate;
        this.relatedBookingId = relatedBookingId;
        this.relatedBookingNo = relatedBookingNo;
        this.message = message;
    }

    public boolean isBlocking() { return "BLOCKING".equals(conflictLevel); }

    public String getConflictType() { return conflictType; }
    public void setConflictType(String conflictType) { this.conflictType = conflictType; }
    public String getConflictLevel() { return conflictLevel; }
    public void setConflictLevel(String conflictLevel) { this.conflictLevel = conflictLevel; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public Long getGridId() { return gridId; }
    public void setGridId(Long gridId) { this.gridId = gridId; }
    public String getGridCode() { return gridCode; }
    public void setGridCode(String gridCode) { this.gridCode = gridCode; }
    public String getGridName() { return gridName; }
    public void setGridName(String gridName) { this.gridName = gridName; }
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public Long getRelatedBookingId() { return relatedBookingId; }
    public void setRelatedBookingId(Long relatedBookingId) { this.relatedBookingId = relatedBookingId; }
    public String getRelatedBookingNo() { return relatedBookingNo; }
    public void setRelatedBookingNo(String relatedBookingNo) { this.relatedBookingNo = relatedBookingNo; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
