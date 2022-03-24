package com.example.iot15;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";


    private TextView textWater;
    private ProgressBar progressWater;
    private int waterProgressValue = 10;
    private TextView textTemperature;
    private ProgressBar progressTemperature;
    private int temperatureProgressValue = 50;
    private TextView textLight;
    private ProgressBar progressLight;
    private int lightProgressValue = 90;
    private TextView plantName;
    private ImageButton editButton;
    private TextView textLastModified;
    private ImageView savedPlantPicture;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText editTextName;
    private Button cancelEditBtn, applyEditBtn;
    private TextView plantTypeList;
    private ExpandableListView plantTypes;
    private TextView chosenType;
    private String type;
    // One Button
    private Button BSelectImage;
    // One Preview Image
    private ImageView IVPreviewImage;
    // constant to compare the activity result code
    private int SELECT_PICTURE = 200;
    private Uri selectedImageUri;

    private List<SensorData> sensorDataList =new ArrayList<>();

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textWater = (TextView) view.findViewById(R.id.textWater);
        progressWater = (ProgressBar) view.findViewById(R.id.progressWater);
        progressWater.setProgress(waterProgressValue);
        textTemperature = (TextView) view.findViewById(R.id.textTemperature);
        progressTemperature = (ProgressBar) view.findViewById(R.id.progressTemperature);
        progressTemperature.setProgress(temperatureProgressValue);
        textLight = (TextView) view.findViewById(R.id.textLight);
        progressLight = (ProgressBar) view.findViewById(R.id.progressLight);
        progressLight.setProgress(lightProgressValue);
        chosenType = (TextView) view.findViewById(R.id.chosenType);
        plantName = (TextView) view.findViewById(R.id.plantName);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
        savedPlantPicture = (ImageView) view.findViewById(R.id.savedPlantPicture);

        double BromeliaValues[] = new double[]{1.8,1.5,2.2}; //moisture, temperature, LDR
        double PineappleValues[] = new double[]{1.9,1.5,2.2};
        double CactusValues[] = new double[]{2.2,2.5,2.8};
        double AloeVeraValues[] = new double[]{2.1,1.8,2.2};

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEditDialog();
            }
        });

        textLastModified = (TextView) view.findViewById(R.id.textLastModified);

        retrieveData();
        return view;
    }

    public void createEditDialog(){
        dialogBuilder = new AlertDialog.Builder(getActivity());
        final View editDialogView = getLayoutInflater().inflate(R.layout.edit_popup, null);

        editTextName = (EditText) editDialogView.findViewById(R.id.editTextName);
        cancelEditBtn = (Button) editDialogView.findViewById(R.id.cancelEditBtn);
        applyEditBtn = (Button) editDialogView.findViewById(R.id.applyEditBtn);
        plantTypes = (ExpandableListView) editDialogView.findViewById(R.id.plant_types);
        chosenType = (TextView) editDialogView.findViewById(R.id.chosenType);
        BSelectImage = editDialogView.findViewById(R.id.BSelectImage);
        IVPreviewImage = editDialogView.findViewById(R.id.IVPreviewImage);

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
                savedPlantPicture.setImageURI(selectedImageUri);
                String editTextNameString = editTextName.getText().toString();
                plantName.setText(editTextNameString);
                dialog.dismiss();
            }
        });

        // handle the Choose Image button to trigger the image chooser function
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });


        expListView = plantTypes;
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
                chosenType.setText("Type: " + type);
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
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void addToArray(String response){
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

    private void addRecentData(){
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
        textLastModified.setText("Last Modified: " + dataWater.getTimestamp());
        progressWater.setProgress((int)dataWater.getValue());
        progressTemperature.setProgress((int) dataTemperature.getValue());
        progressLight.setProgress((int) dataLight.getValue());

    }

    private void retrieveData() {
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://studev.groept.be/api/a21iot15/select_top100/";
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            addToArray(response);
            addRecentData();

        }, error -> plantName.setText("Make new profile:"));

        queue.add(stringRequest);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Choose");

        // Adding child data
        List<String> plant_types = new ArrayList<String>();
        plant_types.add("Bromelia");
        plant_types.add("Pineapple");
        plant_types.add("Cactus");
        plant_types.add("Aloë Vera");

        listDataChild.put(listDataHeader.get(0), plant_types); // Header, Child data
    }
}