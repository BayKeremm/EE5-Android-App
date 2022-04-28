package com.example.iot15;

import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;

public class LoginActivity extends AppCompatActivity {

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
                JSONArray responseJSON = new JSONArray(response);
            }
            catch (JSONException e) {
                e.printStackTrace();
                if(e.toString().contains("token")){
                    Intent goToFragmentHome = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(goToFragmentHome);
                    overridePendingTransition(0, 0);
                }
                else if(e.toString().contains("Declined")) {
                    Toast.makeText(getApplicationContext(),"Not allowed", Toast.LENGTH_SHORT).show();
                }
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
