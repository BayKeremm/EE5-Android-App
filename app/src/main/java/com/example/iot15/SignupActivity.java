package com.example.iot15;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.iot15.classes.User;

public class SignupActivity extends AppCompatActivity {


    EditText etUsername, etPassword, etRepeatPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sign_up_screen);

        viewInitializations();
    }

    void viewInitializations() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etRepeatPassword = findViewById(R.id.et_repeat_password);

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
        if (etRepeatPassword.getText().toString().equals("")) {
            etRepeatPassword.setError("Please Enter Repeat Password");
            return false;
        }

        // Checking if repeat password is same
        if (!etPassword.getText().toString().equals(etRepeatPassword.getText().toString())) {
            etRepeatPassword.setError("Password does not match");
            return false;
        }
        return true;
    }

    // Hook Click Event

    public void performSignUp(View v) {
        if (validateInput()) {

            // Input is valid, here send data to your server

            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            String repeatPassword = etRepeatPassword.getText().toString();

            signUpSQL(username, password);

            //Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();

        }
    }

    public void signUpSQL(String username, String password){
        RequestQueue queue= Volley.newRequestQueue(this);
        String url="https://a21iot15.studev.groept.be/index.php/api/register/" + username + "/" + password;
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, response -> {
            try {
                    System.out.println(url);
                    Intent goToLoginScreen = new Intent(this, LoginActivity.class);
                    startActivity(goToLoginScreen);
                    overridePendingTransition(0, 0);
                    finish();

                }
            catch (Exception e){
                e.printStackTrace();
            }

        }, error -> System.out.println("error"));

        queue.add(stringRequest);

    }

    public void goLoginScreen(View v) {
        Intent goToLoginScreen = new Intent(this, LoginActivity.class);
        startActivity(goToLoginScreen);
        overridePendingTransition(0, 0);
    }
}