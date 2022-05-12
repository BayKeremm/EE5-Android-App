package com.example.iot15;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.classes.Plant;
import com.example.iot15.classes.SensorData;
import com.example.iot15.classes.User;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GraphsFragment extends Fragment {

    private User user;
    private Plant plant;
    public static final int NUMBER_OF_MEASUREMENT_TO_DISPLAY = 10;

    private TextView plantNameText;
    private GraphView graphWater;
    private GraphView graphTemperature;
    private GraphView graphLight;

    private List<SensorData> sensorDataListWater =new ArrayList<>();
    private List<SensorData> sensorDataListTemperature =new ArrayList<>();
    private List<SensorData> sensorDataListLight =new ArrayList<>();

    int textColor = Color.parseColor("#403F3F");
    int waterColor = Color.parseColor("#03A9F4");
    int waterBackColor = Color.parseColor("#8003A9F4");
    int temperatureColor = Color.parseColor("#F44336");
    int temperatureBackColor = Color.parseColor("#80F44336");
    int lightColor = Color.parseColor("#FFC107");
    int lightBackColor = Color.parseColor("#80FFC107");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphs, container, false);

        plantNameText = (TextView) view.findViewById(R.id.plantNameGraphs);
        graphWater = (GraphView) view.findViewById(R.id.GraphView1);
        graphTemperature = (GraphView) view.findViewById(R.id.GraphView2);
        graphLight = (GraphView) view.findViewById(R.id.GraphView3);

        // get User and Plant from mainactivity
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            user = (User) bundle.getSerializable("USER");
            plantNameText.setText(plant.getPlantName());
        }
        if(bundle != null){
            retrieveData();
        }
        return view;
    }

    private void retrieveData(){
        retrieveMeasurements(NUMBER_OF_MEASUREMENT_TO_DISPLAY, "Moisture", sensorDataListWater);
        retrieveMeasurements(NUMBER_OF_MEASUREMENT_TO_DISPLAY, "Temperature", sensorDataListTemperature);
        retrieveMeasurements(NUMBER_OF_MEASUREMENT_TO_DISPLAY, "Light", sensorDataListLight);
    }

    // measurementType = "Temperature", "Light" or "Moisture"
    private void retrieveMeasurements(int numberOfMeasurements, String measurementType, List<SensorData> sensorDataList) {
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listMeasurements" + measurementType + "/" + numberOfMeasurements +"/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            addToMeasurementArray(response, sensorDataList);
        }, error -> System.out.println("Error: " + error));
        queue.add(stringRequest);
    }

    // parse multiple SensorData and add to specified list
    private void addToMeasurementArray(String response, List<SensorData> sensorDataList){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = jsonArray.getJSONObject(i);
                SensorData sensorData = new SensorData();
                sensorData.setId(tempObject.getInt("id"));
                sensorData.setType(tempObject.getString("type"));
                sensorData.setTimestamp(tempObject.getString("timestamp"));
                sensorData.setValue(tempObject.getDouble("value"));
                sensorDataList.add(sensorData);
                System.out.println(sensorData);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        displayGraphs();
    }

    private void displayGraphs(){
        // add water data to graph view
        DataPoint[] dataPointsWater = new DataPoint[sensorDataListWater.size()]; // declare an array of DataPoint objects with the same size as your list
        for (int i = 0; i < sensorDataListWater.size(); i++) {
            // add new DataPoint object to the array for each of your list entries
            dataPointsWater[i] = new DataPoint(i, (double) sensorDataListWater.get(i).getValue());
            System.out.println(dataPointsWater[i].toString());
        }
        LineGraphSeries<DataPoint> graphDataWater = new LineGraphSeries<DataPoint>(dataPointsWater);
        // set layout for water graph
        graphDataWater.setColor(waterColor);
        graphDataWater.setDrawBackground(true);
        graphDataWater.setBackgroundColor(waterBackColor);
        graphDataWater.setDrawDataPoints(true);
        graphWater.setTitle("Water");
        graphWater.setTitleColor(textColor);
        graphWater.setTitleTextSize(50);
        graphWater.addSeries(graphDataWater);

        // add temperature data to graph view
        DataPoint[] dataPointsTemperature = new DataPoint[sensorDataListTemperature.size()];
        for (int i = 0; i < sensorDataListTemperature.size(); i++) {
            dataPointsTemperature[i] = new DataPoint(i, sensorDataListTemperature.get(i).getValue());
        }
        LineGraphSeries<DataPoint> graphDataTemperature = new LineGraphSeries<DataPoint>(dataPointsTemperature);
        // set layout for temperature graph
        graphDataTemperature.setColor(temperatureColor);
        graphDataTemperature.setDrawBackground(true);
        graphDataTemperature.setBackgroundColor(temperatureBackColor);
        graphDataTemperature.setDrawDataPoints(true);
        graphTemperature.setTitle("Temperature");
        graphTemperature.setTitleColor(textColor);
        graphTemperature.setTitleTextSize(50);
        graphTemperature.addSeries(graphDataTemperature);

        // add light data to graph view
        DataPoint[] dataPointsLight = new DataPoint[sensorDataListLight.size()];
        for (int i = 0; i < sensorDataListLight.size(); i++) {
            dataPointsLight[i] = new DataPoint(i, sensorDataListLight.get(i).getValue());
        }
        LineGraphSeries<DataPoint> graphDataLight = new LineGraphSeries<DataPoint>(dataPointsLight);
        // set layout for light graph
        graphDataLight.setColor(lightColor);
        graphDataLight.setDrawBackground(true);
        graphDataLight.setBackgroundColor(lightBackColor);
        graphDataLight.setDrawDataPoints(true);
        graphLight.setTitle("Light");
        graphLight.setTitleColor(textColor);
        graphLight.setTitleTextSize(50);
        graphLight.addSeries(graphDataLight);

    }
}