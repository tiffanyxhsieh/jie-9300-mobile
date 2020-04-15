package com.copapplication.jie9300.cophealthmonitoring;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.Date;

import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarHrData;


public class HealthMetrics extends AppCompatActivity {
    private String TAG = "tiffany";

    private int lowerBound;
    private int upperBound;
    private String deviceID;
    PolarBleApi api;
    SharedPreferences sharedPreferences;
    private boolean connected;

    private TextView heartRate;
    private TextView textView4;
    private ImageView imageView2;
    private ProgressBar goodHeartRateProgressBar;
    private ProgressBar badHeartRateProgressBar;
    private ProgressBar loadingProgressBar;
    private TextView textViewSearchingForDevice;
    private TextView textViewDeviceFound;
    private TextView textViewDeviceConnecting;
    private TextView textViewWaitingForHeartRate;

    private BottomNavigationView bottomNavigationView;

    private int counter;
    private FusedLocationProviderClient fusedLocationClient;
    private String[] coordinates = {"-1", "-1"};
    private boolean hrUiSetup = false;
    private int officerId;


//    String DEVICE_ID = "6C3E002B"; // or bt address like F5:A7:B8:EF:7A:D1 // TODO replace with your device id


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_metrics);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        heartRate = (TextView) findViewById(R.id.heartRate);
        textView4 = (TextView) findViewById(R.id.textView4);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        goodHeartRateProgressBar = (ProgressBar) findViewById(R.id.goodHeartRateProgressBar);
        badHeartRateProgressBar = (ProgressBar) findViewById(R.id.badHeartRateProgressBar);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        textViewSearchingForDevice = (TextView) findViewById(R.id.textViewSearchingForDevice);
        textViewDeviceFound = (TextView) findViewById(R.id.textViewDeviceFound);
        textViewDeviceConnecting = (TextView) findViewById(R.id.textViewDeviceConnecting);
        textViewWaitingForHeartRate = (TextView) findViewById(R.id.textViewWaitingForHeartRate);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        sharedPreferences = getSharedPreferences("Settings", 0);
        deviceID = sharedPreferences.getString("deviceID", "0");
        lowerBound = sharedPreferences.getInt("minRate", 0);
        upperBound = sharedPreferences.getInt("maxRate", 0);
        officerId = sharedPreferences.getInt("officer_id", 0);
        connected = false;

        Log.d(TAG, "minHR: " + lowerBound);
        Log.d(TAG, "maxHR: " + upperBound);

        counter = 0;

        Log.d(TAG, deviceID);

        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.setPolarFilter(false);


        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void deviceConnected(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG, "CONNECTED: " + polarDeviceInfo.deviceId);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewDeviceFound.setVisibility(View.INVISIBLE);
                        textViewDeviceConnecting.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void deviceConnecting(PolarDeviceInfo polarDeviceInfo) {
                Handler handler = new Handler(Looper.getMainLooper());
                final String deviceId = polarDeviceInfo.deviceId;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "CONNECTING: " + deviceId);
                        textViewSearchingForDevice.setVisibility(View.INVISIBLE);
                        textViewDeviceFound.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void hrFeatureReady(String identifier) {
                Handler handler = new Handler();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "HR READY: ");
                        textViewDeviceConnecting.setVisibility(View.INVISIBLE);
                        textViewWaitingForHeartRate.setVisibility(View.VISIBLE);

                    }
                });
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG,"DISCONNECTED: " + polarDeviceInfo.deviceId);
            }

            @Override
            public void hrNotificationReceived(String identifier, PolarHrData data) {

                Log.d(TAG, "Here");

                if (!hrUiSetup || heartRate.getVisibility() == View.INVISIBLE) {
                    hrUiReady(); //Sets up the UI for the hr view
                }

                if (connected) {
                    counter++;
                    if (counter == 10) {
                        counter = 0;
                        heartRateData(Integer.toString(data.hr));
                        getLocation();
                        Log.d(TAG, coordinates[0] + " " + coordinates[1]);
                        Log.d(TAG, Integer.toString(data.hr));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateTime = dateFormat.format(new Date()); // Find todays date

                        updateOfficerStatus(Float.parseFloat(coordinates[0]), Float.parseFloat(coordinates[1]), data.hr, currentDateTime);
                    }

                    heartRateData(Integer.toString(data.hr));

                }
            }

        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                try {
                    api.disconnectFromDevice(deviceID);
                } catch (PolarInvalidArgument polarInvalidArgument) {
                    polarInvalidArgument.printStackTrace();
                }

                switch (item.getItemId()) {
                    case R.id.action_preferences:
                        Intent newIntent = new Intent(HealthMetrics.this, BluetoothPrompt.class);
                        startActivity(newIntent);
                        break;
                    case R.id.action_logout:
                        Intent newIntent2 = new Intent(HealthMetrics.this, MainActivity.class);
                        startActivity(newIntent2);
                        break;
                    case R.id.action_disconnect:
                        Intent newIntent3 = new Intent(HealthMetrics.this, BluetoothPrompt.class);
                        startActivity(newIntent3);
                        break;
                }
                return true;
            }
        });

//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    api.disconnectFromDevice(deviceID);
//                } catch (PolarInvalidArgument polarInvalidArgument) {
//                    polarInvalidArgument.printStackTrace();
//                }
//                Intent newIntent = new Intent(HealthMetrics.this, BluetoothPrompt.class);
//                startActivity(newIntent);
//            }
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && savedInstanceState == null) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        connectDevice();
    }

    public void connectDevice() {

        try {
            api.connectToDevice(deviceID);
        } catch (PolarInvalidArgument polarInvalidArgument) {
            polarInvalidArgument.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1) {
            Log.d(TAG, "bt ready");
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        api.backgroundEntered();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        api.foregroundEntered();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        api.shutDown();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void disconnectDevice(View view) {
        Intent newIntent = new Intent(this, BluetoothPrompt.class);
        startActivity(newIntent);
    }

    //setup for when we need to pass dynamic value to heart rate number
    public void heartRateData(String value) {

        final String hrReading = value;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView heartRate = (TextView) findViewById(R.id.heartRate);
                heartRate.setText(hrReading);

                ProgressBar goodProgressBar = (ProgressBar) findViewById(R.id.goodHeartRateProgressBar);
                ProgressBar badProgressBar = (ProgressBar) findViewById(R.id.badHeartRateProgressBar);

                double heartRateValue = Double.parseDouble(hrReading);
                if (outsideBounds(heartRateValue)) {
                    goodProgressBar.setVisibility(View.INVISIBLE);
                    badProgressBar.setVisibility(View.VISIBLE);
                } else {
                    badProgressBar.setVisibility(View.INVISIBLE);
                    goodProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    public boolean outsideBounds(double heartRateValue) {
        return (heartRateValue < lowerBound || heartRateValue > upperBound);
    }

    public void getLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            coordinates[0] = Double.toString(location.getLatitude());
                            coordinates[1] = Double.toString(location.getLongitude());
                        }
                    }
                });
    }

    public void hrUiReady() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "here1");
                connected = true;
                textViewWaitingForHeartRate.setVisibility(View.INVISIBLE);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                heartRate.setVisibility(View.VISIBLE);
                textView4.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.VISIBLE);
                goodHeartRateProgressBar.setVisibility(View.VISIBLE);

                hrUiSetup = true;
                Log.d(TAG, "ui setup done");
            }
        });


    }


    private void updateOfficerStatus(float latitude, float longitude, int hr, String timestamp) {
        String url = getString(R.string.api_base_url) + "/healthMetrics";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("officer_id", officerId);
            requestBody.put("time_stamp", timestamp);
            requestBody.put("heart_rate", hr);
            requestBody.put("latitude", latitude);
            requestBody.put("longitude", longitude);


        } catch (JSONException e) {
            Log.d("tiffany", "json login param error");
        }
        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        requestBody,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String responseString = response.getString("message");
                                    if (responseString.equals("UPDATE_SUCCESSFUL")) {
                                        Log.d(TAG, "SUCCESSFUL POST!!!!");
                                    }


                                } catch (JSONException e) {
                                    Log.d(TAG, e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Couldn't POST :(");

                    }
                });

        ApiRequest.getInstance(this).addToRequestQueue(request);

    }
}