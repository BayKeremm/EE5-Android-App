package com.example.iot15;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.iot15.classes.Plant;
import com.example.iot15.classes.User;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphsFragment extends Fragment {

    private User user;
    private Plant plant;

    private TextView plantNameText;
    private GraphView graphView1;
    private GraphView graphView2;
    private GraphView graphView3;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphs, container, false);

        plantNameText = (TextView) view.findViewById(R.id.plantNameGraphs);
        graphView1 = (GraphView) view.findViewById(R.id.GraphView1);
        graphView2 = (GraphView) view.findViewById(R.id.GraphView2);
        graphView3 = (GraphView) view.findViewById(R.id.GraphView3);

        // get User and Plant from mainactivity
        // TODO this doesn't always work
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            user = (User) bundle.getSerializable("USER");
            System.out.println("\n\n\n user = " + user + "\n\n\n");
            plantNameText.setText(plant.getPlantName());
        }

        int textColor = Color.parseColor("#403F3F");
        int waterColor = Color.parseColor("#03A9F4");
        int waterBackColor = Color.parseColor("#8003A9F4");
        int temperatureColor = Color.parseColor("#F44336");
        int temperatureBackColor = Color.parseColor("#80F44336");
        int lightColor = Color.parseColor("#FFC107");
        int lightBackColor = Color.parseColor("#80FFC107");

        // add water data to graph view
        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(new DataPoint[]{
                // add each point on x and y axis
                new DataPoint(0, 50),
                new DataPoint(1, 30),
                new DataPoint(2, 40),
                new DataPoint(3, 90),
                new DataPoint(4, 60),
                new DataPoint(5, 30),
                new DataPoint(6, 60),
                new DataPoint(7, 100),
                new DataPoint(8, 61),
                new DataPoint(9, 84),
                new DataPoint(10, 25)
        });
        series1.setColor(waterColor);
        series1.setDrawBackground(true);
        series1.setBackgroundColor(waterBackColor);
        series1.setDrawDataPoints(true);
        graphView1.setTitle("Water");
        graphView1.setTitleColor(textColor);
        graphView1.setTitleTextSize(50);
        graphView1.addSeries(series1);

        // add temperature data to graph view
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(new DataPoint[]{
                // add each point on x and y axis
                new DataPoint(0, 25),
                new DataPoint(1, 29),
                new DataPoint(2, 24),
                new DataPoint(3, 19),
                new DataPoint(4, 26),
                new DataPoint(5, 23),
                new DataPoint(6, 26),
                new DataPoint(7, 18),
                new DataPoint(8, 20),
                new DataPoint(9, 28),
                new DataPoint(10, 25)
        });
        series2.setColor(temperatureColor);
        series2.setDrawBackground(true);
        series2.setBackgroundColor(temperatureBackColor);
        series2.setDrawDataPoints(true);
        graphView2.setTitle("Temperature");
        graphView2.setTitleColor(textColor);
        graphView2.setTitleTextSize(50);
        graphView2.addSeries(series2);

        // add light data to graph view
        LineGraphSeries<DataPoint> series3 = new LineGraphSeries<DataPoint>(new DataPoint[]{
                // add each point on x and y axis
                new DataPoint(0, 50),
                new DataPoint(1, 30),
                new DataPoint(2, 40),
                new DataPoint(3, 90),
                new DataPoint(4, 60),
                new DataPoint(5, 30),
                new DataPoint(6, 60),
                new DataPoint(7, 100),
                new DataPoint(8, 61),
                new DataPoint(9, 84),
                new DataPoint(10, 25)
        });
        series3.setColor(lightColor);
        series3.setDrawBackground(true);
        series3.setBackgroundColor(lightBackColor);
        series3.setDrawDataPoints(true);
        graphView3.setTitle("Light");
        graphView3.setTitleColor(textColor);
        graphView3.setTitleTextSize(50);
        graphView3.addSeries(series3);

        return view;
    }
}