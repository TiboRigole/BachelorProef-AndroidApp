package com.retailsonar.retailsonar.regio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.UserService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

/**
 * Google Maps implementatie met marker op huidig pand en aanduiding huidige locatie
 * Created by aaron on 4/6/2018.
 */

public class Activity_Locatie_Pand extends AppCompatActivity implements OnMapReadyCallback {

    // HTTP request help
    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit = builder.build();
    PandService pandService = retrofit.create(PandService.class);

    // huidige pand en id
    Pand huidigPand;
    long pandId;

    // coordinaten van huidig pand
    double latPand;
    double longPand;

    // plaats gebruiker
    private FusedLocationProviderClient mFusedLocationClient;
    double lonHuidig;
    double latHuidig;

    // Google map
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locatie_pand);

        // setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // initialisatie huidig pand
        pandId = (long) getIntent().getExtras().get("pandId");
        Call<Pand> pandRequest = pandService.getPandById(pandId);
        System.out.println(pandRequest.request().toString());
        pandRequest.enqueue(new Callback<Pand>() {
            @Override
            public void onResponse(Call<Pand> call, Response<Pand> response) {
                huidigPand = response.body();

                // indien pand gevonden marker op pand in map
                latPand = huidigPand.getLat();
                longPand = huidigPand.getLongi();
                LatLng pand = new LatLng(latPand, longPand);
                mMap.addMarker(new MarkerOptions().position(pand).title("Marker op pand " + huidigPand.getStraat()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pand));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
                mMap.setMaxZoomPreference(20);
            }

            @Override
            public void onFailure(Call<Pand> call, Throwable t) {
                t.printStackTrace();
            }
        });



    }

    /**
     * methode uitgevoerd nadat map klaar is
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // checken van permissions en mogelijkheid tot gebruiken google maps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        })
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // laatst gekende locatie verkregen, in sommige gevallen kan dat null zijn
                        if (location != null) {
                            lonHuidig = location.getLongitude();
                            latHuidig = location.getLatitude();

                            // permission vragen om laatste locatie op te vragen
                            checkPermission();
                            mMap.setMyLocationEnabled(true);
                        }
                    }





                });

    }

    /**
     * permission handling
     */
    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

}
