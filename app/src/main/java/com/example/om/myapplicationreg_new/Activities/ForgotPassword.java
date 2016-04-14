package com.example.om.myapplicationreg_new.Activities;

/**
 * Created by om on 4/8/2016.
 */
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.om.myapplicationreg_new.ContentProvider.UserLocalStore;
import com.example.om.myapplicationreg_new.R;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kshivang on 08/04/16.
 */
public class ForgotPassword extends AppCompatActivity {

    String mobileNo, pwd;
    RelativeLayout layout;
    ProgressBar progressBar;
    TextView password, confirmPassword;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Button change_password = (Button) findViewById(R.id.change_password);
        layout = (RelativeLayout) findViewById(R.id.layout);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        password = (TextView) findViewById(R.id.password);
        confirmPassword = (TextView) findViewById(R.id.confirmPassword);

        Intent onIntent = getIntent();
        mobileNo = onIntent.getStringExtra("MOBILE");
        if(mobileNo == null || mobileNo.equals("null")){
            userLocalStore = new UserLocalStore(this);
            mobileNo = userLocalStore.getMobileNumber();
        }

        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pwd = password.getText().toString();
                String cpwd = confirmPassword.getText().toString();
                if ((pwd.length() > 0) && (cpwd.length() > 0)) {
                    if (pwd.length() > 7) {
                        if (pwd.equals(cpwd)) {
                            new RequestBackground().execute();
                            progressBar.setVisibility(View.VISIBLE);
                        } else {
                            Snackbar.make(layout, "Password do not match", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(layout, "Password must be of more than of 8 character", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(layout, "Some necessary fields missing!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            startActivity(new Intent(getApplicationContext(), Login.class));
        }
        return super.onKeyDown(keyCode, event);
    }

    RequestQueue requestQueue;
    public class RequestBackground extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            final StringRequest request = new StringRequest(Request.Method.PUT, getString(R.string.forgot_password_url), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        String error = object.getString("error");

                        progressBar.setVisibility(View.GONE);
                        switch (error){
                            case "true":
                                Toast.makeText(getApplicationContext(), "No changes made!", Toast.LENGTH_SHORT).show();
                                break;
                            case "false":
                                String message = object.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ForgotPassword.this, Login.class);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "some error", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    progressBar.setVisibility(View.GONE);
                    if (networkResponse != null && networkResponse.statusCode == 400) {
                        Toast.makeText(getApplicationContext(), "necessary field/s missing", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Check network Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }) {
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    if (response.headers == null) {
                        // cant just set a new empty map because the member is final.
                        response = new NetworkResponse(
                                response.statusCode,
                                response.data,
                                Collections.<String, String>emptyMap(), // this is the important line, set an empty but non-null map.
                                response.notModified,
                                response.networkTimeMs);
                    }

                    return super.parseNetworkResponse(response);
                }


                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("contact", mobileNo);
                    parameters.put("password", pwd);
                    return parameters;
                }
            };
            request.setShouldCache(false);
            requestQueue.add(request);
            return null;
        }
    }
}
