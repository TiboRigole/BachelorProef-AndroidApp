package com.retailsonar.retailsonar.regio;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import com.codetroopers.betterpickers.widget.PageIndicator;
import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.SectionsStatePagerAdapter;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.regio.parameter_fragments.Bouwjaar_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.CommercieleActiviteit_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.CorrectieFactor_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.Education_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.LengteVoorgevel_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.LokaleMonopolie_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.MicroToegankelijkheid_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.Oppervlakte_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.Parking_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.Passage_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.PubliekTransport_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.ShopAreaAppreciation_Fragment;
import com.retailsonar.retailsonar.regio.parameter_fragments.Toegankelijkheid_Fragment;
import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.UserService;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * activity met alle fragments om parameters in te vullen
 * Created by aaron on 4/10/2018.
 */

public class Activity_Invul_Parameters extends AppCompatActivity {

    // fragment attributen
    private ViewPager mViewPager;

    // HTTP request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    UserService userService= retrofit.create(UserService.class);
    PandService pandService= retrofit.create(PandService.class);
    Map<String,Integer> fragmentPairs=new HashMap<>();
    Pand pand;
    long pandId;

    // ViewPager
    int oldPos;
    SpringDotsIndicator springDotsIndicator;

    ArrayList<String> params= new ArrayList<>();


    // start pagina
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invul_param);

        // pandId uit intent halen die meegegeven werd door vorige activity
        // initialisatie van pand
        pandId= (long) getIntent().getExtras().get("pandId");
        Call<Pand> pandCall=pandService.getPandById(pandId);
        pandCall.enqueue(new Callback<Pand>() {
            @Override
            public void onResponse(Call<Pand> call, Response<Pand> response) {
                pand=response.body();
            }

            @Override
            public void onFailure(Call<Pand> call, Throwable t) {
                t.printStackTrace();
            }
        });

        // parameters van pand initialiseren
        initParams();




        Button volgende = findViewById(R.id.volgende);
        volgende.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                naarVolgendeFrame();
            }
        });
    }


    /**
     * setup viewpager met nodige fragments
     * @param viewPager pager die alle fragments moet bevatten
     */
    private void setupViewPage(ViewPager viewPager){
        SectionsStatePagerAdapter adapter= new SectionsStatePagerAdapter(getSupportFragmentManager());
        int fragment=0;

        if(params.contains("lengtevoorgevel")) {
           adapter.addFragment(new LengteVoorgevel_Fragment(), "Lengte Voorgevel");
           fragmentPairs.put("Lengte Voorgevel", fragment);
            fragment++;
        }
        if(params.contains("oppervlakte")) {
            adapter.addFragment(new Oppervlakte_Fragment(), "Oppervlakte");
            fragmentPairs.put("Oppervlakte", fragment);
            fragment++;
        }
        if(params.contains("parking")) {
            adapter.addFragment(new Parking_Fragment(), "Parking");
            fragmentPairs.put("Parking", fragment);
            fragment++;
        }
        if(params.contains("commercieleActiviteit")) {
            adapter.addFragment(new CommercieleActiviteit_Fragment(), "Commerciële Activiteit");
            fragmentPairs.put("Commerciële Activiteit", fragment);
            fragment++;
        }
        if(params.contains("publiekTransport")) {
            adapter.addFragment(new PubliekTransport_Fragment(), "Publiek Transport");
            fragmentPairs.put("Publiek Transport", fragment);
            fragment++;
        }
        if(params.contains("education")) {
            adapter.addFragment(new Education_Fragment(), "Nabijheid van scholen");
            fragmentPairs.put("Nabijheid van scholen", fragment);
            fragment++;
        }
        if(params.contains("bouwjaar")) {
            adapter.addFragment(new Bouwjaar_Fragment(), "Bouwjaar / Laatste Renovatie");
            fragmentPairs.put("Bouwjaar / Laatste Renovatie", fragment);
            fragment++;
        }
        if(params.contains("passage")) {
            adapter.addFragment(new Passage_Fragment(), "Passage");
            fragmentPairs.put("Passage", fragment);
            fragment++;
        }
        if(params.contains("toegankelijkheid")) {
            adapter.addFragment(new Toegankelijkheid_Fragment(), "Toegankelijkheid");
            fragmentPairs.put("Toegankelijkheid", fragment);
            fragment++;
        }
        if(params.contains("microtoegankelijkheid")) {
            adapter.addFragment(new MicroToegankelijkheid_Fragment(), "Microtoegankelijkheid");
            fragmentPairs.put("Microtoegankelijkheid", fragment);
            fragment++;
        }
        if(params.contains("shopareaappreciation")) {
            adapter.addFragment(new ShopAreaAppreciation_Fragment(), "Shop Area Appreciation");
            fragmentPairs.put("Shop Area Appreciation", fragment);
            fragment++;
        }
        if(params.contains("correctiefactor")) {
            adapter.addFragment(new CorrectieFactor_Fragment(), "Correctiefactor");
            fragmentPairs.put("Correctiefactor", fragment);
            fragment++;
        }
        if(params.contains("lokaalmonopolie")) {
            adapter.addFragment(new LokaleMonopolie_Fragment(), "Lokale Monopolie");
            fragmentPairs.put("Lokale Monopolie", fragment);
            fragment++;
        }

        System.out.println(adapter.getCount());
        viewPager.setAdapter(adapter);
        springDotsIndicator = (SpringDotsIndicator) findViewById(R.id.spring_dots_indicator);
        springDotsIndicator.setViewPager(viewPager);
        springDotsIndicator.setDotsClickable(false);


        String parameterExtra=(String) getIntent().getExtras().get("parameter");
        if(parameterExtra.equals("")) {
            setmViewPager(0);
        }
        else{
            int x= fragmentPairs.get(parameterExtra);
            System.out.println("parameter die is doorgegeven: "+fragmentPairs.get(parameterExtra));
            setmViewPager(x);
        }
        oldPos=mViewPager.getCurrentItem();
        /*
        Resources r = getResources();
        int px10dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
        int px5dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        int px200dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics());
        int px100dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());
        int px150dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, r.getDisplayMetrics());
        int px52dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, r.getDisplayMetrics());
*/
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {



            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(position > oldPos) {
                    //Moving to the right

                    System.out.println("naar rechts");

                } else if(position < oldPos) {
                    //Moving to the Left

                    System.out.println("naar links");
                }

                oldPos=mViewPager.getCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * zichtbaar fragment veranderen
     * @param fragmentNumber nummer van fragment
     */
    public void setmViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }


    public Pand getPand(){
        return pand;
    }


    /**
     * opvragen van nodige parameters
     * hierna kunnen fragments toegevoegd worden aan viewpager
     */
    public void initParams(){
        Call<JsonObject> paramsJson=pandService.getNoodzakelijkeParamsPand(pandId);
        paramsJson.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                // object bevat alle parameters
                JsonObject obj= response.body();

                for(int i=0; i<obj.size(); i++) {
                    String parameter = obj.get("parameter" + i).getAsString();
                    params.add(parameter);
                }



                mViewPager=(ViewPager)findViewById(R.id.formulier);
                setupViewPage(mViewPager);

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    /**
     * navigeren naar volgende frame/parameter
     */
    public void naarVolgendeFrame(){
        int current= mViewPager.getCurrentItem();
        if(current<mViewPager.getAdapter().getCount()-1) {
            setmViewPager(current + 1);
        }
        // bij laatste frame pand updaten naar databank en activity afsluiten
        else{
            updatePand();
            finish();
        }
    }


    //********************************************************************************************
    // Op elke mogelijke manier van sluiten van de activiteit, pand update sturen naar databank
    //********************************************************************************************

    @Override
    public void onStop() {
        super.onStop();
        updatePand();
    }

    @Override
    public void onPause() {
        super.onPause();
        updatePand();
    }


    public void updatePand() {
        Call<String> updateCall=pandService.updatePand(pand);
        System.out.println(updateCall.request());
        updateCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                System.out.println("pand geupdate naar databank");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
}
