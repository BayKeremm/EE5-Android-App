package com.example.iot15;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.iot15.classes.Plant;
import com.example.iot15.classes.User;

public class GraphsFragment extends Fragment {

    private User user;
    private Plant plant;

    private TextView plantNameText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphs, container, false);

        plantNameText = (TextView) view.findViewById(R.id.plantNameGraphs);

        // get User and Plant from mainactivity
        // TODO this doesn't always work
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            plant = (Plant) bundle.getSerializable("PLANT");
            user = (User) bundle.getSerializable("USER");
            System.out.println("\n\n\n user = " + user + "\n\n\n");
            plantNameText.setText(plant.getPlantName());
        }

        return view;
    }
}