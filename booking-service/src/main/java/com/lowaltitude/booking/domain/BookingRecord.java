package com.lowaltitude.booking.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingRecord {
    private Long id;
    private String bookingNo;
    private String taskName;
    private Long orgId;
    private Long applicantUserId;
    private String applicantName;
    private Long routeTemplateId;
    private String routeTemplateName;
    private Long levelId;
    private LocalDate bookingDate;
    private String status;
    private String applyReason;
    private String approvalComment;
    private String rejectReason;
    private String cancelReason;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> timeSlotIds = new ArrayList<>();
    private List<BookingFlowRecord> flows = new ArrayList<>();
    private List<ResourceOccupancy> occupancies = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
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
    public String getRouteTemplateName() { return routeTemplateName; }
    public void setRouteTemplateName(String routeTemplateName) { this.routeTemplateName = routeTemplateName; }
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApplyReason() { return applyReason; }
    public void setApplyReason(String applyReason) { this.applyReason = applyReason; }
    public String getApprovalComment() { return approvalComment; }
    public void setApprovalComment(String approvalComment) { this.approvalComment = approvalComment; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Long> getTimeSlotIds() { return timeSlotIds; }
    public void setTimeSlotIds(List<Long> timeSlotIds) { this.timeSlotIds = timeSlotIds; }
    public List<BookingFlowRecord> getFlows() { return flows; }
    public void setFlows(List<BookingFlowRecord> flows) { this.flows = flows; }
    public List<ResourceOccupancy> getOccupancies() { return occupancies; }
    public void setOccupancies(List<ResourceOccupancy> occupancies) { this.occupancies = occupancies; }
}
