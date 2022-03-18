package com.example.iot15;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;

public class Plants extends Activity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plants);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.plant_types);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collapsed listener
        expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Bromelia");
        listDataHeader.add("Pineapple plant");
        listDataHeader.add("Cactus");
        listDataHeader.add("Aloë vera");

        // Adding child data
        List<String> Bromelia = new ArrayList<String>();
        Bromelia.add("water: once a week");
        Bromelia.add("no direct sunlight");
        Bromelia.add("not toxic");
        // Adding child data
        List<String> Pineapple_plant = new ArrayList<String>();
        Pineapple_plant.add("water: once a week");
        Pineapple_plant.add("no direct sunlight");
        Pineapple_plant.add("not toxic");
        // Adding child data
        List<String> Cactus = new ArrayList<String>();
        Cactus.add("water: once every 3 months");
        Cactus.add("no light preference");
        Cactus.add("toxic");
        // Adding child data
        List<String> Aloë_vera = new ArrayList<String>();
        Cactus.add("water: once every month");
        Cactus.add("shadow");
        Cactus.add("toxic");


        listDataChild.put(listDataHeader.get(0), Bromelia); // Header, Child data
        listDataChild.put(listDataHeader.get(1), Pineapple_plant);
        listDataChild.put(listDataHeader.get(2), Cactus);
        listDataChild.put(listDataHeader.get(3), Aloë_vera);
    }
}