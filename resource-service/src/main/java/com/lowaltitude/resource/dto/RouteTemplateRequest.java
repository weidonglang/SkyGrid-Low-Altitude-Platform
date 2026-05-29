package com.lowaltitude.resource.dto;

import jakarta.validation.constraints.NotBlank;

public class RouteTemplateRequest {
    @NotBlank(message = "航线模板编码不能为空")
    private String routeCode;
    @NotBlank(message = "航线模板名称不能为空")
    private String routeName;
    private String description;
    private Boolean enabled = true;
    private String createdBy;

    public String getRouteCode() { return routeCode; }
    public void setRouteCode(String routeCode) { this.routeCode = routeCode; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
