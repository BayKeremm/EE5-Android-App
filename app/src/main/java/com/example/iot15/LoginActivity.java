package com.example.iot15;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void performSignUp (View v) {
        if (validateInput()) {

            // Input is valid, here send data to your server

            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            checkPassword(username, password);

            //Toast.makeText(this,"Login Success",Toast.LENGTH_SHORT).show();
        }
    }

    public void goToSignup(View v) {
        // Open your SignUp Activity if the user wants to signup
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void checkPassword(String username, String password) {
        RequestQueue queue= Volley.newRequestQueue(this);
        String url="https://studev.groept.be/api/a21iot15/retrieve_by_username/" + username;
        System.out.println(url);
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray responseJSON = new JSONArray(response);
                System.out.println("\n" + responseJSON + "\n");
                // compare passwords
                if(responseJSON.getJSONObject(0).getString("password").compareTo(password) == 0){
                    // move to next screen and pass on username
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("username", responseJSON.getJSONObject(0).getString("username"));
                    startActivity(intent);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }, error -> System.out.println("error"));

        queue.add(stringRequest);
    }
}
