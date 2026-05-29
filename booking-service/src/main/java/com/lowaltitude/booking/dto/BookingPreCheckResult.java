package com.lowaltitude.booking.dto;

import java.util.ArrayList;
import java.util.List;

public class BookingPreCheckResult {
    private boolean passed;
    private boolean canSubmit;
    private boolean canApprove;
    private int blockingCount;
    private int riskCount;
    private int candidateOccupancyCount;
    private String summary;
    private List<ConflictCheckItem> conflicts = new ArrayList<>();

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public boolean isCanSubmit() { return canSubmit; }
    public void setCanSubmit(boolean canSubmit) { this.canSubmit = canSubmit; }
    public boolean isCanApprove() { return canApprove; }
    public void setCanApprove(boolean canApprove) { this.canApprove = canApprove; }
    public int getBlockingCount() { return blockingCount; }
    public void setBlockingCount(int blockingCount) { this.blockingCount = blockingCount; }
    public int getRiskCount() { return riskCount; }
    public void setRiskCount(int riskCount) { this.riskCount = riskCount; }
    public int getCandidateOccupancyCount() { return candidateOccupancyCount; }
    public void setCandidateOccupancyCount(int candidateOccupancyCount) { this.candidateOccupancyCount = candidateOccupancyCount; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<ConflictCheckItem> getConflicts() { return conflicts; }
    public void setConflicts(List<ConflictCheckItem> conflicts) { this.conflicts = conflicts; }
}
