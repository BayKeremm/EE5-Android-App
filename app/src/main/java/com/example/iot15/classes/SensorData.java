package com.example.iot15.classes;

public class SensorData {
/*    private enum SensorDataType {
        MOISTURE,
        TEMPERATURE,
        LIGHT
    };
    private SensorDataType type;
    public SensorDataType getType() {
        return type;
    }

    public void setType(SensorDataType type) {
        this.type = type;
    }
    */

    private int id;
    private String type;
    private String timestamp;
    private double value;

    public SensorData() {
        this.id = 0;
        this.type = null;
        this.timestamp = "No data";
        this.value = 0.0;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
