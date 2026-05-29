package com.lowaltitude.booking.client;

import java.util.ArrayList;
import java.util.List;

public class ResourceRouteTemplate {
    private Long id;
    private String routeCode;
    private String routeName;
    private String description;
    private Boolean enabled;
    private String createdBy;
    private List<ResourceRouteTemplateGrid> grids = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public List<ResourceRouteTemplateGrid> getGrids() { return grids; }
    public void setGrids(List<ResourceRouteTemplateGrid> grids) { this.grids = grids; }
}
