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
            retrieveData();
        }
        return view;
    }

    private void retrieveData(){
        retrieveMeasurements(NUMBER_OF_MEASUREMENT_TO_DISPLAY, "Moisture", sensorDataListWater, graphWater, waterColor, waterBackColor);
        retrieveMeasurements(NUMBER_OF_MEASUREMENT_TO_DISPLAY, "Temperature", sensorDataListTemperature, graphTemperature, temperatureColor, temperatureBackColor);
        retrieveMeasurements(NUMBER_OF_MEASUREMENT_TO_DISPLAY, "Light", sensorDataListLight, graphLight, lightColor, lightBackColor);
    }

    // measurementType = "Temperature", "Light" or "Moisture"
    private void retrieveMeasurements(int numberOfMeasurements, String measurementType, List<SensorData> sensorDataList,GraphView graphView, int graphColor, int backgroundColor) {
        RequestQueue queue= Volley.newRequestQueue(getContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listMeasurements" + measurementType + "/" + numberOfMeasurements +"/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            addToMeasurementList(response, sensorDataList, graphView, measurementType, graphColor, backgroundColor);
        }, error -> System.out.println("Error: " + error));
        queue.add(stringRequest);
    }

    // parse multiple SensorData and add to specified list
    private void addToMeasurementList(String response, List<SensorData> sensorDataList, GraphView graphView, String title, int graphColor, int backgroundColor){
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
            displayGraph(sensorDataList,graphView, title, graphColor, backgroundColor);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void displayGraph(List<SensorData> sensorDataList, GraphView graphView, String title, int graphColor, int backgroundColor) {
        // add water data to graph view
        DataPoint[] dataPoints = new DataPoint[sensorDataList.size()]; // declare an array of DataPoint objects with the same size as your list
        for (int i = 0; i < sensorDataList.size(); i++) {
            // add new DataPoint object to the array for each of your list entries
            dataPoints[i] = new DataPoint(i, (double) sensorDataList.get(i).getValue());
            System.out.println(dataPoints[i].toString());
        }
        LineGraphSeries<DataPoint> graphData = new LineGraphSeries<DataPoint>(dataPoints);
        // set layout for water graph
        graphData.setColor(graphColor);
        graphData.setDrawBackground(true);
        graphData.setBackgroundColor(backgroundColor);
        graphData.setDrawDataPoints(true);
        graphView.setTitle(title);
        graphView.setTitleColor(textColor);
        graphView.setTitleTextSize(50);
        graphView.addSeries(graphData);
    }
}