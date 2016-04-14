package com.example.om.myapplicationreg_new.Activities;

/**
 * Created by om on 4/8/2016.
 */
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.om.myapplicationreg_new.ContentProvider.UserLocalStore;
import com.example.om.myapplicationreg_new.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kshivang on 08/04/16.
 */
public class OtpVerification extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    TextView mTextField;
    RelativeLayout resendOTP, layout;
    ProgressBar progressBar;
    String mobile_no, otp, nav;
    Button verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        UserLocalStore userLocalStore = new UserLocalStore(this);

        mTextField = (TextView) findViewById(R.id.count);
        ImageView imageView = (ImageView) findViewById(R.id.edit_mobile_no);
        resendOTP = (RelativeLayout) findViewById(R.id.resendOTP);
        layout = (RelativeLayout) findViewById(R.id.layout);
        TextView mobile = (TextView) findViewById(R.id.mobile_no);
        verify = (Button) findViewById(R.id.verify);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Intent onIntent = getIntent();
        mobile_no = onIntent.getStringExtra("MOBILE");
        nav = onIntent.getStringExtra("NAV");
        if(nav == null || nav.equals("null")){
            progressBar.setVisibility(View.VISIBLE);
            mobile_no = userLocalStore.getMobileNumber();
            nav = userLocalStore.getNavigation();
        }


        mobile.setText("+91 " + mobile_no);

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                new AsyncHttpTask().execute(getString(R.string.otp_url) + mobile_no + "&" + getString(R.string.otp_key));
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtpVerification.this, OtpVerification.class);
                intent.putExtra("NAV", nav);
                startActivity(intent);
                finish();
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                otp = ((EditText) findViewById(R.id.otp_number)).getText().toString();

                if(otp.length() == 6) {
                    progressBar.setVisibility(View.VISIBLE);
                    new AsyncHttpTask().execute(getString(R.string.otp_url)+ mobile_no + "&" + getString(R.string.otp_key) + "&code=" + otp);
                } else {
                    Snackbar.make(layout, "One Time Password is 6 digit code", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextField.setText( "" + (millisUntilFinished / 1000));
            }

            public void onFinish() {
                mTextField.setText("0");
                resendOTP.setVisibility(View.VISIBLE);
            }
        }.start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            Intent intent = new Intent(OtpVerification.this, MobileVerification.class);
            intent.putExtra("NAV", nav);
            startActivity(intent);

        }
        return super.onKeyDown(keyCode, event);
    }
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {


        String responseString;

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

                    responseString = response.toString();
                    switch (responseString){
                        case ("success | 100 | New code generated and code was sent the user"):
                            result = 100; // otp resend
                            break;
                        case ("success | 101 | User already in the system and code was sent to the user again"):
                            result = 101;
                            break;
                        case ("success | 103 | Previous code is expired and new code was sent to the user again"):
                            result = 103;
                            break;
                        case ("success | 200 | Code matched successfully and user has been verified"):
                            result = 200;
                            break;
                        case ("error | 907 | Code is expired"):
                            result = 907;
                            break;
                        case ("error | 903 | Invalid code"):
                            result = 903;
                            break;
                        default:
                            result = 500;
                    }

                } else {
                    result = 0; //"Failed to fetch data!";
                }

            } catch (Exception e) {
                Log.d("OtpVerification", e.getLocalizedMessage());
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            // Download complete. Let us update UI
            progressBar.setVisibility(View.GONE);

            Intent intent;
            switch (result){
                case 100:
                    intent = new Intent(OtpVerification.this, OtpVerification.class);
                    intent.putExtra("MOBILE", mobile_no);
                    intent.putExtra("NAV", nav);
                    startActivity(intent);
                    finish();
                    break;
                case 101:
                    intent = new Intent(OtpVerification.this, OtpVerification.class);
                    intent.putExtra("MOBILE", mobile_no);
                    intent.putExtra("NAV", nav);
                    startActivity(intent);
                    finish();
                    break;
                case 103:
                    intent = new Intent(OtpVerification.this, OtpVerification.class);
                    intent.putExtra("MOBILE", mobile_no);
                    intent.putExtra("NAV", nav);
                    startActivity(intent);
                    finish();
                    break;
                case 200:
                    if(nav.equals("Register")) {
                        intent = new Intent(OtpVerification.this, Register.class);
                        intent.putExtra("MOBILE", mobile_no);
                        startActivity(intent);
                        finish();
                    } else {
                        intent = new Intent(OtpVerification.this, ForgotPassword.class);
                        intent.putExtra("MOBILE", mobile_no);
                        startActivity(intent);
                        finish();
                    }
                    break;
                case 907:
                    Snackbar.make(layout, "OTP expired please request OTP again", Snackbar.LENGTH_LONG).show();
                    break;
                case 903:
                    Snackbar.make(layout, "Invalid OTP", Snackbar.LENGTH_LONG).show();
                    break;
                case 500:
                    Snackbar.make(layout, "Server is down", Snackbar.LENGTH_LONG).show();
                    break;
                default:
                    Snackbar.make(layout, "Check network connection", Snackbar.LENGTH_LONG).show();
            }

        }
    }
}
