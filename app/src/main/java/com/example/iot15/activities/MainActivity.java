package com.example.iot15.activities;

import static com.example.iot15.classes.Values.API_GETDEVICEID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.R;
import com.example.iot15.classes.Plant;
import com.example.iot15.classes.User;
import com.example.iot15.fragments.GraphsFragment;
import com.example.iot15.fragments.HomeFragment;
import com.example.iot15.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private User user;
    private Plant plant;

    @Override
    public Intent getIntent() {
        return super.getIntent();
    }

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        bottomNavigationView=findViewById(R.id.bottomMenu);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavMethod);

        // retrieve user and plant
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("USER");
        plant = (Plant) intent.getSerializableExtra("PLANT");

        retrieveDeviceId();

        Bundle bundle = new Bundle();
        bundle.putSerializable("USER", user);
        bundle.putSerializable("PLANT", plant);
        Fragment initialHomeFragment = new HomeFragment();
        initialHomeFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, initialHomeFragment).commit();
    }

    private void retrieveDeviceId() {
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        String url= API_GETDEVICEID + plant.getId() + "?token=" + user.getToken();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                plant.setDeviceId(jsonObject.getInt("deviceId"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        queue.add(stringRequest);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavMethod=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragment = null;
                Bundle bundle = new Bundle();
                bundle.putSerializable("USER", user);
                bundle.putSerializable("PLANT", plant);

                switch (item.getItemId())
                {
                    case R.id.home:
                        fragment= new HomeFragment();
                        fragment.setArguments(bundle);
                        break;

                    case R.id.graphs:
                        fragment= new GraphsFragment();
                        fragment.setArguments(bundle);
                        break;

                    case R.id.settings:
                        fragment= new SettingsFragment();
                        fragment.setArguments(bundle);
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
    };
}