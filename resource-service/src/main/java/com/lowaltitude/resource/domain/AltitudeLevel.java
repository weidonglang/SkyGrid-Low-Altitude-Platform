package com.lowaltitude.resource.domain;

import java.time.LocalDateTime;

public class AltitudeLevel {
    private Long id;
    private String levelCode;
    private String levelName;
    private Integer minAltitudeM;
    private Integer maxAltitudeM;
    private Integer sortOrder;
    private Boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public Integer getMinAltitudeM() { return minAltitudeM; }
    public void setMinAltitudeM(Integer minAltitudeM) { this.minAltitudeM = minAltitudeM; }
    public Integer getMaxAltitudeM() { return maxAltitudeM; }
    public void setMaxAltitudeM(Integer maxAltitudeM) { this.maxAltitudeM = maxAltitudeM; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
