package com.lowaltitude.resource.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AltitudeLevelRequest {
    @NotBlank(message = "高度层编码不能为空")
    private String levelCode;
    @NotBlank(message = "高度层名称不能为空")
    private String levelName;
    @NotNull(message = "最低高度不能为空")
    @Min(0)
    private Integer minAltitudeM;
    @NotNull(message = "最高高度不能为空")
    @Min(1)
    private Integer maxAltitudeM;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
    private String description;

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
}
