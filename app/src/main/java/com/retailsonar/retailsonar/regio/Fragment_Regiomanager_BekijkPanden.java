package com.retailsonar.retailsonar.regio;

import android.Manifest;
import android.app.Dialog;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
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
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.R;
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
 * Activity bij ondersteuning overzicht van alle panden behorend
 * bij een bepaalde regiomanagers regio
 * Created by aaron on 3/23/2018.
 */

public class Fragment_Regiomanager_BekijkPanden extends Fragment {

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


    // plaats van gebruiker
    private FusedLocationProviderClient mFusedLocationClient;
    double lon;
    double lat;

    // lijst met panden van gebruiker
    List<Pand> panden;


    // layout xml
    LinearLayout grid;
    Dialog myDialog;

    // afbeeldingsupport
    byte[] afbeelding;
    Bitmap afbeeldingBitMap;


    /**
     * Methode bij aanmaken van view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regiomanager_panden, container, false);


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
                initPanden(user.getWerkRegio(), user.getWinkel(), view);
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });







        return view;
    }



    //panden opvragen die bij winkel van huidige user horen
    public void initPanden(String regio,String winkel, View view){

        Call<List<Pand>> regioPanden= pandService.getRegioPanden(regio, winkel);
        System.out.println(regioPanden.request().toString());
        try {

            // laatst gekende locatie opvragen
            mFusedLocationClient.getLastLocation().addOnFailureListener(((RegioManagerActivity)getActivity()), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            })
                    .addOnSuccessListener(((RegioManagerActivity)getActivity()), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                System.out.println("location "+location.toString());
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

        // regiopanden opvragen
        regioPanden.enqueue(new Callback<List<Pand>>() {
                                 @Override
                                 public void onResponse(Call<List<Pand>> call, Response<List<Pand>> response) {
                                     panden = response.body();

                                    // sorteren volgens afstand van gebruiker
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
                                         System.out.println("pand "+ p.getStraat()+" ligt op "+ p.distance(lat, lon)+" meter" );
                                     }

                                     makeGrid(view);
                                 }

                                 @Override
                                 public void onFailure(Call<List<Pand>> call, Throwable t) {
                                     Toast.makeText(((RegioManagerActivity)getActivity()), "mislukte initialisatie panden", Toast.LENGTH_SHORT).show();

                                 }
                             }

        );




    }

    /**
     * aanmaken van grid op meegegeven view met alle panden
     * @param view
     */
    public void makeGrid(View view){

        // application context
        Context mContext = ((RegioManagerActivity)getActivity()).getApplicationContext();
        // voor elk pand een linearlayout
        LinearLayout pandView;

        grid= view.findViewById(R.id.linearPanden);





        for(int i=0; i<panden.size(); i++){



            // pandView layout
            pandView= new LinearLayout(mContext);
            pandView.setOrientation(LinearLayout.VERTICAL);
            pandView.setPadding((int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5));
            LayoutParams params = new LayoutParams(
                    (int)getResources().getDimension(R.dimen.dp_200),
                    LayoutParams.WRAP_CONTENT
            );
            params.setMargins((int)getResources().getDimension(R.dimen.dp_20),(int)getResources().getDimension(R.dimen.dp_20),(int)getResources().getDimension(R.dimen.dp_20),0 );
            pandView.setLayoutParams(params);
            pandView.setGravity(RelativeLayout.CENTER_HORIZONTAL);
            pandView.setBackgroundResource(R.drawable.grid_row_border);
            pandView.setPadding((int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10));

            // image in pandView
            ImageView img= new ImageView(mContext);
            RelativeLayout.LayoutParams lpImage= new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,(int)getResources().getDimension(R.dimen.dp_150));
            lpImage.setMargins((int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10));
            img.setPadding((int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5));
            img.setLayoutParams(lpImage);

            // afbeelding van pand opvragen
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


            // pop up bij lang klikken op pand met korte informatie ( adres en oppervlakte )
            final int nmbr=i;
            pandView.setOnLongClickListener(new View.OnLongClickListener() {
                                          @Override
                                          public boolean onLongClick(View view) {
                                              showPopUp(view, nmbr);
                                              return true;

                                          }
                                      }
            );
            final long pandId=panden.get(i).getId();

            // doorverwijzen naar opties voor pand
            pandView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent intent= new Intent( ((RegioManagerActivity)getActivity()).getApplicationContext(), Activity_Regio_Options.class);
                    intent.putExtra("pandId", pandId);
                    startActivity(intent);
                }
            });

            // pop up initialiseren
            myDialog = new Dialog(((RegioManagerActivity)getActivity()));

            pandView.addView(img);
            TextView tv= new TextView(((RegioManagerActivity)getActivity()));
            tv.setText(panden.get(i).getStraat() +"\n " + panden.get(i).getStad());
            tv.setTextColor(Color.WHITE);
            tv.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            pandView.addView(tv);

            grid.addView(pandView);




        }
    }

    /**
     * tonen van popup menu behorend bij pand
     * @param v
     * @param i
     */
    public void showPopUp(View v, int i){

        TextView txtclose;
        TextView straat;
        TextView stad;
        TextView land;
        TextView oppervlakte;
        ImageView afbeeldingView;
        myDialog.setContentView(R.layout.pand_popup);

        // close button
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("X");
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });



        // info
        afbeeldingView=myDialog.findViewById(R.id.afbeelding);

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

                        afbeelding=Base64.decode(response.body().get("afbeelding").getAsString(), Base64.DEFAULT);
                        afbeeldingBitMap=BitmapFactory.decodeByteArray(afbeelding, 0, afbeelding.length);
                        afbeeldingView.setImageBitmap(afbeeldingBitMap);

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


        straat=myDialog.findViewById(R.id.straat);
        straat.setText(panden.get(i).getStraat());

        stad=myDialog.findViewById(R.id.stad);
        stad.setText(panden.get(i).getPostcode() +" " +panden.get(i).getStad());

        land=myDialog.findViewById(R.id.land);
        land.setText(panden.get(i).getLand());

        oppervlakte=myDialog.findViewById(R.id.oppervlakte);
        StringBuilder sb= new StringBuilder();
        sb.append(Double.toString(panden.get(i).getOppervlakte())).append(Html.fromHtml("m<sup>2</sup>"));
        oppervlakte.setText(sb.toString());



        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    /**
     * permission opvragen voor huidige locatie te kennen
     */
    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(((RegioManagerActivity)getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(((RegioManagerActivity)getActivity()),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(((RegioManagerActivity)getActivity()),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }



}
