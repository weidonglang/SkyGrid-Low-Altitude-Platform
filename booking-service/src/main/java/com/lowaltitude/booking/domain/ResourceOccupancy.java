package com.lowaltitude.booking.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResourceOccupancy {
    private Long id;
    private Long bookingId;
    private String bookingNo;
    private Long routeTemplateId;
    private Long gridId;
    private String gridCode;
    private String gridName;
    private Long levelId;
    private Long timeSlotId;
    private LocalDate bookingDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public Long getRouteTemplateId() { return routeTemplateId; }
    public void setRouteTemplateId(Long routeTemplateId) { this.routeTemplateId = routeTemplateId; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
}
