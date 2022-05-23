package com.example.iot15.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.iot15.R;
import com.example.iot15.classes.Plant;
import com.example.iot15.classes.User;
import com.example.iot15.esptouch.EspTouchActivity;
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
import java.nio.charset.StandardCharsets;

public class SettingsFragment extends Fragment {
    public static final String TAG = "SettingsFragment";

    private User user;
    private Plant plant;

    private TextView textAutomationState;
    private TextView plantNameTextView;
    private Switch switchAutomation;
    private Switch switchWatering;
    private Switch switchLightLevelControl;
    private StringBuilder mqttMessage = new StringBuilder("00000");
    private Button wifiBtn;
    private Slider lightLevelControlSlider;
    private ConstraintLayout manualModeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        plantNameTextView = view.findViewById(R.id.plantNameSettings);
        switchAutomation = view.findViewById(R.id.switchAutomation);
        switchWatering = view.findViewById(R.id.switchWatering);
        switchLightLevelControl = view.findViewById(R.id.switchLightLevelControl);
        wifiBtn = view.findViewById(R.id.wifiBtn);
        textAutomationState = view.findViewById(R.id.textAutomationState);
        lightLevelControlSlider = view.findViewById(R.id.lightLevelControlSlider);
        manualModeContainer = view.findViewById(R.id.manualModeContainer);

        // get User and Plant from mainactivity
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            user = (User) bundle.getSerializable("USER");
            plantNameTextView.setText(plant.getPlantName());
        }

        wifiBtn.setOnClickListener(v -> {
            Intent goToEspTouch = new Intent(getActivity(), EspTouchActivity.class);
            goToEspTouch.putExtra("USER", user);
            goToEspTouch.putExtra("PLANT", plant);
            startActivity(goToEspTouch);
            requireActivity().overridePendingTransition(0, 0);
        });

        mqttConnectAndSubscribe();

        lightLevelControlSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            @SuppressLint("RestrictedApi")
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // nothing has to be done here
            }

            @Override
            @SuppressLint("RestrictedApi")
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // only when slider is released, the mqtt message will be sent. Otherwise there are
                // too many messages at once.
                changeSliderMqttValue(slider.getValue());
            }
        });

        switchAutomation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Manual mode
                    changeMqttMessage(0, '1');
                    changeMqttMessage(1, '0');
                    changeMqttMessage(2, '0');
                    textAutomationState.setText("Manual");
                    manualModeContainer.setAlpha(1.0F);
                    switchWatering.setEnabled(true);
                    switchLightLevelControl.setEnabled(true);
                } else {
                    // Automatic mode
                    changeMqttMessage(0, '0');
                    changeMqttMessage(1, '0');
                    changeMqttMessage(2, '0');
                    textAutomationState.setText("Automatic");
                    fadeManualScreen();
                }
                mqttConnectAndPublish("/EE5iot15/commands/" + plant.getDeviceId(), mqttMessage.toString());
            }
        });
        switchWatering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Water on
                    changeMqttMessage(2, '1');
                } else {
                    // Water off
                    changeMqttMessage(2, '0');
                }
                mqttConnectAndPublish("/EE5iot15/commands/" + plant.getDeviceId(), mqttMessage.toString());
            }
        });
        switchLightLevelControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Light on
                    changeMqttMessage(1, '1');
                    lightLevelControlSlider.setVisibility(View.VISIBLE);
                } else {
                    // Light off, aka manual
                    changeMqttMessage(1, '0');
                    lightLevelControlSlider.setVisibility(View.GONE);
                }
                mqttConnectAndPublish("/EE5iot15/commands/" + plant.getDeviceId(), mqttMessage.toString());
            }
        });

        return view;
    }

    public void fadeManualScreen() {
        manualModeContainer.setAlpha(0.5F);
        switchWatering.setChecked(false);
        switchWatering.setEnabled(false);
        switchLightLevelControl.setChecked(false);
        lightLevelControlSlider.setVisibility(View.GONE);
        switchLightLevelControl.setEnabled(false);
    }

    public void changeSliderMqttValue(float value) {
        StringBuilder valueString = new StringBuilder(String.valueOf(value));
        if (value < 10) {
            changeMqttMessage(3, valueString.charAt(0));
            changeMqttMessage(4, '0');
        } else {
            changeMqttMessage(3, valueString.charAt(0));
            changeMqttMessage(4, valueString.charAt(1));
        }
        mqttConnectAndPublish("/EE5iot15/commands/" + plant.getDeviceId(), mqttMessage.toString());
    }

    public void changeMqttMessage(int index, char status) {
        mqttMessage.setCharAt(index, status);
    }

    public void mqttConnectAndPublish(String topic, String message) {
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

    public void mqttConnectAndSubscribe() {
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

    public void mqttPublish(MqttAndroidClient client, String topic, String messageString) {
        byte[] encodedPayload;
        try {
            encodedPayload = messageString.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void mqttSubscribe(MqttAndroidClient client) {
        int qos = 1;
        try {
            client.subscribe("/EE5iot15/warnings/" + plant.getDeviceId(), qos);
            client.subscribe("/EE5iot15/commands/" + plant.getDeviceId(), qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if (topic.compareTo("/EE5iot15/warnings/" + plant.getDeviceId()) == 0) {
                        // show warnings somewhere
                    } else if (topic.compareTo("/EE5iot15/commands/" + plant.getDeviceId()) == 0) {
                        String response = new String(message.getPayload());
                        mqttMessage = new StringBuilder(response);
                        if (response.charAt(0) == '0') {
                            fadeManualScreen();
                        } else {
                            switchAutomation.setChecked(true);
                        }
                        if (response.charAt(1) == '1') {
                            switchLightLevelControl.setChecked(true);
                        } else {
                            lightLevelControlSlider.setVisibility(View.GONE);
                        }
                        if (response.charAt(2) == '1') {
                            switchWatering.setChecked(true);
                        }
                        lightLevelControlSlider.setValue(Integer.valueOf(mqttMessage.substring(3, 5)));
                        // once last state of app is fetched, subscribing to this topic is no longer necessary
                        client.unsubscribe("/EE5iot15/commands");
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
}