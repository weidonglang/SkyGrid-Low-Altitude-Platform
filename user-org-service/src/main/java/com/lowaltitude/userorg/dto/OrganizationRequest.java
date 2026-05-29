package com.lowaltitude.userorg.dto;

import jakarta.validation.constraints.NotBlank;

public class OrganizationRequest {
    @NotBlank(message = "组织编码不能为空")
    private String orgCode;
    @NotBlank(message = "组织名称不能为空")
    private String orgName;
    private Long parentId;
    private String contactName;
    private String contactPhone;
    private Boolean enabled = true;
    private String description;

    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
