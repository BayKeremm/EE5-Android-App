package com.example.iot15.classes;

import java.io.Serializable;

public class Plant implements Serializable {
    private int id;
    private int userId;
    private int deviceId;
    private String plantName;
    private int plantType;
    private String imgBlob;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public int getPlantType() {
        return plantType;
    }

    public void setPlantType(int plantType) {
        this.plantType = plantType;
    }

    public String getImgBlob() {
        return imgBlob;
    }

    public void setImgBlob(String imgUri) {
        this.imgBlob = imgUri;
    }

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + id +
                ", userId=" + userId +
                ", plantName='" + plantName + '\'' +
                ", plantType='" + plantType + '\'' +
                ", imgBlob='" + imgBlob + '\'' +
                '}';
    }
}
