package com.retailsonar.retailsonar.regio.parameter_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class Oppervlakte_Fragment extends Fragment {
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
    EditText oppervlakte;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_oppervlakte, container, false);

        initPand(view);

        TextView naam= view.findViewById(R.id.naam);
        TextView beschrijving= view.findViewById(R.id.beschrijving);

        Call<JsonObject> eigenschappenRequest= parameterService.getEigenschappen("oppervlakte");
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

        oppervlakte=view.findViewById(R.id.editOppervlakte);
        oppervlakte.setText(Double.toString(huidigPand.getOppervlakte()));



        return view;
    }
    @Override
    public void onPause() {
        Log.e("DEBUG", "OnPause of loginFragment");
        ((Activity_Invul_Parameters)getActivity()).getPand().setOppervlakte(Double.parseDouble(oppervlakte.getText().toString()));
        super.onPause();
    }

    private void initPand(View view) {
        huidigPand= ((Activity_Invul_Parameters)getActivity()).getPand();

    }

    private void naarVolgendeFrame(){
        ((Activity_Invul_Parameters)getActivity()).naarVolgendeFrame();
    }


}
