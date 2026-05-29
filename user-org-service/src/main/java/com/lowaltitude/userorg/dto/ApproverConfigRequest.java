package com.lowaltitude.userorg.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ApproverConfigRequest {
    @NotNull(message = "组织ID不能为空")
    private Long orgId;
    @NotNull(message = "审批员用户ID不能为空")
    private Long approverUserId;
    @NotNull(message = "审批层级不能为空")
    @Min(value = 1, message = "审批层级必须从1开始")
    private Integer levelOrder;
    private Boolean enabled = true;
    private String description;

    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
    public Integer getLevelOrder() { return levelOrder; }
    public void setLevelOrder(Integer levelOrder) { this.levelOrder = levelOrder; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
