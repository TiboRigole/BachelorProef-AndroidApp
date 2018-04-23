package com.retailsonar.retailsonar.regio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
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

/**
 * activity bij verschillende opties na selecteren pand
 * Created by aaron on 4/5/2018.
 */

public class Activity_Regio_Options extends AppCompatActivity {

    // HTTP request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    PandService pandService= retrofit.create(PandService.class);

    // huidig pand en parameters
    long currentPandId;
    Pand huidigPand;
    ArrayList<String> noodzParameters=new ArrayList<>();

    /**
     * tijdens aanmaken
     * @param savedInstanceState eerder opgeslagen instantie state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regio_options);


        //opvragen huidige pand
        currentPandId= (long) this.getIntent().getExtras().get("pandId");
        Call<Pand> pandRequest= pandService.getPandById(currentPandId);
        pandRequest.enqueue(new Callback<Pand>() {
            @Override
            public void onResponse(Call<Pand> call, Response<Pand> response) {
                huidigPand=new Pand(response.body());
                zoekParameters(huidigPand.getId());
            }

            @Override
            public void onFailure(Call<Pand> call, Throwable t) {
                t.printStackTrace();
            }
        });


        // verschillende opties
        LinearLayout afbeeldingToevoegen=findViewById(R.id.afbeeldingToevoegen);
        afbeeldingToevoegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), TakePicture_Activity.class);
                System.out.println("naar foto met pandId "+currentPandId);
                intent.putExtra("pandId", currentPandId);
                startActivity(intent);
            }
        });


        LinearLayout gaNaarLocatie=findViewById(R.id.gaNaarLocatie);
        gaNaarLocatie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), Activity_Locatie_Pand.class);
                System.out.println("naar locatie met pandId "+currentPandId);
                intent.putExtra("pandId", currentPandId);
                startActivity(intent);
            }
        });

        LinearLayout gaNaarParams=findViewById(R.id.gaNaarParams);
        gaNaarParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), Activity_Regio_ChangeParams.class);
                intent.putExtra("pandId", currentPandId);
                startActivity(intent);
            }
        });

        LinearLayout gaNaarAgenda= findViewById(R.id.gaNaarAgenda);
        gaNaarAgenda.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            StringBuilder title= new StringBuilder();
            title.append(huidigPand.getStraat()).append(" bezoeken");
            intent.putExtra("title", title.toString());
            intent.putExtra("description", "Bezoeken van pand");
            StringBuilder plaats= new StringBuilder();
            plaats.append(huidigPand.getStraat()).append(", ").append(huidigPand.getStad()).append(", ").append(huidigPand.getLand());
            intent.putExtra("location", plaats.toString());
            startActivity(intent);
        });

        // terug naar overzicht panden
        ImageButton backToPanden=findViewById(R.id.backToPanden);
        backToPanden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });


    }


    /**
     * alle parameters bij huidig pand initialiseren
     * @param pandId
     */
    public void zoekParameters(long pandId){
        Call<JsonObject> parameters=pandService.getNoodzakelijkeParamsPand(pandId);
        System.out.println(parameters.request().toString());
        parameters.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject obj= response.body();
                for(int i= 0; i<obj.size(); i++){
                    StringBuilder sb= new StringBuilder();
                    sb.append("parameter").append(i);
                    noodzParameters.add(obj.get(sb.toString()).toString());
                }

                for(String param: noodzParameters){
                    System.out.println(param);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
}
