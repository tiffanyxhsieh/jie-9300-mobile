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

    private String officerId;
    private int minRate;
    private int maxRate;
    private String deviceID;

    private SharedPreferences sharedPreferences;
    private Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_prompt);
        sharedPreferences = getSharedPreferences("Settings", 0);
        officerId = sharedPreferences.getString("officer_id", "0");

        minRate = Optional.ofNullable(sharedPreferences.getInt("minRate", 0)).orElse(0);
        Log.d("tiffany", Integer.toString(maxRate));
        maxRate = Optional.ofNullable(sharedPreferences.getInt("maxRate",0)).orElse(0);
        Log.d("tiffany", Integer.toString(maxRate));

        deviceID = Optional.ofNullable(sharedPreferences.getString("deviceID","0")).orElse("Serial #");

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


                if (checkTextValues()) {
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

        if ((minHR >= 200) || (maxHR < 0)) {
            editTextMinHR.setError("Enter a valid minimum HR");
            return false;
        }

        if ((maxHR >= 200) || (maxHR < 0)) {
            editTextMaxHR.setError("Enter a valid maximum HR");
            return false;
        }

        if (minHR > maxHR) {
            editTextMaxHR.setError("The maximum HR must be greater than the minimum HR");
            return false;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("deviceID", deviceID);
        editor.putInt("minRate", minHR);
        editor.putInt("maxRate", maxHR);
        editor.commit();

        return true;

    }

}
