package com.copapplication.jie9300.cophealthmonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class BluetoothPrompt extends AppCompatActivity {

    private Button buttonConfirm;
    private EditText editTextDeviceID;
    private EditText editTextMaxHR;
    private EditText editTextMinHR;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_prompt);

        buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
        editTextDeviceID = (EditText) findViewById(R.id.editTextDeviceID);
        editTextMaxHR = (EditText) findViewById(R.id.editTextMaxHR);
        editTextMinHR = (EditText) findViewById(R.id.editTextMinHR);
        sharedPreferences = getSharedPreferences("Settings", 0);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

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

        if ((minHR >= 200) || (maxHR < 0) ) {
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
        editor.putInt("minHR",minHR);
        editor.putInt("maxHR", maxHR);
        editor.commit();

        return true;

    }

}
