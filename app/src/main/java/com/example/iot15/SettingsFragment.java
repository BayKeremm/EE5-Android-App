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
import org.eclipse.paho.client.mqttv3.IMqttToken;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textAutomations = (TextView) view.findViewById(R.id.textAutomations);
        textWatering = (TextView) view.findViewById(R.id.textWatering);
        textLightLevelControl = (TextView) view.findViewById(R.id.textLightLevelControl);
        switchWatering = (Switch) view.findViewById(R.id.switchWatering);
        switchLightLevelControl = (Switch) view.findViewById(R.id.switchLightLevelControl);

        switchWatering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked==true){
                    mqttConnect("WaterON");
                } else {
                    mqttConnect("WaterOFF");
                }
            }
        });
        switchLightLevelControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked==true){
                    mqttConnect("LightON");
                } else {
                    mqttConnect("LightOFF");
                }
            }
        });

        return view;
    }

    // from https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/
    public void mqttConnect(String message){
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

    public void mqttPublish(MqttAndroidClient client, String messageString){
        String topic = "michiel";
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
}