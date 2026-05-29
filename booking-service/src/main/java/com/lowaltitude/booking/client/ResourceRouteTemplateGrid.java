package com.lowaltitude.booking.client;

public class ResourceRouteTemplateGrid {
    private Long id;
    private Long routeTemplateId;
    private Long gridId;
    private String gridCode;
    private String gridName;
    private String gridStatus;
    private Integer rowIndex;
    private Integer colIndex;
    private Integer sequenceNo;
    private Integer plannedDurationMinutes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRouteTemplateId() { return routeTemplateId; }
    public void setRouteTemplateId(Long routeTemplateId) { this.routeTemplateId = routeTemplateId; }
    public Long getGridId() { return gridId; }
    public void setGridId(Long gridId) { this.gridId = gridId; }
    public String getGridCode() { return gridCode; }
    public void setGridCode(String gridCode) { this.gridCode = gridCode; }
    public String getGridName() { return gridName; }
    public void setGridName(String gridName) { this.gridName = gridName; }
    public String getGridStatus() { return gridStatus; }
    public void setGridStatus(String gridStatus) { this.gridStatus = gridStatus; }
    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }
    public Integer getColIndex() { return colIndex; }
    public void setColIndex(Integer colIndex) { this.colIndex = colIndex; }
    public Integer getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }
    public Integer getPlannedDurationMinutes() { return plannedDurationMinutes; }
    public void setPlannedDurationMinutes(Integer plannedDurationMinutes) { this.plannedDurationMinutes = plannedDurationMinutes; }
}
