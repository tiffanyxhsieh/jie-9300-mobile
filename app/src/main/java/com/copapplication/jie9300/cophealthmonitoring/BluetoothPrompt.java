package com.copapplication.jie9300.cophealthmonitoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class BluetoothPrompt extends AppCompatActivity {

    private Button buttonConfirm;
    private EditText editTextDeviceID;
    private EditText editTextMaxHR;
    private EditText editTextMinHR;

    private int officerId;
    private int minRate;
    private int maxRate;
    private String deviceID;

    private SharedPreferences sharedPreferences;
    private Context ctx = this;

    private String TAG = "tiffany";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_prompt);
        sharedPreferences = getSharedPreferences("Settings", 0);
        officerId = sharedPreferences.getInt("officer_id", 0);
        Log.d(TAG, "THIS IS THE OFFICER_ID" + officerId);

        minRate = Optional.ofNullable(sharedPreferences.getInt("minRate", 0)).orElse(0);
        maxRate = Optional.ofNullable(sharedPreferences.getInt("maxRate",0)).orElse(0);

        deviceID = Optional.ofNullable(sharedPreferences.getString("device_id","0")).orElse("Serial #");

        buttonConfirm = (Button) findViewById(R.id.buttonConfirm);

        editTextDeviceID = (EditText) findViewById(R.id.editTextDeviceID);
        editTextDeviceID.setText(deviceID);

        editTextMaxHR = (EditText) findViewById(R.id.editTextMaxHR);
        editTextMaxHR.setText(Integer.toString(maxRate));

        editTextMinHR = (EditText) findViewById(R.id.editTextMinHR);
        editTextMinHR.setText(Integer.toString(minRate));

        buttonConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (checkTextValues()) {
                    saveSettings();
                    Intent newIntent = new Intent(BluetoothPrompt.this, HealthMetrics.class);
                    startActivity(newIntent);
                }
            }
        });
    }

    private boolean checkTextValues() {

        String deviceID = String.valueOf(editTextDeviceID.getText());

        if (editTextMinHR.getText().length() == 0) {
            editTextMinHR.setError("Enter a valid minimum HR");
            return false;
        }

        if (editTextMaxHR.getText().length() == 0) {
            editTextMaxHR.setError("Enter a valid maximum HR");
            return false;
        }

        int maxHR = Integer.parseInt(editTextMaxHR.getText().toString());
        int minHR = Integer.parseInt(editTextMinHR.getText().toString());

        if (deviceID.length() != 8) {
            editTextDeviceID.setError("Enter a valid DeviceID");
            return false;
        }

        if ((minHR >= 200) || (maxHR < 30)) {
            editTextMinHR.setError("Enter a valid minimum HR");
            return false;
        }

        if ((maxHR >= 220) || (maxHR < 0)) {
            editTextMaxHR.setError("Enter a valid maximum HR");
            return false;
        }

        if (minHR > maxHR) {
            editTextMaxHR.setError("The maximum HR must be greater than the minimum HR");
            return false;
        }


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("deviceID", deviceID);
        Log.d(TAG, "sharedPref deviceID: " + deviceID);
        editor.putInt("minRate", minHR);
        editor.putInt("maxRate", maxHR);
        editor.commit();

        return true;
    }

    public void saveSettings() {
        Log.d(TAG, "saveSettings()");
        //check if current deviceId field matches value stored in db, update db if different
        if (!editTextDeviceID.getText().toString().equals(deviceID)) {
            Log.d(TAG, "deviceId has been changed!");
            updateDeviceId(editTextDeviceID.getText().toString());
        }

        //check if current hr  fields match values stored in db, update db if different
        if (!editTextMaxHR.getText().toString().equals(maxRate) ||
                !editTextMinHR.getText().toString().equals(minRate)) {
            Log.d(TAG, "hr bounds have been changed!");

            updateHRBounds();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("minRate", Integer.parseInt(editTextMinHR.getText().toString()));
        editor.putInt("maxRate", Integer.parseInt(editTextMaxHR.getText().toString()));
        editor.putString("device_id", (editTextDeviceID.getText().toString()));

        editor.commit();
    }

    public void updateDeviceId(String newDevice) {
        String url = getString(R.string.api_base_url) +
                "/devices?officer_id="+officerId +
                "&device_id=" + newDevice.trim();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("tiffany", "device update: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        });
        ApiRequest.getInstance(ctx).addToRequestQueue(stringRequest);
    }

    public void updateHRBounds() {
        String url = getString(R.string.api_base_url) + "/update_rateBounds";

        Response.Listener<JSONObject> apiResponse = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("tiffany", response.toString());
                Intent newIntent = new Intent(BluetoothPrompt.this, HealthMetrics.class);
                startActivity(newIntent);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tiffany", error.toString());
            }
        };

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("officer_id", officerId);
            requestBody.put("minRate", Integer.parseInt(editTextMinHR.getText().toString()));
            requestBody.put("maxRate", Integer.parseInt(editTextMaxHR.getText().toString()));
        } catch (JSONException e) {

        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                apiResponse,
                errorListener);

        ApiRequest.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

}
