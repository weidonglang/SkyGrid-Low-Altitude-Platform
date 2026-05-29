package com.lowaltitude.userorg.dto;

import jakarta.validation.constraints.NotBlank;

public class UserAccountRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String passwordHash;
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    private String phone;
    private String email;
    private Long orgId;
    private Boolean enabled = true;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
