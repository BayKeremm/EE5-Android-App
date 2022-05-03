package com.example.iot15;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.iot15.classes.Plant;
import com.example.iot15.classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        // retrieve user from Login activity
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("USER");
        plant = (Plant) intent.getSerializableExtra("PLANT");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavMethod=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragment = null;
                Bundle bundle = new Bundle();
                bundle.putString("TEST", "test");
                bundle.putSerializable("USER", user);
                bundle.putSerializable("PLANT", plant);

                switch (item.getItemId())
                {
                    case R.id.home:
                        fragment= new HomeFragment();
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