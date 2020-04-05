package com.copapplication.jie9300.cophealthmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import android.util.Log;


import org.json.JSONObject;


public class LogIn extends AppCompatActivity {
    private String tag = "tiffany";
    private String loginEndpoint;
    private String officerEndpoint;

    JsonObjectRequest jsonObjectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loginEndpoint = getString(R.string.api_base_url) + "/login";
        officerEndpoint = getString(R.string.api_base_url) + "/officer";
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

        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, loginEndpoint, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
//                        textView.setText("Response: " + response.toString());
                        Log.d(tag, response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });



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
