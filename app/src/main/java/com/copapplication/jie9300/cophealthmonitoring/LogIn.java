package com.copapplication.jie9300.cophealthmonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class LogIn extends AppCompatActivity {
    private String tag = "tiffany";
    private String officerId;
    SharedPreferences sharedPreferences;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
         String loginEndpoint = getString(R.string.api_base_url) + "/validate_login";


        sharedPreferences = getSharedPreferences("Settings", 0);


    }

    @Override
    protected void onResume() {
        super.onResume();
        resetPage();

    }

    /**
     * Puts things back to the default state (meant for when the user returns from the Bluetooth
     * connection page).
     */
    private void resetPage() {
        EditText userName = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);

        userName.setText("");
        password.setText("");
    }

    /**
     * Handles back press click; takes user back to previous activity
     *
     * @param view Automatic parameter for user interaction
     */
    public void ClickedBackButton(View view) {
        onBackPressed();
    }

    public void PressLogIn(View view) {
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);

        final String usernameString = username.getText().toString().trim();
        final String passwordString = password.getText().toString();


        //
        JSONObject loginParams = new JSONObject();
        try {
            loginParams.put("username", usernameString);
            loginParams.put("password", passwordString);
            Log.d(tag, "username: "+usernameString);
            Log.d(tag, "password: "+ passwordString);
        } catch(JSONException e) {
            Log.d("tiffany", "json object error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST,
                        "http://192.168.1.4:5000/validate_login",
                        loginParams,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("tiffany", response.toString());
                                try {
                                    if (response.getString("message").equals("LOGIN_SUCCESS")) {
                                        officerId = response.getString("officer_id");
                                        successfulLogin();
                                    } else {
                                        retryCredentials();
                                    }
                                } catch(JSONException e) {

                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.d("tiffany", "error json");
                            }
                        });

        ApiRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);



    }

    /**
     * Checks the passed in username and password with the ones on file.
     *
     * @param username The username being confirmed for the sign-in
     * @param password The password that should correspond with the given username
     * @return the validity of the sign in (true if valid, false if not valid)
     */
    private static boolean validSignIn(String username, String password) {
        return true;
    }

    private void successfulLogin() {
        Log.d(tag, "helper method");
        String officerEndpoint = getString(R.string.api_base_url) + "/officer";

        String url = officerEndpoint + "?officer_id=" + officerId;
        Log.d(tag, "endpoint: "+ url);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("officer_id", officerId);
        editor.commit();



        //TODO: call /officer endpoint and store result in sharedpreferences

//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            Log.d(tag,response);
//                            HashMap<String, Object> hashMap = new HashMap<>(JsonUtility.jsonToMap(response));
//                            Log.d(tag, hashMap.toString());
//                        } catch(JSONException e) {
//                            Log.d(tag,"couldn't parse JSON");
//                            Log.d(tag, e.toString());
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
////                textView.setText("That didn't work!");
//            }
//        });
//
//        ApiRequest.getInstance(this).addToRequestQueue(stringRequest);
        Intent newIntent = new Intent(this, BluetoothPrompt.class);
        startActivity(newIntent);

    }

    private void retryCredentials() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Invalid Credentials");
        dialog.setMessage("Please make sure you enter valid credentials.");
        dialog.setPositiveButton("OK", null);
        dialog.show();
    }

}
