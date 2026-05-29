package com.lowaltitude.resource.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RouteTemplateGridRequest {
    @NotNull(message = "网格ID不能为空")
    private Long gridId;
    @NotNull(message = "航线顺序不能为空")
    @Min(value = 1, message = "航线顺序必须从1开始")
    private Integer sequenceNo;
    @Min(value = 1, message = "预计通过时间必须大于0")
    private Integer plannedDurationMinutes = 5;

    public Long getGridId() { return gridId; }
    public void setGridId(Long gridId) { this.gridId = gridId; }
    public Integer getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }
    public Integer getPlannedDurationMinutes() { return plannedDurationMinutes; }
    public void setPlannedDurationMinutes(Integer plannedDurationMinutes) { this.plannedDurationMinutes = plannedDurationMinutes; }
}
