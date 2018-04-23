package com.retailsonar.retailsonar.regio.parameter_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.regio.Activity_Invul_Parameters;
import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.ParameterService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by aaron on 4/10/2018.
 */

public class LokaleMonopolie_Fragment extends Fragment {
    Pand huidigPand;
    View view;

    // HTTP request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    PandService pandService= retrofit.create(PandService.class);
    ParameterService parameterService= retrofit.create(ParameterService.class);
    JsonObject eigenschappen;
    ImageView[] afbeeldingen=new ImageView[2];
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lokalemonopolie, container, false);


        initPand(view);

        TextView naam= view.findViewById(R.id.naam);
        TextView beschrijving= view.findViewById(R.id.beschrijving);

        Call<JsonObject> eigenschappenRequest= parameterService.getEigenschappen("lokaalmonopolie");
        System.out.println(eigenschappenRequest.request().toString());
        eigenschappenRequest.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                eigenschappen= response.body();
                naam.setText(eigenschappen.get("naam").getAsString());
                beschrijving.setText(eigenschappen.get("beschrijving").getAsString());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

        afbeeldingen[0]=view.findViewById(R.id.lokmon1);
        afbeeldingen[1]=view.findViewById(R.id.lokmon2);
        getScore();

        afbeeldingen[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huidigPand.setLokaalmonopolie(0);
                getScore();
            }
        });

        afbeeldingen[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huidigPand.setLokaalmonopolie(1);
                getScore();
            }
        });






        return view;
    }

    public void getScore(){
        if(huidigPand.getLokaalmonopolie()==0){
            afbeeldingen[0].setBackgroundColor(getResources().getColor(R.color.bpDarker_red));
            afbeeldingen[1].setBackgroundColor(0);

        }
        if(huidigPand.getLokaalmonopolie()==1){
            afbeeldingen[1].setBackgroundColor(getResources().getColor(R.color.bpDarker_red));
            afbeeldingen[0].setBackgroundColor(0);

        }

    }

    @Override
    public void onPause() {
        Log.e("DEBUG", "OnPause of loginFragment");
        ((Activity_Invul_Parameters)getActivity()).getPand().setLokaalmonopolie(huidigPand.getLokaalmonopolie());
        super.onPause();
    }

    private void initPand(View view) {
        huidigPand= ((Activity_Invul_Parameters)getActivity()).getPand();

    }
    private void naarVolgendeFrame(){
        ((Activity_Invul_Parameters)getActivity()).naarVolgendeFrame();
    }



}
