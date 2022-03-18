package com.example.iot15;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import android.app.Activity;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;

public class Types extends Fragment {
    public static final String TAG = "HomeFragment";


    private TextView textWater;
    private ProgressBar progressWater;
    private int waterProgressValue = 0;
    private TextView textTemperature;
    private ProgressBar progressTemperature;
    private int temperatureProgressValue = 50;
    private TextView textLight;
    private ProgressBar progressLight;
    private int lightProgressValue = 90;
    private TextView plantName;
    private ImageButton editButton;
    private TextView textLastModified;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button cancelEditBtn, applyEditBtn;
    private TextView plantTypeList;

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

        plantName = (TextView) view.findViewById(R.id.plantName);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
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
        final View editDialogView = getLayoutInflater().inflate(R.layout.finnn, null);

        cancelEditBtn = (Button) editDialogView.findViewById(R.id.cancelEditBtn);
        applyEditBtn = (Button) editDialogView.findViewById(R.id.applyEditBtn);
        plantTypeList = (TextView) editDialogView.findViewById(R.id.plantTypeList);

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
                dialog.dismiss();
            }
        });

        //doe iets met list
        prepareListData();
        listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);

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

        textWater.setText("Moisture");
        textTemperature.setText("Temperature");
        textLight.setText("Light Level");

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

        }, error -> plantName.setText("Data : Response Failed"));

        queue.add(stringRequest);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Plant type");

        // Adding child data
        List<String> plant_types = new ArrayList<String>();
        plant_types.add("Bromelia");
        plant_types.add("Pineapple Plant");
        plant_types.add("Cactus");
        plant_types.add("Aloë Vera");

        listDataChild.put(listDataHeader.get(0), plant_types); // Header, Child data
}