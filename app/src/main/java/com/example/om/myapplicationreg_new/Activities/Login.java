package com.example.om.myapplicationreg_new.Activities;

/**
 * Created by om on 4/8/2016.
 */
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.om.myapplicationreg_new.ContentProvider.User;
import com.example.om.myapplicationreg_new.ContentProvider.UserLocalStore;
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
public class Login extends AppCompatActivity {

    TextView registerLink;
    EditText mobile, password;
    Button btLogin;
    ProgressBar progressBar;
    RequestQueue requestQueue;
    String loginUrl, mobileNo;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userLocalStore = new UserLocalStore(this);

        mobile = (EditText) findViewById(R.id.mobile);
        password = (EditText) findViewById(R.id.password);

        mobileNo = userLocalStore.getMobileNumber();

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        loginUrl = getString(R.string.login_url);

        btLogin = (Button) findViewById(R.id.login);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mobile.getText().length() > 0) && (password.getText().length() > 0)) {
                    progressBar.setVisibility(View.VISIBLE);
                    new LoginInBackground().execute();
                } else
                    Toast.makeText(Login.this, "Some Fields Missing", Toast.LENGTH_SHORT).show();
            }
        });

        registerLink = (TextView) findViewById(R.id.register);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, MobileVerification.class);
                intent.putExtra("NAV", "Register");
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, MobileVerification.class);
                intent.putExtra("NAV", "Forgot password");
                startActivity(intent);
                finish();
            }
        });
        if(mobileNo != null && mobileNo.length() == 10){
            mobile.setText(mobileNo);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate()) {
            Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Login.this, Home.class);
            intent.putExtra("fragment_no", 1);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    private void StoreLoggedInUser(String apiKey, String name, String email, String contact) {

        User user = new User(apiKey, name, email, contact);
        UserLocalStore userLocalStore = new UserLocalStore(this);
        userLocalStore.storeUserData(user);

    }

    private boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    public class LoginInBackground extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            requestQueue = Volley.newRequestQueue(getApplicationContext());

            final StringRequest request = new StringRequest(Request.Method.POST, loginUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        Boolean error = object.getBoolean("error");

                        if (error) {
                            String message = object.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            String apiKey = object.getString("apiKey");
                            String name = object.getString("name");
                            String email_value = object.getString("email");
                            String contact = object.getString("contact");

                            Toast.makeText(getApplicationContext(), "Hello " + name + "!", Toast.LENGTH_SHORT).show();

                            StoreLoggedInUser(apiKey, name, email_value, contact);

                            userLocalStore.setUserLoggedIn(true);

                            Intent intent = new Intent(Login.this, Home.class);
                            intent.putExtra("fragment_no", 1);
                            startActivity(intent);
                            progressBar.setVisibility(View.GONE);
                            finish();
                            //start next activity
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    if ((error.networkResponse != null) && (error.networkResponse.statusCode == 400)) {
                        Toast.makeText(getApplicationContext(), "Necessary field/s missing", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Check network Connection", Toast.LENGTH_SHORT).show();
                    }

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("contact", mobile.getText().toString());
                    parameters.put("password", password.getText().toString());

                    return parameters;
                }
            };
            request.setShouldCache(false);
            requestQueue.add(request);

            return null;
        }

    }

}
