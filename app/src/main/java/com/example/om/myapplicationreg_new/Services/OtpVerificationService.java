package com.example.om.myapplicationreg_new.Services;

/**
 * Created by om on 4/8/2016.
 */
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.om.myapplicationreg_new.Activities.ForgotPassword;
import com.example.om.myapplicationreg_new.Activities.OtpVerification;
import com.example.om.myapplicationreg_new.Activities.Register;
import com.example.om.myapplicationreg_new.Apps.RegistrationDemoApp;
import com.example.om.myapplicationreg_new.ContentProvider.UserLocalStore;
import com.example.om.myapplicationreg_new.R;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by kshivang on 08/04/16.
 */
public class OtpVerificationService extends IntentService {

    String nav, mobileNumber;

    public OtpVerificationService() {
        super(OtpVerificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        UserLocalStore userLocalStore = new UserLocalStore(this);
        nav = userLocalStore.getNavigation();
        mobileNumber = userLocalStore.getMobileNumber();
        if (intent != null) {
            String otp = intent.getStringExtra("otp");
            verifyOtp(otp);
        }
    }

    /**
     * Posting the OTP to server and activating the user
     *
     * @param otp otp received in the SMS
     */

    private void verifyOtp(final String otp) {


        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.otp_url) + mobileNumber + "&" + getString(R.string.otp_key) + "&code=" + otp, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseString) {
                try {
                    Intent intent;
                    switch (responseString){
                        case ("success | 100 | New code generated and code was sent the user"):
                            intent = new Intent(OtpVerificationService.this, OtpVerification.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            break;
                        case ("success | 101 | User already in the system and code was sent to the user again"):
                            intent = new Intent(OtpVerificationService.this, OtpVerification.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            break;
                        case ("success | 103 | Previous code is expired and new code was sent to the user again"):
                            intent = new Intent(OtpVerificationService.this, OtpVerification.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            break;
                        case ("success | 200 | Code matched successfully and user has been verified"):
                            if(nav.equals("Register")) {
                                intent = new Intent(OtpVerificationService.this, Register.class);
                                intent.putExtra("MOBILE", mobileNumber);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                intent = new Intent(OtpVerificationService.this, ForgotPassword.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("MOBILE", mobileNumber);
                                startActivity(intent);
                            }
                            break;
                        case ("error | 907 | Code is expired"):
                            Toast.makeText(getApplicationContext(), "OTP expired please request OTP again", Toast.LENGTH_LONG).show();
                            break;
                        case ("error | 903 | Invalid code"):
                            Toast.makeText(getApplicationContext(), "Invalid OTP", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (Exception e){
                    Log.d("HttpService", e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RegistrationDemoApp.getInstance().addToRequestQueue(stringRequest);
    }
}
