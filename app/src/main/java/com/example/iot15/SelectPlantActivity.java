package com.example.iot15;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.classes.Plant;
import com.example.iot15.classes.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectPlantActivity extends AppCompatActivity {
    private User user;

    ListView listView;
    List<Plant> listPlants;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_plant);

        // retrieve user from Login activity
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("USER");

        listView = findViewById(R.id.plant_listview);
        listPlants = new ArrayList<>();
        retrievePlants();

        adapter = new ArrayAdapter(SelectPlantActivity.this, android.R.layout.simple_list_item_1, listPlants);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Plant selectedPlant = (Plant) adapter.getItem(position);

                Intent goToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                goToMainActivity.putExtra("USER", user);
                goToMainActivity.putExtra("PLANT", selectedPlant);
                startActivity(goToMainActivity);
            }
        });
    }

    private void retrievePlants() {
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url="https://a21iot15.studev.groept.be/index.php/api/listOwnedPlants/" + user.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            addJSONtoPlantList(response);
        }, error -> listPlants.add(new Plant()));

        queue.add(stringRequest);
    }

    private void addJSONtoPlantList(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = jsonArray.getJSONObject(i);
                Plant plant = new Plant();
                plant.setId(tempObject.getInt("id"));
                plant.setUserId(tempObject.getInt("userId"));
                plant.setPlantType(tempObject.getInt("plantId"));
                plant.setPlantName(tempObject.getString("nickName"));
                plant.setImgBlob(tempObject.getString("img"));
                listPlants.add(plant);
                System.out.println(plant);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}