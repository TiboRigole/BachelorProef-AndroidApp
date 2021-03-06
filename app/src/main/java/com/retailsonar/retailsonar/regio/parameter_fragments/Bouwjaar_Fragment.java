package com.retailsonar.retailsonar.regio.parameter_fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.codetroopers.betterpickers.datepicker.DatePickerBuilder;
import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.regio.Activity_Invul_Parameters;
import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.ParameterService;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by aaron on 4/10/2018.
 */

public class Bouwjaar_Fragment extends Fragment {
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
    Spinner spinYear;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bouwjaar, container, false);

        initPand(view);

        TextView naam= view.findViewById(R.id.naam);
        TextView beschrijving= view.findViewById(R.id.beschrijving);

        Call<JsonObject> eigenschappenRequest= parameterService.getEigenschappen("bouwjaar");
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


        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 1500; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(((Activity_Invul_Parameters)getActivity()),R.layout.spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinYear = (Spinner)view.findViewById(R.id.spinner);
        spinYear.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        spinYear.setAdapter(adapter);
        if(huidigPand.getBouwjaar()!=0) {
            spinYear.setSelection(adapter.getPosition(Integer.toString(huidigPand.getBouwjaar())));
        }




        return view;
    }

    @Override
    public void onPause() {
        Log.e("DEBUG", "OnPause of loginFragment");
        ((Activity_Invul_Parameters)getActivity()).getPand().setBouwjaar(Integer.parseInt(spinYear.getSelectedItem().toString()));
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void initPand(View view) {
        huidigPand= ((Activity_Invul_Parameters)getActivity()).getPand();

    }

    private void naarVolgendeFrame(){
        ((Activity_Invul_Parameters)getActivity()).naarVolgendeFrame();
    }







}
