package com.example.iot15;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.classes.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private User user;

    EditText etUsername, etPassword;

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
        Intent goToSignup = new Intent(this, SignupActivity.class);
        startActivity(goToSignup);
        overridePendingTransition(0, 0);
    }

    private void checkPassword(String username, String password) {
        RequestQueue queue= Volley.newRequestQueue(this);
        String url="https://a21iot15.studev.groept.be/index.php/api/login/" + username+ "/" +password;
        System.out.println(url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject responseJSON = new JSONObject(response);
                if(responseJSON.getString("token") != null){
                    Intent goSelectPlantActivity = new Intent(getApplicationContext(), SelectPlantActivity.class);
                    user = new User(responseJSON.getInt("userId"), username, responseJSON.getString("token"));
                    goSelectPlantActivity.putExtra("USER", user);
                    startActivity(goSelectPlantActivity);
                    overridePendingTransition(0, 0);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> System.out.println("error"));

        queue.add(stringRequest);
    }

    public void goFragmentHomeTEST(View v) {
        Intent goToFragmentHomeTEST = new Intent(this, MainActivity.class);
        startActivity(goToFragmentHomeTEST);
        overridePendingTransition(0, 0);
    }
}
