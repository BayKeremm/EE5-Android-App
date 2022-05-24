package com.example.iot15.fragments;

import static com.example.iot15.classes.Values.API_GETMEASUREMENTS;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.R;
import com.example.iot15.classes.Plant;
import com.example.iot15.classes.SensorData;
import com.example.iot15.classes.User;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GraphsFragment extends Fragment {

    private User user;
    private Plant plant;
    public static final int NUMBER_OF_MEASUREMENTS_TO_DISPLAY = 5;
    public static final int TIME_INTERVAL_MEASUREMENTS = 2;

    private TextView plantNameText;
    private GraphView graphWater;
    private GraphView graphTemperature;
    private GraphView graphLight;
    private ImageButton refreshBtn;

    private final List<SensorData> sensorDataListWater = new ArrayList<>();
    private final List<SensorData> sensorDataListTemperature = new ArrayList<>();
    private final List<SensorData> sensorDataListLight = new ArrayList<>();

    int textColor = Color.parseColor("#403F3F");
    int waterColor = Color.parseColor("#03A9F4");
    int waterBackColor = Color.parseColor("#3003A9F4");
    int temperatureColor = Color.parseColor("#F44336");
    int temperatureBackColor = Color.parseColor("#30F44336");
    int lightColor = Color.parseColor("#FFC107");
    int lightBackColor = Color.parseColor("#30FFC107");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphs, container, false);

        plantNameText = view.findViewById(R.id.plantNameGraphs);
        graphWater = view.findViewById(R.id.GraphView1);
        graphTemperature = view.findViewById(R.id.GraphView2);
        graphLight = view.findViewById(R.id.GraphView3);
        refreshBtn = view.findViewById(R.id.refreshBtn);

        // get User and Plant from mainactivity
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            user = (User) bundle.getSerializable("USER");
            plantNameText.setText(plant.getPlantName());
            retrieveData();
        }

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bundle != null) {
                    retrieveData();
                }
            }
        });
        return view;
    }

    private void retrieveData() {
        retrieveMeasurements(NUMBER_OF_MEASUREMENTS_TO_DISPLAY, "Moisture", "%", sensorDataListWater, graphWater, waterColor, waterBackColor);
        retrieveMeasurements(NUMBER_OF_MEASUREMENTS_TO_DISPLAY, "Temperature", "°C", sensorDataListTemperature, graphTemperature, temperatureColor, temperatureBackColor);
        retrieveMeasurements(NUMBER_OF_MEASUREMENTS_TO_DISPLAY, "Light", "%", sensorDataListLight, graphLight, lightColor, lightBackColor);
    }

    // measurementType = "Temperature", "Light" or "Moisture"
    private void retrieveMeasurements(int numberOfMeasurements, String measurementType, String unit, List<SensorData> sensorDataList, GraphView graphView, int graphColor, int backgroundColor) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = API_GETMEASUREMENTS + measurementType + "/" + numberOfMeasurements + "/" + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            addToMeasurementList(response, sensorDataList, graphView, measurementType, unit, graphColor, backgroundColor);
        }, error -> Log.e("Volley", error.toString()));
        queue.add(stringRequest);
    }

    // parse multiple SensorData and add to specified list
    private void addToMeasurementList(String response, List<SensorData> sensorDataList, GraphView graphView, String title, String unit, int graphColor, int backgroundColor) {
        try {
            sensorDataList.clear();
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = jsonArray.getJSONObject(i);
                SensorData sensorData = new SensorData();
                sensorData.setId(tempObject.getInt("id"));
                sensorData.setType(tempObject.getString("type"));
                sensorData.setTimestamp(tempObject.getString("timestamp"));
                sensorData.setValue(tempObject.getDouble("value"));
                sensorDataList.add(sensorData);
            }
            displayGraph(sensorDataList, graphView, title, unit, graphColor, backgroundColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayGraph(List<SensorData> sensorDataList, GraphView graphView, String title, String unit, int graphColor, int backgroundColor) {
        // add data to graph view
        DataPoint[] dataPoints = new DataPoint[sensorDataList.size()]; // declare an array of DataPoint objects with the same size as your list
        for (int i = 0; i < sensorDataList.size(); i++) {
            // add new DataPoint object to the array for each of your list entries
            //dataPoints[i] = new DataPoint(i, sensorDataList.get(i).getValue()); // original
            //TODO temp:
            dataPoints[i] = new DataPoint(-(sensorDataList.size()-1-i)*TIME_INTERVAL_MEASUREMENTS, sensorDataList.get(i).getValue());
        }
        LineGraphSeries<DataPoint> graphData = new LineGraphSeries<>(dataPoints);
        // set layout + add data points graph
        graphView.removeAllSeries();

        for(int i = 0; i<dataPoints.length;i++){
            System.out.println("Datapoint: " + "test");
        }

        title += " (" + unit + ")";
        graphView.setTitle(title);
        graphView.setTitleColor(textColor);
        graphView.setTitleTextSize(50);

        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Minutes");

        graphView.addSeries(graphData);

        graphData.setColor(graphColor);
        graphData.setBackgroundColor(backgroundColor);
        graphData.setDrawBackground(true);
        graphData.setDrawDataPoints(true);

    }
}