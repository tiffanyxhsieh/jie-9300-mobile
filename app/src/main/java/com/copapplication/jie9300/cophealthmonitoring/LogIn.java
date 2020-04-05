package com.copapplication.jie9300.cophealthmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.app.Activity;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.Fitness;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.support.annotation.NonNull;



public class LogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
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

        final Editable userName = username.getText();
        final Editable passwordText = password.getText();
        final String userNameString = userName.toString();
        final String passwordString = passwordText.toString();

        if (validSignIn(userNameString, passwordString)) {
            Intent newIntent = new Intent(this, BluetoothPrompt.class);
            Log.d("tiffany", "starting new intent");
            startActivity(newIntent);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Invalid Credentials");
            dialog.setMessage("Please make sure you enter valid credentials.");
            dialog.setPositiveButton("OK", null);
            dialog.show();
        }
    }

    /**
     * Checks the passed in username and password with the ones on file.
     *
     * @param username The username being confirmed for the sign-in
     * @param password The password that should correspond with the given username
     * @return the validity of the sign in (true if valid, false if not valid)
     */
    private static boolean validSignIn(String username, String password) {
        return username.equals("a") && password.equals("b");
    }


}
