package com.lowaltitude.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GridRequest {
    @NotBlank(message = "网格编码不能为空")
    private String gridCode;
    @NotBlank(message = "网格名称不能为空")
    private String gridName;
    @NotNull(message = "行号不能为空")
    private Integer rowIndex;
    @NotNull(message = "列号不能为空")
    private Integer colIndex;
    private BigDecimal centerLon;
    private BigDecimal centerLat;
    private String status = "ACTIVE";
    private String description;

    public String getGridCode() { return gridCode; }
    public void setGridCode(String gridCode) { this.gridCode = gridCode; }
    public String getGridName() { return gridName; }
    public void setGridName(String gridName) { this.gridName = gridName; }
    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }
    public Integer getColIndex() { return colIndex; }
    public void setColIndex(Integer colIndex) { this.colIndex = colIndex; }
    public BigDecimal getCenterLon() { return centerLon; }
    public void setCenterLon(BigDecimal centerLon) { this.centerLon = centerLon; }
    public BigDecimal getCenterLat() { return centerLat; }
    public void setCenterLat(BigDecimal centerLat) { this.centerLat = centerLat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
