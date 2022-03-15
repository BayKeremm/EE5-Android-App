package com.example.iot15;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

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
    private Button wifiBtn;
    private Slider lightLevelControlSlider;
    private TextView textLightAutomationState;
    private TextView textWateringAutomationState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textAutomations = (TextView) view.findViewById(R.id.textAutomations);
        textWatering = (TextView) view.findViewById(R.id.textWatering);
        textLightLevelControl = (TextView) view.findViewById(R.id.textLightLevelControl);
        switchWatering = (Switch) view.findViewById(R.id.switchWatering);
        switchLightLevelControl = (Switch) view.findViewById(R.id.switchLightLevelControl);
        wifiBtn = (Button) view.findViewById(R.id.wifiBtn);
        lightLevelControlSlider = (Slider) view.findViewById(R.id.lightLevelControlSlider);
        textLightAutomationState = (TextView) view.findViewById(R.id.textLightAutomationState);
        textWateringAutomationState = (TextView) view.findViewById(R.id.textWateringAutomationState);

        wifiBtn.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), EspTouchActivity.class)));

        mqttConnectAndSubscribe();

        lightLevelControlSlider.addOnChangeListener((slider, value, fromUser) -> mqttConnectAndPublish("/EE5iot15/lightlevel","" + value));

        switchWatering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked==true){
                    // Water on
                    changeMqttMessage(1, '1');
                    textWateringAutomationState.setText("On");
                } else {
                    // Water off
                    changeMqttMessage(1, '0');
                    textWateringAutomationState.setText("Off");
                }
                mqttConnectAndPublish("/EE5iot15/commands", mqttMessage.toString());
            }
        });
        switchLightLevelControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked==true){
                    // Light on
                    changeMqttMessage(0, '1');
                    lightLevelControlSlider.setVisibility(View.GONE);
                    textLightAutomationState.setText("Automatic");
                } else {
                    // Light off, aka manual
                    changeMqttMessage(0, '0');
                    lightLevelControlSlider.setVisibility(View.VISIBLE);
                    textLightAutomationState.setText("Manual");
                }
                mqttConnectAndPublish("/EE5iot15/commands", mqttMessage.toString());
            }
        });

        return view;
    }

    public void changeMqttMessage(int index, char status){
        mqttMessage.setCharAt(index, status);
    }

    public void mqttConnectAndPublish(String topic, String message){
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
                    mqttPublish(client, topic, message);
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

    public void mqttPublish(MqttAndroidClient client, String topic, String messageString){
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
        int qos = 1;
        try {
            client.subscribe("/EE5iot15/warnings", qos);
            client.subscribe("/EE5iot15/commands",qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(topic.compareTo("/EE5iot15/warnings") == 0){
                        // show warnings somewhere
                    }
                    else if(topic.compareTo("/EE5iot15/commands") == 0){
                        String response = new String(message.getPayload());
                        mqttMessage = new StringBuilder(response);
                        if(response.charAt(0) == '1'){
                            switchLightLevelControl.setChecked(true);
                        }
                        if(response.charAt(1) == '1'){
                            switchWatering.setChecked(true);
                        }
                    }
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