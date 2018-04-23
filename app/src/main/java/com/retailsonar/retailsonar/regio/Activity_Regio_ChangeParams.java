package com.retailsonar.retailsonar.regio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.ParameterService;

import java.lang.reflect.Field;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Activity bij overzicht van parameters huidig pand
 * Created by aaron on 4/7/2018.
 */

public class Activity_Regio_ChangeParams extends AppCompatActivity {

    // HTTP request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    PandService pandService= retrofit.create(PandService.class);
    ParameterService parameterService= retrofit.create(ParameterService.class);


    // huidige pand met eigenschappen
    Pand huidigPand;
    long currentPandId;
    ArrayList<String> params=new ArrayList<>();

    // Textviews en linearlayouts voor parameters
    ArrayList<TextView> parameterViews= new ArrayList<>();
    ArrayList<LinearLayout> parameterParent=new ArrayList<>();
    // json object met alle eigenschappen van een parameter
    JsonObject eigenschappen;

    // container voor alle parameters
    LinearLayout container;


    // bovenste toolbar met zoekmogelijkheid
    Toolbar toolbar, searchtollbar;
    Menu search_menu;
    MenuItem item_search;


    /**
     * bij aanmekan van activity
     * @param savedInstances eerder opgeslagen instanties
     */
    @Override
    public void onCreate(Bundle savedInstances){
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_overzicht_params);

        // huidig pand initialiseren
        currentPandId=(long) getIntent().getExtras().get("pandId");
        Call<Pand> getCurrentPand= pandService.getPandById(currentPandId);
        getCurrentPand.enqueue(new Callback<Pand>() {
            @Override
            public void onResponse(Call<Pand> call, Response<Pand> response) {
                huidigPand=response.body();
                // alle parameters ophalen
                initParams();

            }

            @Override
            public void onFailure(Call<Pand> call, Throwable t) {

            }
        });

        // toolbars init
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Parameters");
        setSearchtollbar();

    }


    /**
     * aanvragen parameters, toevoegen aan lijst, daarna GUI lijst maken met parameters
     */
    public void initParams(){
        Call<JsonObject> paramsJson=pandService.getNoodzakelijkeParamsPand(currentPandId);
        paramsJson.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject obj= response.body();
                for(int i=0; i<obj.size(); i++){
                    String parameter=obj.get("parameter"+i).getAsString();
                    params.add(parameter);
                }


                makeList();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }


    /**
     * eigenschappen van parameter opvragen
     * @param parameter naam van parameter
     * @param p index in lijst van parameters
     */
    public void getEigenschappen(String parameter, int p){
        Call<JsonObject> eigenschappenRequest= parameterService.getEigenschappen(parameter);
        System.out.println(eigenschappenRequest.request().toString());

        eigenschappenRequest.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                eigenschappen= response.body();
                // verder lijst vervolledigen
                vervolledigParameter(p, parameter);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });


    }

    /**
     * Vervolledigen van lijst met parameters met score
     *
     * @param p index van lijst parameter
     * @param parameter naam parameter
     */
    public void vervolledigParameter(int p, String parameter){

        // GUI dimensies
        int px45dp = (int) getResources().getDimension(R.dimen.dp_45);
        int px10dp = (int) getResources().getDimension(R.dimen.dp_10);

        // container voor volgende parameter
        LinearLayout volgendeParam = new LinearLayout(this.getApplicationContext());
        volgendeParam.setBackgroundColor(Color.parseColor("#32000000"));
        volgendeParam.setOrientation(LinearLayout.HORIZONTAL);
        volgendeParam.setGravity(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px45dp,5);
        volgendeParam.setLayoutParams(layoutParams);

        // naam van parameter
        TextView naam = new TextView(this.getApplicationContext());
        LinearLayout.LayoutParams layoutText= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 4);
        naam.setLayoutParams(layoutText);
        naam.setGravity(Gravity.CENTER_HORIZONTAL);
        naam.setPadding(px10dp, px10dp, px10dp, px10dp);
        naam.setText(eigenschappen.get("naam").getAsString());
        naam.setTextColor(Color.WHITE);
        parameterViews.add(naam);

        // score/waarde van parameter
        TextView score = new TextView(this.getApplicationContext());
        LinearLayout.LayoutParams layoutScore= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutScore.setMarginEnd(0);
        score.setLayoutParams(layoutScore);
        score.setGravity(Gravity.CENTER_HORIZONTAL);
        score.setPadding(px10dp, px10dp, px10dp, px10dp);
        // onderscheid maken tussen score en waarde
        if(eigenschappen.get("type").getAsString().equals("score") || eigenschappen.get("type").getAsString().equals("optie")) {
            StringBuilder sb= new StringBuilder();
            int punten;
            punten = (int)getPunten(parameter);
            sb.append(punten).append("/").append(eigenschappen.get("aantal"));
            score.setText(sb.toString());
        }
        else{
            StringBuilder sb= new StringBuilder();
            if(parameter.equals("bouwjaar")){
             int punten;
             punten= (int)getPunten(parameter);
             sb.append(punten);
            }
            else {
                double punten;
                punten = getPunten(parameter);
                sb.append(punten);
            }
            score.setText(sb.toString());
        }
        score.setTextColor(Color.WHITE);

        // click listener toevoegen zodat men rechtstreeks naar fragment van parameter kan gaan
        volgendeParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(v.getContext(), Activity_Invul_Parameters.class);
                intent.putExtra("pandId", currentPandId);
                intent.putExtra("parameter",naam.getText());
                startActivity(intent);
            }
        });

        // afronden
        volgendeParam.addView(naam);
        volgendeParam.addView(score);
        parameterParent.add(volgendeParam);
        container.addView(volgendeParam);
    }

    /**
     * lijst van parameters initialiseren
     */
    public void makeList(){
        container=findViewById(R.id.params_container);

        for(int p=0; p<params.size(); p++) {
            String parameter=params.get(p);
            // na het verkrijgen van eigenschappen kan lijst gecomplete worden
            getEigenschappen(parameter, p);
        }
    }

    /**
     * waarde van parameter huidig pand vragen
     * @param parameter naam van parameter
     * @return punten
     */
    public double getPunten(String parameter){
        if(parameter.equals("commercieleActiviteit")){
            return Integer.parseInt(huidigPand.getCommercieleActiviteit());
        }
        else if(parameter.equals("education")){
            return huidigPand.getEducation();
        }
        else if(parameter.equals("parking")){
            return huidigPand.getParking();
        }
        else if(parameter.equals("publiekTransport")){
            return huidigPand.getPubliekTransport();
        }
        else if(parameter.equals("oppervlakte")){
            return huidigPand.getOppervlakte();
        }
        else if(parameter.equals("lengtevoorgevel")){
            return huidigPand.getLengtevoorgevel();
        }

        else if(parameter.equals("bouwjaar")){
            return huidigPand.getBouwjaar();
        }
        else if(parameter.equals("passage")){
            return huidigPand.getPassage();
        }

        else if(parameter.equals("toegankelijkheid")){
            return huidigPand.getToegankelijkheid();
        }
        else if(parameter.equals("microtoegankelijkheid")){
            return huidigPand.getMicrotoegankelijkheid();
        }
        else if(parameter.equals("shopareaappreciation")){
            return huidigPand.getShopareaappreciation();
        }
        else if(parameter.equals("correctiefactor")){
            return huidigPand.getCorrectiefactor();
        }
        else if(parameter.equals("lokaalmonopolie")){
            return huidigPand.getLokaalmonopolie();
        }

        else{
            return 0;
        }
    }

    /**
     * menu openen in toolbar
     * @param menu menu met start en refresh
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * bij selectie uit menu (in toolbar)
     * @param item item waarop geklikt werd
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_start:
                // naar activity die alle frames overloopt van nodige parameters
                Intent intent= new Intent(this.getApplicationContext(), Activity_Invul_Parameters.class);
                intent.putExtra("pandId", currentPandId);
                intent.putExtra("parameter","");
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                recreate();
                return true;
            case R.id.action_search:
                // open search
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    circleReveal(R.id.searchtoolbar,1,true,true);
                else
                    searchtollbar.setVisibility(View.VISIBLE);

                item_search.expandActionView();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * search toolbar + animatie circle reveal mogelijk maken
     */
    public void setSearchtollbar()
    {
        searchtollbar = (Toolbar) findViewById(R.id.searchtoolbar);
        if (searchtollbar != null) {
            searchtollbar.inflateMenu(R.menu.menu_search);
            search_menu=searchtollbar.getMenu();

            searchtollbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        circleReveal(R.id.searchtoolbar,1,true,false);
                    else
                        searchtollbar.setVisibility(View.GONE);
                }
            });

            item_search = search_menu.findItem(R.id.action_filter_search);

            MenuItemCompat.setOnActionExpandListener(item_search, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // Do something when collapsed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        circleReveal(R.id.searchtoolbar,1,true,false);

                    }
                    else
                        searchtollbar.setVisibility(View.GONE);


                    for(int i=0; i<parameterViews.size();i++){

                        parameterParent.get(i).setVisibility(View.VISIBLE);

                    }
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {

                    return true;
                }
            });


            initSearchView();


        } else
            Log.d("toolbar", "setSearchtollbar: NULL");
    }

    /**
     * logica achter search view
     */
    public void initSearchView()
    {
        final SearchView searchView =
                (SearchView) search_menu.findItem(R.id.action_filter_search).getActionView();

        // Enable/Disable Submit button in the keyboard

        searchView.setSubmitButtonEnabled(false);

        // Change search close button image

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_close);


        // set hint and the text colors

        EditText txtSearch = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint("Zoek parameter..");
        txtSearch.setHintTextColor(Color.DKGRAY);
        txtSearch.setTextColor(getResources().getColor(R.color.colorBlack));


        // set the cursor

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callSearch(newText);
                return true;
            }

            public void callSearch(String query) {
                //Do searching
                for(int i=0; i<parameterViews.size();i++){

                    if (!parameterViews.get(i).getText().toString().toLowerCase().contains(query.toLowerCase())){
                        parameterParent.get(i).setVisibility(View.GONE);
                    }
                    else{
                        parameterParent.get(i).setVisibility(View.VISIBLE);
                    }
                }
                Log.i("query", "" + query);

            }

        });

    }


    /**
     * circle reveal on search
     * @param viewID
     * @param posFromRight
     * @param containsOverflow
     * @param isShow
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow)
    {
        final View myView = findViewById(viewID);

        int width=myView.getWidth();

        if(posFromRight>0)
            width-=(posFromRight*getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material))-(getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)/ 2);
        if(containsOverflow)
            width-=getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

        int cx=width;
        int cy=myView.getHeight()/2;

        Animator anim;
        if(isShow)
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0,(float)width);
        else
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float)width, 0);

        anim.setDuration((long)220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!isShow)
                {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // make the view visible and start the animation
        if(isShow)
            myView.setVisibility(View.VISIBLE);

        // start the animation
        anim.start();


    }
}
