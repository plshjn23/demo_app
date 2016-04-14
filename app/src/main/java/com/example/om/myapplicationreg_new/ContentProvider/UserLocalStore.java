package com.example.om.myapplicationreg_new.ContentProvider;

/**
 * Created by om on 4/8/2016.
 */
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kshivang on 08/04/16.
 */
public class UserLocalStore {
    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";
    private static final String KEY_MOBILE_NUMBER = "mobile_number";
    private static final String KEY_NAVIGATION = "navigation";

    public static final String SP_NAME = "userDetails";
    SharedPreferences userLocalDatabase;
    SharedPreferences.Editor spEditor;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
        spEditor = userLocalDatabase.edit();
        spEditor.apply();
    }

    public void setIsWaitingForSms(boolean isWaiting) {
        spEditor = userLocalDatabase.edit();
        spEditor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        spEditor.apply();
    }

    public boolean isWaitingForSms() {
        return userLocalDatabase.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }

    public void setMobileNumber(String mobileNumber, String nav) {
        spEditor = userLocalDatabase.edit();
        spEditor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        spEditor.putString(KEY_NAVIGATION, nav);
        spEditor.apply();
    }

    public String getMobileNumber() {
        return userLocalDatabase.getString(KEY_MOBILE_NUMBER, null);
    }

    public String getNavigation() {
        return userLocalDatabase.getString(KEY_NAVIGATION, null);
    }

    public void storeUserData(User user) {
        spEditor = userLocalDatabase.edit();
        spEditor.putString("apiKey", user.apiKey);
        spEditor.putString("name", user.name);
        spEditor.putString("email", user.email);
        spEditor.putString("contact", user.contact);
        spEditor.apply();
    }

    public User getLoggedInUser() {
        String apiKey = userLocalDatabase.getString("apiKey", null);
        String name = userLocalDatabase.getString("name", null);
        String email = userLocalDatabase.getString("email", null);
        String contact = userLocalDatabase.getString("contact", null);

        return new User(apiKey, name, email, contact);
    }

    public boolean getUserLoggedIn() {
        return userLocalDatabase.getBoolean("loggedIn", false);
    }

    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.apply();
    }

    public void clearUserData() {
        spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.apply();
    }

}
