package com.example.iot15;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.classes.Plant;
import com.example.iot15.classes.PlantType;
import com.example.iot15.classes.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectPlantActivity extends AppCompatActivity {
    private User user;

    ListView listView;
    List<Plant> listPlants;
    List<String>listNamePlants;
    ArrayAdapter adapter;
    Plant selectedPlantObject;
    private Button addPlantButton;
    private AlertDialog.Builder addNewPlantDialogBuilder;
    private AlertDialog dialog;
    private EditText editTextName;
    private EditText editDeviceID;
    private Button cancelEditBtn, applyEditBtn;
    private ExpandableListView plantTypesListView;
    private TextView chosenTypeText;
    private String type;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private List<PlantType> plantTypeList= new ArrayList<PlantType>();
    private Plant plant;
    private TextView plantNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_plant);

        // retrieve user from Login activity
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("USER");

        listView = findViewById(R.id.plant_listview);
        addPlantButton = findViewById(R.id.addPlantButton);
        listPlants = new ArrayList<>();
        listNamePlants = new ArrayList<>();
        retrievePlants();
        retrievePlantTypes();

        adapter = new ArrayAdapter(SelectPlantActivity.this, android.R.layout.simple_list_item_1, listNamePlants);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedPlantName = adapter.getItem(position).toString();
                for (int i = 0; i < listPlants.size(); i++) {
                    if( selectedPlantName == listPlants.get(i).getPlantName()){
                        selectedPlantObject = listPlants.get(i);
                    }
                }

                Intent goToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                goToMainActivity.putExtra("USER", user);
                goToMainActivity.putExtra("PLANT", selectedPlantObject);
                startActivity(goToMainActivity);
                overridePendingTransition(0, 0);
            }
        });

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddNewPlantDialog();
            }
        });
    }

    public void createAddNewPlantDialog(){
        addNewPlantDialogBuilder = new AlertDialog.Builder(this);
        final View editDialogView = getLayoutInflater().inflate(R.layout.add_plant_popup, null);

        editTextName = (EditText) editDialogView.findViewById(R.id.editTextName);
        cancelEditBtn = (Button) editDialogView.findViewById(R.id.cancelEditBtn);
        applyEditBtn = (Button) editDialogView.findViewById(R.id.applyEditBtn);
        plantTypesListView = (ExpandableListView) editDialogView.findViewById(R.id.plant_types);
        chosenTypeText = (TextView) editDialogView.findViewById(R.id.chosenType);
        editDeviceID = (EditText) editDialogView.findViewById(R.id.editDeviceID);

        addNewPlantDialogBuilder.setView(editDialogView);
        dialog = addNewPlantDialogBuilder.create();
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
                //TODO
                updatePlantInfo(editTextName.getText().toString(), plant.getPlantType(), Integer.parseInt(editDeviceID.getText().toString()));
                dialog.dismiss();
            }
        });

        expListView = plantTypesListView;
        prepareListData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {}
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {}
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
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
                listNamePlants.add(plant.getPlantName());
                System.out.println(plant);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
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

    private void retrievePlantTypes(){
        RequestQueue queue= Volley.newRequestQueue(this);
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

    private void updatePlantInfo(String plantName, int plantTypeId, int deviceID){
        // /updateOwnedPlant/plantId/plantTypeId/imgBinary/nickname?token=logintoken
        RequestQueue queue= Volley.newRequestQueue(this);
        String url="https://a21iot15.studev.groept.be/index.php/api/insertOwnedPlant/" + user.getId() +"/" + plantTypeId +"/" + deviceID + "/" + plantName +"?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, response -> {
            /*plant.setPlantType(plantTypeId);
            plant.setPlantName(plantName);
            plant.setDeviceId(deviceID);

            String editTextNameString = editTextName.getText().toString();
            plantNameText.setText(editTextNameString);*/
        }, error -> System.out.println("Error: " + error));

        queue.add(stringRequest);
    }
}