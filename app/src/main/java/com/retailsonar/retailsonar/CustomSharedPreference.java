package com.retailsonar.retailsonar;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * uitbreiding shared preferences met user data
 * Created by aaron on 4/19/2018.
 */

public class CustomSharedPreference {

    private SharedPreferences sharedPref;

    public CustomSharedPreference(Context context) {
        sharedPref = context.getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
    }

    public SharedPreferences getInstanceOfSharedPreference(){
        return sharedPref;
    }

    //Save user information
    public void setUserData(String userData){
        sharedPref.edit().putString("USER", userData).apply();
    }

    public String getUserData(){
        return sharedPref.getString("USER", "");
    }
}
