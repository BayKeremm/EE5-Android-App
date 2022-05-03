package com.example.iot15;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";

    private User user;
    private Plant plant;
    private List<PlantType> plantTypeList= new ArrayList<PlantType>();

    private ProgressBar progressWater;
    private int waterProgressValue = 10;
    private ProgressBar progressTemperature;
    private int temperatureProgressValue = 50;
    private ProgressBar progressLight;
    private int lightProgressValue = 90;
    private TextView plantNameText;
    private ImageButton editButton;
    private TextView textLastModified;
    private ImageView savedPlantPicture;

    // plant edit dialog
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText editTextName;
    private Button cancelEditBtn, applyEditBtn;
    //private TextView plantTypeList;
    private ExpandableListView plantTypesListView;
    private TextView chosenTypeText;
    private String type;
    private Button BSelectImage;
    private ImageView IVPreviewImage;
    private int SELECT_PICTURE = 200; // constant to compare the activity result code
    private Uri selectedImageUri;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private List<SensorData> sensorDataList =new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressWater = (ProgressBar) view.findViewById(R.id.progressWater);
        progressWater.setProgress(waterProgressValue);
        progressTemperature = (ProgressBar) view.findViewById(R.id.progressTemperature);
        progressTemperature.setProgress(temperatureProgressValue);
        progressLight = (ProgressBar) view.findViewById(R.id.progressLight);
        progressLight.setProgress(lightProgressValue);
        chosenTypeText = (TextView) view.findViewById(R.id.chosenType);
        plantNameText = (TextView) view.findViewById(R.id.plantName);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
        savedPlantPicture = (ImageView) view.findViewById(R.id.savedPlantPicture);
        textLastModified = (TextView) view.findViewById(R.id.textLastModified);

        // get User and Plant from mainactivity
        // TODO this doesn't always work
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            user = (User) bundle.getSerializable("USER");
            System.out.println("\n\n\n user = " + user + "\n\n\n");
            plantNameText.setText(plant.getPlantName());
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEditDialog();
            }
        });

        if(bundle != null){
            retrieveData();
        }

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
        IVPreviewImage.setImageBitmap(StringToBitMap(plant.getImgBlob()));

        dialogBuilder.setView(editDialogView);
        dialog = dialogBuilder.create();
        dialog.show();

        cancelEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        applyEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TEMP
                // TODO
                try {
                    savedPlantPicture.setImageBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri));
                    System.out.println(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // TEMP


                //savedPlantPicture.setImageURI(selectedImageUri);
                dialog.dismiss();
                updatePlantInfo(editTextName.getText().toString(), plant.getPlantType(), String.valueOf(imageUriToBlob(selectedImageUri)));
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
                //chosenType.setText("here database info");
                type = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                chosenTypeText.setText("Type: " + type);
                //send data to database
                expListView.collapseGroup(groupPosition);
                return false;
            }
        });
    }

    // this function is triggered when the Select Image Button is clicked
    void imageChooser() {
        // create an instance of the intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // compare the resultCode with the SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                }
            }
        }
    }


    private void updatePlantInfo(String plantName, int plantTypeId, String imgBlob){
       // /updateOwnedPlant/plantId/plantTypeId/imgBinary/nickname?token=logintoken
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/updateOwnedPlant/" + plant.getId() +"/" + plantTypeId + "/" + imgBlob + "/" + plantName +"?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, response -> {
            plant.setPlantType(plantTypeId);
            plant.setPlantName(plantName);
            plant.setImgBlob(imgBlob);

            String editTextNameString = editTextName.getText().toString();
            plantNameText.setText(editTextNameString);
        }, error -> System.out.println("Error: " + error));

        queue.add(stringRequest);
    }

    private byte[] imageUriToBlob(Uri uri){
        // turn image URI into a bitmap for database
        byte[] bArray = null;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bArray = bos.toByteArray();

            System.out.println("\n\n bit array for image:\n" + bArray + "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bArray;
    }

    private void addToMeasurementArray(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = jsonArray.getJSONObject(i);
                SensorData sensorData = new SensorData();
                sensorData.setId(tempObject.getInt("id"));
                sensorData.setType(tempObject.getString("type"));
                sensorData.setTimestamp(tempObject.getInt("timestamp"));
                sensorData.setValue(tempObject.getDouble("value"));
                System.out.println(sensorData.toString());
                sensorDataList.add(sensorData);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void displayRecentMeasurements(){
        SensorData dataWater = new SensorData();
        SensorData dataTemperature = new SensorData();
        SensorData dataLight = new SensorData();
        // check for most recent data for each sensor
        for(int i = 0; i<sensorDataList.size();i++){
            if(sensorDataList.get(i).getType().equals("Moisture")){
                if(sensorDataList.get(i).getId() > dataWater.getId()){
                    dataWater = sensorDataList.get(i);
                }
            }
        }
        for(int i = 0; i<sensorDataList.size();i++){
            if(sensorDataList.get(i).getType().equals("Temperature")){
                if(sensorDataList.get(i).getId() > dataTemperature.getId()){
                    dataTemperature = sensorDataList.get(i);
                }
            }
        }
        for(int i = 0; i<sensorDataList.size();i++){
            if(sensorDataList.get(i).getType().equals("Light")){
                if(sensorDataList.get(i).getId() > dataLight.getId()){
                    dataLight = sensorDataList.get(i);
                }
            }
        }

        // modify progress bars and last modified according to recieved data
        if(dataWater.getTimestamp() != 0){
            textLastModified.setText("Last Modified: " + dataWater.getTimestamp());
        } else {
            textLastModified.setText("No new data");
        }
        progressWater.setProgress((int)dataWater.getValue());
        progressTemperature.setProgress((int) dataTemperature.getValue());
        progressLight.setProgress((int) dataLight.getValue());
    }

    private void retrieveData(){
        retrievePlantTypes();
        retrieveMeasurements();
    }

    private void retrieveMeasurements() {
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listMeasurements/" + 12 +"/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            addToMeasurementArray(response);
            displayRecentMeasurements();

        }, error -> System.out.println("Error: " + error));

        queue.add(stringRequest);
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
        listDataHeader.add("Choose");

        // Adding child data
        List<String> plantTypeNames = new ArrayList<String>();
        for(int i = 0; i<plantTypeList.size();i++){
            plantTypeNames.add(plantTypeList.get(i).getName());
        }

        listDataChild.put(listDataHeader.get(0), plantTypeNames); // Header, Child data
    }
}