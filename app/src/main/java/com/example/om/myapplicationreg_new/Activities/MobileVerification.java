package com.example.om.myapplicationreg_new.Activities;

/**
 * Created by om on 4/8/2016.
 */
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kshivang on 08/04/16.
 */
public class MobileVerification extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    static String mobileText;
    RelativeLayout layout;
    ProgressBar progressBar;
    static String nav;
    static Intent toIntent, loginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);
        Button verify = (Button) findViewById(R.id.verify);
        layout = (RelativeLayout) findViewById(R.id.layout);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        loginIntent = new Intent(getApplicationContext(), Login.class);
        Intent onIntent = getIntent();
        nav = onIntent.getStringExtra("NAV");

        toIntent = new Intent(getApplicationContext(), OtpVerification.class);
        if (nav.equals("Register")) {
            toIntent.putExtra("NAV", "Forgot password");
        } else {
            toIntent.putExtra("NAV", "Register");
        }


        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileText = ((EditText) findViewById(R.id.mobile_no)).getText().toString();
                if(mobileText.length() == 10 || (mobileText.length() == 12 && mobileText.subSequence(0, 2).equals("91")) || (mobileText.length() == 13 && mobileText.subSequence(0, 3).equals("+91"))) {
                    if(mobileText.length() == 12){
                        mobileText = mobileText.substring(2);
                    } else if(mobileText.length() == 13) {
                        mobileText = mobileText.substring(3);
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    new RequestBackground().execute();

                } else {
                    switch (mobileText.length()){
                        case 0:
                            Snackbar.make(layout, "Enter mobile no. first", Snackbar.LENGTH_LONG).show();
                            break;
                        default:
                            Snackbar.make(layout, "Wrong mobile no. format (e.g. 9876598765)", Snackbar.LENGTH_LONG).show();
                    }
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

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {


        String temp;

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;

            try {
                /* forming the java.net.URL object */
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                /* for Get request */
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);

                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }

                    temp = response.toString();
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }

            } catch (Exception e) {
                Log.d("MobileVerification", e.getLocalizedMessage());
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            // Download complete. Let us update UI
            progressBar.setVisibility(View.GONE);

            if (result == 1) {
                Intent intent = new Intent(MobileVerification.this, OtpVerification.class);
                intent.putExtra("MOBILE", mobileText);
                intent.putExtra("NAV", nav);
                startActivity(intent);
                finish();
            } else {
                Snackbar.make(layout, "Check network connection", Snackbar.LENGTH_LONG).show();
            }

        }
    }
    public class RequestBackground extends AsyncTask<Void,Void,Void> {

        final UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            final StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.check_user_url), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        String error = object.getString("error");

                        if(nav.equals("Register")) {
                            switch (error) {
                                case "false":
                                    userLocalStore.setMobileNumber(mobileText, "Forgot password");
                                    progressBar.setVisibility(View.GONE);
                                    FragmentManager fm = getFragmentManager();
                                    Dialog dialogFragment = new Dialog();
                                    dialogFragment.show(fm, "Forget Password");
                                    break;
                                case "true":
                                    userLocalStore.setMobileNumber(mobileText, "Register");
                                    new AsyncHttpTask().execute(getString(R.string.otp_url) + mobileText + "&" + getString(R.string.otp_key));
                                    break;
                                default:
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "some error", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            switch (error) {
                                case "false":
                                    userLocalStore.setMobileNumber(mobileText, "Forgot password");
                                    new AsyncHttpTask().execute(getString(R.string.otp_url) + mobileText + "&" + getString(R.string.otp_key));
                                    break;
                                case "true":
                                    userLocalStore.setMobileNumber(mobileText, "Register");
                                    progressBar.setVisibility(View.GONE);
                                    FragmentManager fm = getFragmentManager();
                                    Dialog dialogFragment = new Dialog();
                                    dialogFragment.show(fm, "Register");
                                    break;
                                default:
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "some error", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    NetworkResponse networkResponse = error.networkResponse;
                    progressBar.setVisibility(View.GONE);
                    if((networkResponse != null) && (networkResponse.statusCode == 400)){
                        Toast.makeText(getApplicationContext(), "necessary field/s missing", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Check network connection", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String,String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("contact", mobileText);
                    return parameters;
                }
            };

            request.setShouldCache(false);
            requestQueue.add(request);

            return null;
        }
    }
    public static class Dialog extends DialogFragment {

        String Type;
        View rootView;

        public Dialog() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Type = getTag();
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


            if(Type.equals("Register")) {
                rootView = inflater.inflate(R.layout.fragment_signup, container, false);

                Button register = (Button) rootView.findViewById(R.id.register);

                register.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncHttpTask().execute(getString(R.string.otp_url) + mobileText + "&" + getString(R.string.otp_key));
                    }
                });

            } else {
                rootView = inflater.inflate(R.layout.fragment_forgot, container, false);

                Button changePassword = (Button) rootView.findViewById(R.id.change_password);
                changePassword.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                changePassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncHttpTask().execute(getString(R.string.otp_url) + mobileText + "&" + getString(R.string.otp_key));
                    }
                });
            }

            rootView.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(loginIntent);
                    getActivity().finish();
                    dismiss();
                }
            });

            return rootView;
        }

        public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {


            String temp;

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Integer doInBackground(String... params) {
                Integer result = 0;
                HttpURLConnection urlConnection;

                try {
                    /* forming the java.net.URL object */
                    URL url = new URL(params[0]);

                    urlConnection = (HttpURLConnection) url.openConnection();

                    /* for Get request */
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setUseCaches(false);

                    int statusCode = urlConnection.getResponseCode();

                    // 200 represents HTTP OK
                    if (statusCode == 200) {
                        BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            response.append(line);
                        }

                        temp = response.toString();
                        result = 1; // Successful
                    } else {
                        result = 0; //"Failed to fetch data!";
                    }

                } catch (Exception e) {
                    Log.d("Dialog", e.getLocalizedMessage());
                }

                return result; //"Failed to fetch data!";
            }

            @Override
            protected void onPostExecute(Integer result) {

                // Download complete. Let us update UI

                if (result == 1) {

                    toIntent.putExtra("MOBILE", mobileText);
                    startActivity(toIntent);
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Check network connection", Toast.LENGTH_LONG).show();
                }
                dismiss();

            }
        }
    }
}
