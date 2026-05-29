package com.lowaltitude.booking.client;

public class ResourceGrid {
    private Long id;
    private String gridCode;
    private String gridName;
    private Integer rowIndex;
    private Integer colIndex;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGridCode() { return gridCode; }
    public void setGridCode(String gridCode) { this.gridCode = gridCode; }
    public String getGridName() { return gridName; }
    public void setGridName(String gridName) { this.gridName = gridName; }
    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }
    public Integer getColIndex() { return colIndex; }
    public void setColIndex(Integer colIndex) { this.colIndex = colIndex; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
