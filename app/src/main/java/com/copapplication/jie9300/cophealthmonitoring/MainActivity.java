package com.copapplication.jie9300.cophealthmonitoring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "dawar";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();

    }

    /**
     * Manages the click of the LogIn button
     */
    public void ClickLogIn(View view) {
        Intent newIntent = new Intent(this, LogIn.class);
        startActivity(newIntent);
    }
}
