package com.lowaltitude.booking.client;

public class ResourceAltitudeLevel {
    private Long id;
    private String levelCode;
    private String levelName;
    private Integer minAltitudeM;
    private Integer maxAltitudeM;
    private Boolean enabled;

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
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
