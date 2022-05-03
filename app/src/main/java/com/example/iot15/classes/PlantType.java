package com.example.iot15.classes;

public class PlantType {
    private int id;
    private String name;
    private double idealMoisture;
    private double idealTemperature;
    private double idealLight;

    public PlantType(){}

    public PlantType(int id, String name, double idealMoisture, double idealTemperature, double idealLight) {
        this.id = id;
        this.name = name;
        this.idealMoisture = idealMoisture;
        this.idealTemperature = idealTemperature;
        this.idealLight = idealLight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getIdealMoisture() {
        return idealMoisture;
    }

    public void setIdealMoisture(double idealMoisture) {
        this.idealMoisture = idealMoisture;
    }

    public double getIdealTemperature() {
        return idealTemperature;
    }

    public void setIdealTemperature(double idealTemperature) {
        this.idealTemperature = idealTemperature;
    }

    public double getIdealLight() {
        return idealLight;
    }

    public void setIdealLight(double idealLight) {
        this.idealLight = idealLight;
    }
}
