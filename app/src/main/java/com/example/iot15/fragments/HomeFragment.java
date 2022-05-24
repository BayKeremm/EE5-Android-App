package com.example.iot15.fragments;

import static android.app.Activity.RESULT_OK;
import static com.example.iot15.classes.Values.API_GETMEASUREMENTS;
import static com.example.iot15.classes.Values.API_GETPLANTTYPES;
import static com.example.iot15.classes.Values.API_UPDATEPLANT;
import static com.example.iot15.classes.Values.MQTT_SERVER_URI;
import static com.example.iot15.classes.Values.MQTT_WARNING;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.R;
import com.example.iot15.activities.SelectPlantActivity;
import com.example.iot15.adapters.ExpandableListAdapter;
import com.example.iot15.classes.Base64Encoder;
import com.example.iot15.classes.Plant;
import com.example.iot15.classes.PlantType;
import com.example.iot15.classes.SensorData;
import com.example.iot15.classes.User;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";

    private User user;
    private Plant plant;
    private final List<PlantType> plantTypeList = new ArrayList<>();

    private ProgressBar progressWater;
    private ProgressBar progressTemperature;
    private ProgressBar progressLight;
    private TextView plantNameText;
    private ImageButton editButton;
    private ImageButton refreshHomeData;
    private ImageButton returnButton;
    private TextView textLastModified;
    private ImageView savedPlantPicture;
    private TextView textWarning;

    // plant edit dialog
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText editTextName;
    private Button cancelEditBtn, applyEditBtn;
    //private TextView plantTypeList;
    private ExpandableListView plantTypesListView;
    private TextView chosenTypeText;
    private int chosenPlantTypeId;
    private Button BSelectImage;
    private ImageView IVPreviewImage;
    private final int SELECT_PICTURE = 200; // constant to compare the activity result code
    private Bitmap selectedImageBitmap;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private final List<SensorData> sensorDataList = new ArrayList<>();
    private Bundle bundle = null;
    private Boolean newImageSelected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressWater = view.findViewById(R.id.progressWater);
        progressTemperature = view.findViewById(R.id.progressTemperature);
        progressLight = view.findViewById(R.id.progressLight);
        chosenTypeText = view.findViewById(R.id.chosenType);
        plantNameText = view.findViewById(R.id.plantNameHome);
        editButton = view.findViewById(R.id.editButton);
        refreshHomeData = view.findViewById(R.id.refreshHomeData);
        returnButton = view.findViewById(R.id.returnToSelectPlant);
        savedPlantPicture = view.findViewById(R.id.savedPlantPicture);
        textLastModified = view.findViewById(R.id.textLastModified);
        textWarning = view.findViewById(R.id.textWarning);


        // get User and Plant from mainactivity
        bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            user = (User) bundle.getSerializable("USER");
            plantNameText.setText(plant.getPlantName());
            if (plant.getImgRef() != null) {
                SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(getContext());
                String previouslyEncodedImage = shre.getString(plant.getImgRef(), "");
                if (!previouslyEncodedImage.equalsIgnoreCase("")) {
                    Bitmap bitmap = Base64Encoder.decodeImage(previouslyEncodedImage);
                    savedPlantPicture.setImageBitmap(bitmap);
                }
            }
            retrievePlantTypes();
            retrieveData();
            mqttConnectAndSubscribe();
        }

        refreshHomeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveData();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEditDialog();
            }
        });

        returnButton.setOnClickListener(v -> {
            Intent goToSelectPlantActivity = new Intent(getActivity(), SelectPlantActivity.class);
            goToSelectPlantActivity.putExtra("USER", user);
            startActivity(goToSelectPlantActivity);
            requireActivity().overridePendingTransition(0, 0);
        });

        return view;
    }

    public void createEditDialog() {
        dialogBuilder = new AlertDialog.Builder(getActivity());
        final View editDialogView = getLayoutInflater().inflate(R.layout.edit_popup, null);

        editTextName = editDialogView.findViewById(R.id.editTextName);
        cancelEditBtn = editDialogView.findViewById(R.id.cancelEditBtn);
        applyEditBtn = editDialogView.findViewById(R.id.applyEditBtn);
        plantTypesListView = editDialogView.findViewById(R.id.plant_types);
        chosenTypeText = editDialogView.findViewById(R.id.chosenType);
        BSelectImage = editDialogView.findViewById(R.id.BSelectImage);
        IVPreviewImage = editDialogView.findViewById(R.id.IVPreviewImage);
        IVPreviewImage.setImageURI(Uri.parse(plant.getImgRef()));

        //add previous data that can be edited
        if (bundle != null) {
            editTextName.setText(plant.getPlantName());
            editTextName.setAlpha(0.50F);
            chosenPlantTypeId = plant.getPlantType();
        }
        dialogBuilder.setView(editDialogView);
        dialog = dialogBuilder.create();
        dialog.show();

        cancelEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newImageSelected = false;
                dialog.dismiss();
            }
        });

        applyEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only update img uri if new image was selected
                if (validateNewName()) {
                    if (newImageSelected) {
                        savedPlantPicture.setImageBitmap(selectedImageBitmap);
                        String encodedImage = Base64Encoder.encodeImage(selectedImageBitmap);
                        String imageUri = saveImageToSharedPreferences(encodedImage);
                        updatePlantInfo(editTextName.getText().toString(), chosenPlantTypeId, imageUri);
                    } else {
                        updatePlantInfo(editTextName.getText().toString(), chosenPlantTypeId, plant.getImgRef());
                    }
                    changeProgressBarScale();
                    newImageSelected = false;
                    dialog.dismiss();
                }
            }
        });

        // handle the Choose Image button to trigger the image chooser function
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        expListView = plantTypesListView;
        preparePlantTypeListData();
        listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // childPosition also corresponds to the position in the plantTypeList
                chosenPlantTypeId = plantTypeList.get(childPosition).getId();
                // change header of expandable list
                listDataHeader.set(0, plantTypeList.get(childPosition).getName());
                //chosenTypeText.setText("Type: ");
                expListView.collapseGroup(groupPosition);
                return false;
            }
        });
    }

    boolean validateNewName() {
        if (editTextName.getText().toString().equals("")) {
            editTextName.setError("Not Allowed");
            return false;
        }
        return true;
    }


    // this function is triggered when the Select Image Button is clicked
    void imageChooser() {
        // create an instance of the intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // pass the constant to compare it with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // compare the resultCode with the SELECT_PICTURE constant
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), data.getData());
                IVPreviewImage.setImageBitmap(selectedImageBitmap);
                newImageSelected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePlantInfo(String plantName, int plantTypeId, String imgRef) {
        // /updateOwnedPlant/plantId/plantTypeId/imgBinary/nickname?token=logintoken
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = API_UPDATEPLANT + plant.getId() + "/" + plantTypeId + "/" + imgRef + "/" + plantName + "?token=" + user.getToken();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            plant.setPlantType(plantTypeId);
            plant.setPlantName(plantName);
            plant.setImgRef(imgRef);

            String editTextNameString = editTextName.getText().toString();
            plantNameText.setText(editTextNameString);
        }, error -> Log.e("Volley", error.getMessage()));

        queue.add(stringRequest);
    }

    private String saveImageToSharedPreferences(String encodedImage) {
        SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = shre.edit();
        String key = "image_" + user.getId() + "_" + plant.getId();
        edit.putString(key, encodedImage);
        edit.commit();

        return key;
    }

    private void retrieveData() {
        retrieveMeasurements(1, "Moisture", progressWater);
        retrieveMeasurements(1, "Temperature", progressTemperature);
        retrieveMeasurements(1, "Light", progressLight);
    }

    // measurementType = "Temperature", "Light" or "Moisture"
    private void retrieveMeasurements(int numberOfMeasurements, String measurementType, ProgressBar progressBar) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = API_GETMEASUREMENTS + measurementType + "/" + numberOfMeasurements + "/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            // parse response to SensorData
            SensorData sensorData = parseToSensorData(response);
            // update last timestamp
            textLastModified.setText("Last Modified: " + sensorData.getTimestamp());
            // display this SensorData on progressBar
            progressBar.setProgress((int) sensorData.getValue());
        }, error -> Log.e("Volley", error.getMessage()));
        queue.add(stringRequest);
    }

    // parse and return 1 SensorData
    private SensorData parseToSensorData(String response) {
        SensorData sensorData = new SensorData();
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = jsonArray.getJSONObject(i);
                sensorData.setId(tempObject.getInt("id"));
                sensorData.setType(tempObject.getString("type"));
                sensorData.setTimestamp(tempObject.getString("timestamp"));
                sensorData.setValue(tempObject.getDouble("value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensorData;
    }

    private void changeProgressBarScale() {
        progressTemperature.setMax((int) (getPlantTypeFromId(plant.getPlantType()).getIdealTemperature() * 2.0));
        progressWater.setMax((int) (getPlantTypeFromId(plant.getPlantType()).getIdealMoisture() * 2.0));
        progressLight.setMax((int) (getPlantTypeFromId(plant.getPlantType()).getIdealLight() * 2.0));
    }

    private void retrievePlantTypes() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = API_GETPLANTTYPES + user.getToken();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject tempObject = jsonArray.getJSONObject(i);

                    PlantType plantType = new PlantType();
                    plantType.setId(tempObject.getInt("id"));
                    plantType.setName(tempObject.getString("name"));
                    plantType.setIdealMoisture(tempObject.getDouble("idealMoisture"));
                    plantType.setIdealTemperature(tempObject.getDouble("idealTemperature"));
                    plantType.setIdealLight(tempObject.getDouble("idealLight"));
                    plantTypeList.add(plantType);
                }
                // if plantType is known, so are the ideal values and thus the scale can be set
                changeProgressBarScale();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.getMessage()));

        queue.add(stringRequest);
    }

    private void preparePlantTypeListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding child data
        listDataHeader.add(getPlantTypeFromId(plant.getPlantType()).getName());

        // Adding child data
        List<String> plantTypeNames = new ArrayList<>();
        for (int i = 0; i < plantTypeList.size(); i++) {
            plantTypeNames.add(plantTypeList.get(i).getName());
        }

        listDataChild.put(listDataHeader.get(0), plantTypeNames); // Header, Child data
    }

    private PlantType getPlantTypeFromId(int plantTypeId) {
        PlantType plant = new PlantType();
        for (int i = 0; i < plantTypeList.size(); i++) {
            if (plantTypeList.get(i).getId() == plantTypeId) {
                return plantTypeList.get(i);
            }
        }
        return plant;
    }

    public void mqttConnectAndSubscribe() {
        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client =
                new MqttAndroidClient(getContext(), MQTT_SERVER_URI,
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

    public void mqttSubscribe(MqttAndroidClient client) {
        int qos = 1;
        try {
            client.subscribe(MQTT_WARNING + plant.getDeviceId(), qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if (topic.compareTo(MQTT_WARNING + plant.getDeviceId()) == 0) {
                        String response = new String(message.getPayload());
                        // show warning only if there is one
                        Log.d("MQQT_RESPONSE", response);
                        System.out.println("\n\n\n\nMQTT = " + response);
                        if(response.compareTo("None") != 0){
                            textWarning.setVisibility(View.VISIBLE);
                            textWarning.setText("WARNING: " + response);
                        } else {
                            textWarning.setVisibility(View.GONE);
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
}