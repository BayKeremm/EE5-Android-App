package com.example.iot15.classes;

public final class Values {
    // API endpoints
    public static final String LOGIN = "https://a21iot15.studev.groept.be/index.php/api/login/"; // + username + password
    public static final String SIGNUP = "\"https://a21iot15.studev.groept.be/index.php/api/register/"; // + username + password

    public static final String GETDEVICEID = "https://a21iot15.studev.groept.be/index.php/api/getDeviceId/"; // + plantId + token
    public static final String GETPLANTS = "https://a21iot15.studev.groept.be/index.php/api/listOwnedPlants/"; // + userId + token
    public static final String GETPLANTTYPES = "https://a21iot15.studev.groept.be/index.php/api/listPlants?token="; // + token
    public static final String ADDPLANT = "https://a21iot15.studev.groept.be/index.php/api/insertOwnedPlant/"; // + userId + planTypeId + deviceId + plantName + token
    public static final String GETMEASUREMENTS = "https://a21iot15.studev.groept.be/index.php/api/listMeasurements"; // + measurementType + numberOfMeasurements + plantId + token

    public static final String UPDATEPLANT = "https://a21iot15.studev.groept.be/index.php/api/updateOwnedPlant/"; // + plantId + plantTypeId + imgRef + plantName + token


}
