package com.example.om.myapplicationreg_new.Activities;

/**
 * Created by om on 4/8/2016.
 */
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.om.myapplicationreg_new.R;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kshivang on 08/04/16.
 */
public class Register extends AppCompatActivity {

    EditText name, email, password, confirmPassword;
    Button btRegister;
    ProgressBar progressBar;
    RequestQueue requestQueue;
    String registerUrl;
    String mobileNo;
    final DBAdapter db1 = new DBAdapter(Register.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent onIntent = getIntent();
        mobileNo = onIntent.getStringExtra("MOBILE");


        registerUrl = getString(R.string.register_url);
        name = (EditText) findViewById(R.id.etName);
        email = (EditText) findViewById(R.id.etEmail);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);
        password = (EditText) findViewById(R.id.etPassword);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        btRegister = (Button) findViewById(R.id.btRegister);
        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pwd = password.getText().toString(), cpwd = confirmPassword.getText().toString();
                if ((name.getText().length() > 0)){
                    if(pwd.length() > 7) {
                        if(pwd.equals(cpwd)){
                            progressBar.setVisibility(View.VISIBLE);
                            new RegisterInBackground().execute();
                        }
                        else {
                            Toast.makeText(Register.this, "Password do not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(Register.this, "Password must be of more than of 8 character", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(Register.this, "Some necessary fields missing!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(Register.this, MobileVerification.class);
            intent.putExtra("NAV", "Register");
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public class RegisterInBackground extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            requestQueue = Volley.newRequestQueue(getApplicationContext());

            StringRequest request = new StringRequest(Request.Method.POST, registerUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        String error = object.getString("error");

                        switch (error){
                            case "true":
                                String message = object.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                break;
                            case "false":
                                message = object.getString("message");
                                db1.open();
                                String i = "0";
                                db1.insertContact(mobileNo , i);
                                db1.close();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Register.this, Login.class));
                                finish();
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Please fill all information", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((error.networkResponse != null) && (error.networkResponse.statusCode == 400)) {
                        Toast.makeText(getApplicationContext(), "Some fields missing or incorrect", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Check network connection", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("name", name.getText().toString());
                    parameters.put("password", password.getText().toString());
                    parameters.put("contact", mobileNo);
                    if(email.getText()!=null && email.getText().toString().length() != 0){
                        parameters.put("email", email.getText().toString());
                    }

                    return parameters;
                }
            };

            request.setShouldCache(false);
            requestQueue.add(request);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

}
