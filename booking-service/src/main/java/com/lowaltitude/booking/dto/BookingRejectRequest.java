package com.lowaltitude.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class BookingRejectRequest {
    private Long operatorUserId;
    private String operatorName;
    @NotBlank
    private String rejectReason;

    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
