package com.example.iot15.classes;

public final class Values {
    // API endpoints
    public static final String API_LOGIN = "https://a21iot15.studev.groept.be/index.php/api/login/"; // + username + password
    public static final String API_SIGNUP = "\"https://a21iot15.studev.groept.be/index.php/api/register/"; // + username + password

    public static final String API_ADDPLANT = "https://a21iot15.studev.groept.be/index.php/api/insertOwnedPlant/"; // + userId + planTypeId + deviceId + plantName + token
    public static final String API_UPDATEPLANT = "https://a21iot15.studev.groept.be/index.php/api/updateOwnedPlant/"; // + plantId + plantTypeId + imgRef + plantName + token

    public static final String API_GETDEVICEID = "https://a21iot15.studev.groept.be/index.php/api/getDeviceId/"; // + plantId + token
    public static final String API_GETPLANTS = "https://a21iot15.studev.groept.be/index.php/api/listOwnedPlants/"; // + userId + token
    public static final String API_GETPLANTTYPES = "https://a21iot15.studev.groept.be/index.php/api/listPlants?token="; // + token
    public static final String API_GETMEASUREMENTS = "https://a21iot15.studev.groept.be/index.php/api/listMeasurements"; // + measurementType + numberOfMeasurements + plantId + token

    // MQTT endpoints
    public static final String MQTT_WARNING = "/EE5iot15/warnings/"; // + deviceId;
    public static final String MQTT_COMMANDS = "/EE5iot15/commands/"; // + deviceId;
    public static final String MQTT_SERVER_URI = "tcp://broker.hivemq.com:1883";


}
