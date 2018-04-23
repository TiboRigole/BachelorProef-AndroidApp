package com.retailsonar.retailsonar.regio;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.SectionsStatePagerAdapter;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.entities.User;
import com.retailsonar.retailsonar.services.BottomNavigationViewHelper;
import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.UserService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * created by Aaron Hallaert
 *
 * Deze acitivity geeft 3 fragments weer aan de hand van een viewPager
 *
 */
public class RegioManagerActivity extends AppCompatActivity {


    // HTTP request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    UserService userService= retrofit.create(UserService.class);
    PandService pandService= retrofit.create(PandService.class);



    // fragment attributen
    private ViewPager mViewPager;
    BottomNavigationView bottom_nav;

    // huidige user
    User user;
    private String token;

    // aantal niet volledige ingevulde panden
    int aantalIncompleet;


    /**
     * on create activity
     * voornamelijk UI gebaseerde code
     *
     * @param savedInstanceState previously saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regiomanager);



        //**********************************
        // UI
        //**********************************


        // verschillende fragments handlen
        mViewPager=(ViewPager)findViewById(R.id.container);
        setupViewPage(mViewPager);

        // navigatie bar init
        setNavBar();

        // wanneer pagina verandert door swipe, verander ook bottom nav selected item
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(mViewPager.getCurrentItem()==0) {
                    bottom_nav.setSelectedItemId(R.id.nieuw);
                }
                if(mViewPager.getCurrentItem()==1) {
                    bottom_nav.setSelectedItemId(R.id.home);
                }
                if(mViewPager.getCurrentItem()==2) {
                    bottom_nav.setSelectedItemId(R.id.all);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        // notificaties toevoegen (nieuw ~ incomplete panden)
        BottomNavigationItemView nieuw= (BottomNavigationItemView) findViewById(R.id.nieuw);
        View badge= LayoutInflater.from(this).inflate(R.layout.notification_badge, bottom_nav, false);
        nieuw.addView(badge);




        // volledig scherm
        ConstraintLayout parentContainer=findViewById(R.id.parentContainer);
        // navbar
        BottomNavigationView bnav=findViewById(R.id.bottom_nav);



        //**********************************
        // LOGICA
        //**********************************

        // token halen uit intent
        token=getIntent().getExtras().get("Token").toString();
        // huidige user opvragen
        Call<User> currentUser=userService.getCurrentUser(token);
        currentUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                user=response.body();
                System.out.println(user.getName());

                setAantalIncompleet(user);

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });


        // ****************
        // pand completen
        // ****************

        // vinkje die verschijnt wanneer "pand" versleept wordt
        ImageView check=findViewById(R.id.compleet);

        // niets doen indien pand niet naar vinkje gesleept wordt
        parentContainer.setOnDragListener(new View.OnDragListener() {
            @Override
                public boolean onDrag(View v, DragEvent event) {
                    int dragEvent=event.getAction();
                    switch (dragEvent){
                        case DragEvent.ACTION_DRAG_ENTERED:

                            break;
                        case DragEvent.ACTION_DRAG_EXITED:

                            break;

                        case DragEvent.ACTION_DROP:
                            final View view= (View) event.getLocalState();
                            check.setVisibility(View.GONE);
                            bnav.setVisibility(View.VISIBLE);
                            break;
                    }
                    return true;
                }

        });


        // indien pand naar vinkje gesleept wordt, set pand completed, verwijder pand van view
        check.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                int dragEvent=event.getAction();
                switch (dragEvent){
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundColor(getResources().getColor(R.color.bpDarker_red));
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:

                        break;

                    case DragEvent.ACTION_DROP:
                        final View view= (View) event.getLocalState();

                       AsyncTask.execute(new Runnable() {
                           @Override
                           public void run() {

                               // pandId zit in contentDescription van RelativeLayout die naar dit vinkje gesleept wordt
                               pandService.getPandById(Long.parseLong(view.getContentDescription().toString())).enqueue(new Callback<Pand>() {
                                   @Override
                                   public void onResponse(Call<Pand> call, Response<Pand> response) {
                                       Pand x= response.body();

                                       x.setCompleted(1);
                                       try {
                                           // pand updaten, in dit geval wordt alleen completed op 1 gezet
                                           pandService.updatePand(x).execute();
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                                   }

                                   @Override
                                   public void onFailure(Call<Pand> call, Throwable t) {

                                   }
                               });
                           }
                       });


                       // view recreaten + zeker zijn dat RelativeLayout met gecomplete pand niet opnieuw verschijnt

                        recreate();
                        overridePendingTransition(0,0);
                        view.setVisibility(View.GONE);
                        setmViewPager(0);
                        break;
                }
                return true;
            }
        });


    }

    /**
     * fragments initialiseren die bij viewpager behoren
     * bottom navigatiebalk status initialiseren
     *
     * @param viewPager
     */
    private void setupViewPage(ViewPager viewPager){
        SectionsStatePagerAdapter adapter= new SectionsStatePagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new Fragment_Regiomanager_Incompleet(), "Regiomanager incomplete panden");
        adapter.addFragment(new Fragment_Regiomanager_Home(),"Regiomanager Home");
        adapter.addFragment(new Fragment_Regiomanager_BekijkPanden(), "Regiomanager alle panden");


        viewPager.setAdapter(adapter);
        bottom_nav=findViewById(R.id.bottom_nav);
        bottom_nav.setSelectedItemId(R.id.home);
        setmViewPager(1);

    }


    /**
     * wisselen van fragment
     *
     * @param fragmentNumber
     */
    public void setmViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }


    /**
     * bottom navigatie bar initialiseren
     */
    private void setNavBar(){
        /*navigatie bar controle*/
        bottom_nav=findViewById(R.id.bottom_nav);
        View view = bottom_nav.findViewById(R.id.home);
        view.performClick();

        // standaard "shift" animatie van navigatiebalk uitzetten
        BottomNavigationViewHelper.disableShiftMode(bottom_nav);


        // acties aan knoppen toevoegen, namelijk wisselen van fragments
        bottom_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.nieuw:
                        setmViewPager(0);
                        break;
                    case R.id.home:
                        setmViewPager(1);
                        break;
                    case R.id.all:
                        setmViewPager(2);
                        break;
                }
                return true;
            }
        });

    }


    /**
     * aantal incomplete panden opvragen via HTTP request naar server
     *
     * @param user huidige gebruiker
     */
    private void setAantalIncompleet(User user){

        // notifications bij nav item voor incomplete panden
        TextView badgeTekst=findViewById(R.id.notifications_badge_text);

        // aantal incomplete panden opvragen
        Call<JsonObject> aantalIncompletePanden=pandService.getAantalIncompletedWinkel(user.getWinkel(), user.getWerkRegio());
        aantalIncompletePanden.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                // notificatie badge updaten
                aantalIncompleet=Integer.parseInt(response.body().get("aantal").toString());
                badgeTekst.setText(Integer.toString(aantalIncompleet));


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });




    }





}
