package com.example.iot15;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class SettingsFragment extends Fragment {
    public static final String TAG = "SettingsFragment";

    private TextView textAutomations;
    private TextView textWatering;
    private TextView textLightLevelControl;
    private Switch switchWatering;
    private Switch switchLightLevelControl;
    private StringBuilder mqttMessage = new StringBuilder("00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textAutomations = (TextView) view.findViewById(R.id.textAutomations);
        textWatering = (TextView) view.findViewById(R.id.textWatering);
        textLightLevelControl = (TextView) view.findViewById(R.id.textLightLevelControl);
        switchWatering = (Switch) view.findViewById(R.id.switchWatering);
        switchLightLevelControl = (Switch) view.findViewById(R.id.switchLightLevelControl);

        mqttConnectAndSubscribe();


        switchWatering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked==true){
                    // Water on
                    changeMqttMessage(1, '1');
                } else {
                    // Water off
                    changeMqttMessage(1, '0');
                }
                mqttConnectAndPublish(mqttMessage.toString());
            }
        });
        switchLightLevelControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked==true){
                    // Light on
                    changeMqttMessage(0, '1');
                } else {
                    // Light off
                    changeMqttMessage(0, '0');
                }
                mqttConnectAndPublish(mqttMessage.toString());
            }
        });

        return view;
    }

    public void changeMqttMessage(int index, char status){
        mqttMessage.setCharAt(index, status);
    }

    public void mqttConnectAndPublish(String message){
        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client =
                new MqttAndroidClient(getContext(), "tcp://broker.hivemq.com:1883",
                        clientId);
        Log.d(TAG, "starts try");
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    mqttPublish(client, message);
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems.
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttConnectAndSubscribe(){
        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client =
                new MqttAndroidClient(getContext(), "tcp://broker.hivemq.com:1883",
                        clientId);
        Log.d(TAG, "starts try");
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    mqttSubscribe(client);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems.
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttPublish(MqttAndroidClient client, String messageString){
        String topic = "/EE5iot15/commands";
        String payload = messageString;
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttSubscribe(MqttAndroidClient client) {
        String topic = "/EE5iot15/warnings";
        int qos = 1;
        try {
            client.subscribe(topic, qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    textAutomations.setText(new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    //Override methods from MqttCallback interface
    //@Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("message is : "+message);
    }



}