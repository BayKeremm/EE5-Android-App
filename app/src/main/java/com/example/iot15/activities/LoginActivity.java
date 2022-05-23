package com.example.iot15.activities;

import static com.example.iot15.classes.Values.API_LOGIN;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.R;
import com.example.iot15.classes.User;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private User user;

    EditText etUsername, etPassword;

    SharedPreferences onBoardingScreen;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login_screen);

        viewInitializations();

    }

    void viewInitializations() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
    }

    // Checking if the input in form is valid
    boolean validateInput() {

        if (etUsername.getText().toString().equals("")) {
            etUsername.setError("Please Enter Username");
            return false;
        }
        if (etPassword.getText().toString().equals("")) {
            etPassword.setError("Please Enter Password");
            return false;
        }

        return true;
    }

    // Hook Click Event

    public void performLogin(View v) {
        if (validateInput()) {

            // Input is valid, here send data to your server

            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            checkPassword(username, password);

            //Toast.makeText(this,"Login Success",Toast.LENGTH_SHORT).show();
        }
    }

    public void goSignup(View v) {
        // Open your SignUp Activity if the user wants to signup
        onBoardingScreen = getSharedPreferences("onBoardingScreen", MODE_PRIVATE);

        boolean isFirst = onBoardingScreen.getBoolean("firstTimeUser", true);

        if (isFirst) {
            SharedPreferences.Editor editor = onBoardingScreen.edit();
            editor.putBoolean("firstTimeUser", false);
            editor.commit();

            Intent goToOnBoarding = new Intent(this, OnBoardingActivity.class);
            startActivity(goToOnBoarding);
        } else {

            Intent goToSignup = new Intent(this, SignupActivity.class);
            startActivity(goToSignup);
        }
        overridePendingTransition(0, 0);
        finish();
    }

    private void checkPassword(String username, String password) {
        RequestQueue queue= Volley.newRequestQueue(this);
        String url= API_LOGIN + username+ "/" +password;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject responseJSON = new JSONObject(response);
                if(responseJSON.getString("token") != null){


                    Intent goToSelectPlantActivity = new Intent(getApplicationContext(), SelectPlantActivity.class);
                    user = new User(responseJSON.getInt("userId"), username, responseJSON.getString("token"));
                    goToSelectPlantActivity.putExtra("USER", user);
                    startActivity(goToSelectPlantActivity);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.e("Volley", error.getMessage()));

        queue.add(stringRequest);
    }
}
