package com.copapplication.jie9300.cophealthmonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class LogIn extends AppCompatActivity {
    private String tag = "tiffany";
    private String officerId;
    private int maxRate;
    private int minRate;

    boolean haveDeviceId = false;
    boolean haveHrBounds = false;

    private String deviceId;
    private String loginEndpoint;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

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

        loginEndpoint = getString(R.string.api_base_url) + "/validate_login";
        JSONObject loginParams = new JSONObject();
        try {
            loginParams.put("username", usernameString);
            loginParams.put("password", passwordString);
            Log.d(tag, "username: "+usernameString);
            Log.d(tag, "password: "+ passwordString);
        } catch(JSONException e) {
            Log.d("tiffany", "json login param error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST,
                        loginEndpoint,
                        loginParams,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("tiffany", response.toString());
                                try {
                                    if (response.getString("message").equals("LOGIN_SUCCESS")) {
                                        officerId = response.getString("officer_id");
                                        getMaxMinHR();
                                        getDeviceId();
                                    } else if (response.getString("message").equals("LOGIN_SUCCESS")){
                                        retryCredentials();
                                    }
                                } catch(JSONException e) {

                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("tiffany", error.toString());
                            }
                        });

        ApiRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);



    }




    private void getDeviceId() {
        String deviceEndpoint = getString(R.string.api_base_url) + "/devices";
        String url = deviceEndpoint + "?officer_id=" + officerId;

        Log.d(tag, "endpoint: " + url);
        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    haveDeviceId = true;
                                    deviceId = response.getString("device_id");
                                    Log.d(tag, "before saveDevice() call");

                                    saveDeviceID(deviceId);
                                    Log.d(tag, deviceId);

                                    if (haveHrBounds) {
                                        goToBluetooth();
                                    }

                                } catch (JSONException e){
                                    Log.d(tag, e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(tag, error.toString());
                    }
                });



        ApiRequest.getInstance(this).addToRequestQueue(request);
    }

    private void getMaxMinHR() {
        String officerEndpoint = getString(R.string.api_base_url) + "/officer";
        String url = officerEndpoint + "?officer_id=" + officerId;

        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    maxRate = response.getInt("maxRate");
                                    minRate = response.getInt("minRate");

                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    editor.putInt("minRate", minRate);
                                    editor.putInt("maxRate", maxRate);
                                    editor.commit();

                                    haveHrBounds = true;
                                    if (haveDeviceId) {
                                        goToBluetooth();
                                    }

                                } catch (JSONException e){
                                    Log.d(tag, e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        ApiRequest.getInstance(this).addToRequestQueue(request);
    }

    private void goToBluetooth() {
        Log.d(tag, "changing intents");
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


    private void saveDeviceID(String deviceId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("device_id", deviceId);
        editor.commit();
    }

}
