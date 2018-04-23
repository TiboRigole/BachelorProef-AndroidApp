package com.retailsonar.retailsonar.regio;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Base64;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.entities.User;
import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.UserService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fragment ondersteuning bij alle incomplete panden
 * Created by aaron on 4/3/2018.
 */

public class Fragment_Regiomanager_Incompleet extends Fragment {

    // voor HTTP requests
    Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create(gson));
    Retrofit retrofit=builder.build();
    UserService userService= retrofit.create(UserService.class);
    PandService pandService=retrofit.create(PandService.class);

    // huidige gebruiker
    User user;
    private String token;

    // lijst met panden van gebruiker
    List<Pand> panden;

    // plaats van gebruiker
    private FusedLocationProviderClient mFusedLocationClient;
    double lon;
    double lat;


    // layout xml
    LinearLayout grid;
    Dialog myDialog;

    // afbeeldingsupport
    byte[] afbeelding;
    Bitmap afbeeldingBitMap;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regiomanager_incompleet, container, false);


        // locatie permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(((RegioManagerActivity)getActivity()));



        // huidige user opvragen
        token=((RegioManagerActivity)getActivity()).getIntent().getExtras().get("Token").toString();
        Call<User> currentUser=userService.getCurrentUser(token);
        currentUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                user=response.body();
                System.out.println(user.getName());

                // panden bij user
                initPanden(user.getWerkRegio(),user.getWinkel(), view);
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });


        return view;
    }


    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(((RegioManagerActivity)getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(((RegioManagerActivity)getActivity()),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(((RegioManagerActivity)getActivity()),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }
    //panden opvragen die bij winkel van huidige user horen
    public void initPanden(String regio, String winkel, View view){

        Call<List<Pand>> regioIncompletePanden= pandService.getIncompleteRegioPanden(regio,winkel );
        System.out.println(regioIncompletePanden.request().toString());
        try {

            mFusedLocationClient.getLastLocation().addOnFailureListener(((RegioManagerActivity)getActivity()), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            })
                    .addOnSuccessListener(((RegioManagerActivity)getActivity()), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                            System.out.println("location "+location.toString());
                            // Got last known location. In some rare situations this can be null.

                                lon=location.getLongitude();
                                lat=location.getLatitude();
                                System.out.println(lat + " " + lon);
                            }
                        }





                    });
        }
        catch(SecurityException e){
            e.printStackTrace();
        }


        regioIncompletePanden.enqueue(new Callback<List<Pand>>() {
                                 @Override
                                 public void onResponse(Call<List<Pand>> call, Response<List<Pand>> response) {
                                     panden = response.body();

                                     // sorteren op afstand

                                     Collections.sort(panden, new Comparator<Pand>() {
                                         @Override
                                         public int compare(Pand pand, Pand t1) {

                                             if(pand.distance(lat, lon)>t1.distance(lat, lon)){
                                                 return 1;
                                             }
                                             if(pand.distance(lat, lon)<t1.distance(lat, lon)) {
                                                 return -1;
                                             }
                                             return 0;
                                         }
                                     });

                                     for(Pand p:panden){
                                         System.out.println(""+ p.distance(lat, lon) );
                                     }

                                     makeGrid(view);
                                 }

                                 @Override
                                 public void onFailure(Call<List<Pand>> call, Throwable t) {
                                     Toast.makeText(((RegioManagerActivity)getActivity()), "mislukt init panden", Toast.LENGTH_SHORT).show();

                                 }
                             }

        );




    }

    /**
     * aanmaken grid van incomplete panden
     * @param view
     */
    public void makeGrid(View view){

        // application context
        Context mContext = ((RegioManagerActivity)getActivity()).getApplicationContext();
        // grid met info van pand
        LinearLayout pandView;

        // grid met incomplete panden
        grid= view.findViewById(R.id.linearPanden);



        for(int i=0; i<panden.size(); i++){



            // layout van pandView
            pandView= new LinearLayout(mContext);
            pandView.setOrientation(LinearLayout.VERTICAL);
            pandView.setPadding((int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    (int)getResources().getDimension(R.dimen.dp_200),
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins((int)getResources().getDimension(R.dimen.dp_20),(int)getResources().getDimension(R.dimen.dp_20),(int)getResources().getDimension(R.dimen.dp_20),0 );
            pandView.setLayoutParams(params);
            pandView.setGravity(RelativeLayout.CENTER_HORIZONTAL);


            pandView.setBackgroundResource(R.drawable.grid_row_border);
            pandView.setPadding((int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10));

            ImageView img= new ImageView(mContext);

            RelativeLayout.LayoutParams lpImage= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,(int)getResources().getDimension(R.dimen.dp_150));

            lpImage.setMargins((int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10));
            img.setPadding((int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5));

            img.setLayoutParams(lpImage);


            // afbeelding bij pand aanvragen

            Call<JsonObject> afbeeldingCall=pandService.getAfbeeldingPand(panden.get(i).getId());
            afbeeldingCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    //your codes here

                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8)
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                .permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        //your codes here

                        try {
                            if(response.body()!=null) {
                                afbeelding = Base64.decode(response.body().get("afbeelding").getAsString(), Base64.DEFAULT);
                                afbeeldingBitMap = BitmapFactory.decodeByteArray(afbeelding, 0, afbeelding.length);
                                img.setImageBitmap(afbeeldingBitMap);

                            }
                            else{
                                img.setImageResource(R.drawable.noimage);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    t.printStackTrace();
                }
            });

            if(afbeelding!=null) {
                panden.get(i).setAfbeelding(afbeelding);
            }

            // schaduw maken bij lang klikken, zodat men kan slepen naar check om pand als compleet aan te duiden
            final int nmbr=i;
            pandView.setOnLongClickListener(new View.OnLongClickListener() {
                                          @Override
                                          public boolean onLongClick(View view) {
                                              ClipData data= ClipData.newPlainText("","");
                                              View.DragShadowBuilder myShadowBuilder = new View.DragShadowBuilder(view);
                                              view.startDrag(data, myShadowBuilder, view,0);

                                              return true;

                                          }
                                      }
            );

            BottomNavigationView bnav= ((RegioManagerActivity)getActivity()).findViewById(R.id.bottom_nav);
            ImageView check=((RegioManagerActivity)getActivity()).findViewById(R.id.compleet);

            pandView.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    int dragEvent=event.getAction();
                    switch (dragEvent){
                        case DragEvent.ACTION_DRAG_ENTERED:

                            bnav.setVisibility(View.GONE);
                            check.setVisibility(View.VISIBLE);
                            break;
                        case DragEvent.ACTION_DRAG_EXITED:

                            break;
                        case DragEvent.ACTION_DROP:
                            bnav.setVisibility(View.VISIBLE);
                            check.setVisibility(View.GONE);
                            break;
                    }
                    return true;
                }
            });

            final long pandId=panden.get(i).getId();
            pandView.setContentDescription(Long.toString(pandId));
            pandView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent intent= new Intent( ((RegioManagerActivity)getActivity()).getApplicationContext(), Activity_Regio_Options.class);
                    intent.putExtra("pandId", pandId);
                    startActivity(intent);
                }
            });


            myDialog = new Dialog(((RegioManagerActivity)getActivity()));
            pandView.setGravity(RelativeLayout.CENTER_HORIZONTAL);
            pandView.addView(img);
            TextView tv= new TextView(((RegioManagerActivity)getActivity()));
            tv.setText(panden.get(i).getStraat() +"\n" + panden.get(i).getStad());
            tv.setLayoutParams(new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            tv.setTextColor(Color.WHITE);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            pandView.addView(tv);

            grid.addView(pandView);




        }
    }



}
