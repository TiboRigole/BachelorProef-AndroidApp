package com.retailsonar.retailsonar;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * uitbreiding van application met methodes om CustomShared data terug te krijgen
 */
public class CustomApplication extends Application {

    private Gson gson;
    private GsonBuilder builder;
    private CustomSharedPreference shared;

    @Override
    public void onCreate() {
        super.onCreate();
        builder = new GsonBuilder();
        gson = builder.create();
        shared = new CustomSharedPreference(getApplicationContext());
    }

    public CustomSharedPreference getShared(){
        return shared;
    }

    public Gson getGsonObject(){
        return gson;
    }

}
