package com.example.iot15;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";
    public static final int GALLERY_INTENT_CALLED = 1;
    public static final int GALLERY_KITKAT_INTENT_CALLED = 2;

    private User user;
    private Plant plant;
    private List<PlantType> plantTypeList= new ArrayList<PlantType>();

    private ProgressBar progressWater;
    private ProgressBar progressTemperature;
    private ProgressBar progressLight;
    private TextView plantNameText;
    private ImageButton editButton;
    private ImageButton refreshHomeData;
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
    private int SELECT_PICTURE = 200; // constant to compare the activity result code
    private Uri selectedImageUri;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private List<SensorData> sensorDataList =new ArrayList<>();
    private Bundle bundle = null;
    private Boolean newImageSelected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressWater = (ProgressBar) view.findViewById(R.id.progressWater);
        progressTemperature = (ProgressBar) view.findViewById(R.id.progressTemperature);
        progressLight = (ProgressBar) view.findViewById(R.id.progressLight);
        chosenTypeText = (TextView) view.findViewById(R.id.chosenType);
        plantNameText = (TextView) view.findViewById(R.id.plantNameHome);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
        refreshHomeData = (ImageButton) view.findViewById(R.id.refreshHomeData);
        savedPlantPicture = (ImageView) view.findViewById(R.id.savedPlantPicture);
        textLastModified = (TextView) view.findViewById(R.id.textLastModified);
        textWarning = (TextView) view.findViewById(R.id.textWarning);


        // get User and Plant from mainactivity
        bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            System.out.println("\n\n\n" + plant.toString() + "\n\n\n");
            user = (User) bundle.getSerializable("USER");
            plantNameText.setText(plant.getPlantName());
            if(plant.getImgBlob() != null){
                savedPlantPicture.setImageURI(Uri.parse(plant.getImgBlob()));
            }
            //setImageFromUri();
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


        return view;
    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    public void createEditDialog(){
        dialogBuilder = new AlertDialog.Builder(getActivity());
        final View editDialogView = getLayoutInflater().inflate(R.layout.edit_popup, null);

        editTextName = (EditText) editDialogView.findViewById(R.id.editTextName);
        cancelEditBtn = (Button) editDialogView.findViewById(R.id.cancelEditBtn);
        applyEditBtn = (Button) editDialogView.findViewById(R.id.applyEditBtn);
        plantTypesListView = (ExpandableListView) editDialogView.findViewById(R.id.plant_types);
        chosenTypeText = (TextView) editDialogView.findViewById(R.id.chosenType);
        BSelectImage = editDialogView.findViewById(R.id.BSelectImage);
        IVPreviewImage = editDialogView.findViewById(R.id.IVPreviewImage);
        IVPreviewImage.setImageURI(Uri.parse(plant.getImgBlob()));

        //add previous data that can be edited
        if(bundle != null) {
            editTextName.setText(plant.getPlantName());
            editTextName.setAlpha(0.50F);
            chosenPlantTypeId = plant.getPlantType(); //TODO just changed
            // set plant type
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
                if(validateNewName()){
                    if(newImageSelected == true){
                        savedPlantPicture.setImageURI(selectedImageUri);
                        System.out.println("\n\n" + selectedImageUri.toString() + "\n\n");
                        updatePlantInfo(editTextName.getText().toString(), chosenPlantTypeId, selectedImageUri.toString());
                    }
                    else{
                        updatePlantInfo(editTextName.getText().toString(), chosenPlantTypeId, plant.getImgBlob());
                    }
                    changeProgressBar();
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
        prepareListData();
        listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {}
        });

        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {}
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
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


//    public void setImageFromUri(){
//        if (Build.VERSION.SDK_INT <19){
//            Intent intent = new Intent();
//            intent.setType("*/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(intent, GALLERY_INTENT_CALLED);
//        } else {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
//        }
//    }

    // this function is triggered when user selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // compare the resultCode with the SELECT_PICTURE constant
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                selectedImageUri = data.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContext().getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
                if (selectedImageUri != null) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                    newImageSelected = true;

                }
        }
//        if (resultCode == RESULT_OK && (requestCode == GALLERY_INTENT_CALLED || requestCode == GALLERY_KITKAT_INTENT_CALLED)) {
//            Uri originalUri = null;
//            if (Build.VERSION.SDK_INT < 19) {
//                originalUri = Uri.parse(plant.getImgBlob());
//            } else {
//                originalUri = Uri.parse(plant.getImgBlob());
//                final int takeFlags = data.getFlags()
//                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//                try {
//                    getActivity().getContentResolver().takePersistableUriPermission(originalUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                } catch (SecurityException e) {
//                    e.printStackTrace();
//                }
//            }
//            // when permission is given, set plant image
//            savedPlantPicture.setImageURI(originalUri);
//    }
    }


    private void updatePlantInfo(String plantName, int plantTypeId, String imgBlob){
       // /updateOwnedPlant/plantId/plantTypeId/imgBinary/nickname?token=logintoken
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/updateOwnedPlant/" + plant.getId() +"/" + plantTypeId + "/" + removeSlashesFromUri(imgBlob) + "/" + plantName +"?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, response -> {
            plant.setPlantType(plantTypeId);
            plant.setPlantName(plantName);
            plant.setImgBlob(imgBlob);

            String editTextNameString = editTextName.getText().toString();
            plantNameText.setText(editTextNameString);
        }, error -> System.out.println("Error: " + error));

        queue.add(stringRequest);
    }

    private String removeSlashesFromUri(String uri){
        String newUri = uri.replace('/', '_');
        return newUri;
    }

    private void retrieveData(){
        retrieveMeasurementsMoisture();
        retrieveMeasurementsLight();
        retrieveMeasurementsTemperature();
    }

    private void retrieveMeasurementsMoisture() {
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listMeasurementsMoisture/" + 1 +"/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            // parse response to SensorData
            SensorData dataWater = parseToSensorData(response);
            // update last timestamp
            textLastModified.setText("Last Modified: " + dataWater.getTimestamp());
            // display this SensorData on progressBar
            progressWater.setProgress((int)dataWater.getValue());
        }, error -> System.out.println("Error: " + error));
        queue.add(stringRequest);
    }

    private void retrieveMeasurementsLight() {
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listMeasurementsLight/" + 1 +"/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            // parse response to SensorData
            SensorData dataLight = parseToSensorData(response);

            // display this SensorData on progressBar
            progressLight.setProgress((int)dataLight.getValue());
        }, error -> System.out.println("Error: " + error));
        queue.add(stringRequest);
    }

    private void retrieveMeasurementsTemperature() {
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listMeasurementsTemperature/" + 1 +"/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            // parse response to SensorData
            SensorData dataTemperature = parseToSensorData(response);

            // display this SensorData on progressBar
            progressTemperature.setProgress((int)dataTemperature.getValue());
        }, error -> System.out.println("Error: " + error));
        queue.add(stringRequest);
    }

    // parse and return 1 SensorData
    private SensorData parseToSensorData(String response){
        SensorData sensorData = new SensorData();
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = jsonArray.getJSONObject(i);
                sensorData.setId(tempObject.getInt("id"));
                sensorData.setType(tempObject.getString("type"));
                sensorData.setTimestamp(tempObject.getString("timestamp"));
                sensorData.setValue(tempObject.getDouble("value"));
                System.out.println(sensorData.toString());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return sensorData;
    }

    private void changeProgressBar(){
        progressTemperature.setMax((int) (getPlantTypeFromId(plant.getPlantType()).getIdealTemperature() * 2.0));
        progressWater.setMax((int) (getPlantTypeFromId(plant.getPlantType()).getIdealMoisture() * 2.0));
        progressLight.setMax((int) (getPlantTypeFromId(plant.getPlantType()).getIdealLight() * 2.0));
    }

    private void retrievePlantTypes(){
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listPlants?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
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
                    System.out.println(plantType.toString());
                    plantTypeList.add(plantType);
                }
                // TODO maybe change this to another location
                // now it is needed here because it needs the different plantypes
                changeProgressBar();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }, error -> System.out.println("Error: " + error));

        queue.add(stringRequest);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add(getPlantTypeFromId(plant.getPlantType()).getName());

        // Adding child data
        List<String> plantTypeNames = new ArrayList<String>();
        for(int i = 0; i<plantTypeList.size();i++){
            plantTypeNames.add(plantTypeList.get(i).getName());
        }

        listDataChild.put(listDataHeader.get(0), plantTypeNames); // Header, Child data
    }

    private PlantType getPlantTypeFromId(int plantTypeId){
        PlantType plant = new PlantType();
        for(int i = 0; i<plantTypeList.size(); i++){
            if(plantTypeList.get(i).getId() == plantTypeId){
                return plantTypeList.get(i);
            }
        }
        return plant;
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

    public void mqttSubscribe(MqttAndroidClient client) {
        int qos = 1;
        try {
            client.subscribe("/EE5iot15/warnings/" + plant.getDeviceId(), qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(topic.compareTo("/EE5iot15/warnings/" + plant.getDeviceId()) == 0){
                        String response = new String(message.getPayload());
                        // show warnings somewhere
                        textWarning.setText("WARNING: " + response);
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