package com.amazonaws.lambda.interx;

public class SensorEvent {
    private String timestamp;
    private String factoryId;
    private String iotCode;
    private String content;

    public void setTimestamp(String timestamp) {

        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public void setIotCode(String iotCode) {
        this.iotCode = iotCode;
    }

    public String getIotCode() {
        return iotCode;
    }

    public void setContent(String content) {

        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public SensorEvent() { }

    public SensorEvent(String timestamp, String factoryId, String iotCode, String content) {
        this.timestamp = timestamp;
        this.factoryId = factoryId;
        this.iotCode = iotCode;
        this.content = content;
    }
}