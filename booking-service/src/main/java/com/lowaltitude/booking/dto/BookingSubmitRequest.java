package com.lowaltitude.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class BookingSubmitRequest {
    @NotBlank
    private String taskName;
    @NotNull
    private Long orgId;
    private Long applicantUserId;
    private String applicantName;
    @NotNull
    private Long routeTemplateId;
    @NotNull
    private Long levelId;
    @NotNull
    @FutureOrPresent
    private LocalDate bookingDate;
    @NotEmpty
    private List<Long> timeSlotIds;
    private String applyReason;
    private String description;

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getApplicantUserId() { return applicantUserId; }
    public void setApplicantUserId(Long applicantUserId) { this.applicantUserId = applicantUserId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public Long getRouteTemplateId() { return routeTemplateId; }
    public void setRouteTemplateId(Long routeTemplateId) { this.routeTemplateId = routeTemplateId; }
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public List<Long> getTimeSlotIds() { return timeSlotIds; }
    public void setTimeSlotIds(List<Long> timeSlotIds) { this.timeSlotIds = timeSlotIds; }
    public String getApplyReason() { return applyReason; }
    public void setApplyReason(String applyReason) { this.applyReason = applyReason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
