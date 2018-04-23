package com.retailsonar.retailsonar.regio;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.common.io.LineReader;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.User;
import com.retailsonar.retailsonar.services.HomeNewsLayout;
import com.retailsonar.retailsonar.services.UserService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static com.retailsonar.retailsonar.AppConstants.BUTTON_TEXT;
import static com.retailsonar.retailsonar.AppConstants.PREF_ACCOUNT_NAME;
import static com.retailsonar.retailsonar.AppConstants.REQUEST_ACCOUNT_PICKER;
import static com.retailsonar.retailsonar.AppConstants.REQUEST_AUTHORIZATION;
import static com.retailsonar.retailsonar.AppConstants.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.retailsonar.retailsonar.AppConstants.REQUEST_PERMISSION_GET_ACCOUNTS;
import static java.lang.Thread.sleep;

/**
 * Created by Aaron Hallaert on 4/2/2018.
 *
 * NewsFeed (+ Calendar @TODO )
 *
 */

public class Fragment_Regiomanager_Home extends Fragment
        implements EasyPermissions.PermissionCallbacks {

    // calendar
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    ProgressDialog mProgress;

    View view;


    // HTTP Request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    UserService userService= retrofit.create(UserService.class);


    // huidige user
    User user;
    String token;


    // nieuwsberichten
    JSONArray newsArray;

    // layout xml
    LinearLayout newsContainer;
    HomeNewsLayout newsScroll;
    ViewGroup.LayoutParams tlp;
    LinearLayout calendarContainerLayout;


    public Thread myThread;
    /**
     * on create view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState previously saved instance state
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_regiomanager_home,container, false);


        //************
        // UI
        //************
        //news
        newsScroll=view.findViewById(R.id.scrollNews);



        // autoscroll news
        myThread= new Thread(new Runnable() {
            @Override
            public void run() {


                while(getActivity()!=null) {

                    try {
                        myThread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (newsScroll.getScrollX()>(newsScroll.getChildAt(0).getMeasuredWidth() -
                                        ((RegioManagerActivity) getActivity()).getWindowManager().getDefaultDisplay().getWidth())) {
                                    newsScroll.setScrollX(0);
                                }
                                else {
                                    newsScroll.setScrollX(newsScroll.getScrollX() + (int) getResources().getDimension(R.dimen.dp_350));
                                }

                            }
                        });
                    }

                }
            }
        });

        myThread.start();

        // minimize news container
        ImageView minNews= view.findViewById(R.id.minNews);
        minNews.setOnClickListener((v) ->{
            if(newsScroll.getHeight()!=0) {
                view.findViewById(R.id.parentContNews).setForeground(null);
                newsScroll.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dp_350), 0));
            }
            else{
                view.findViewById(R.id.parentContNews).setForeground(getResources().getDrawable(R.drawable.round_corners_image));
                newsScroll.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dp_350), (int) getResources().getDimension(R.dimen.dp_220)));
            }

        });


        // agenda

        calendarContainerLayout = view.findViewById(R.id.calendarContainer);

        // minimize agenda feature
        ImageView minCalendar= view.findViewById(R.id.minCalendar);
        minCalendar.setOnClickListener((v) ->{
            if(calendarContainerLayout.getHeight()!=0) {
                view.findViewById(R.id.parentContCal).setForeground(null);
                calendarContainerLayout.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dp_350), 0));
            }
            else{
                view.findViewById(R.id.parentContCal).setForeground(getResources().getDrawable(R.drawable.round_corners_image));
                calendarContainerLayout.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.dp_350), ViewGroup.LayoutParams.WRAP_CONTENT));
            }

        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0);
        calendarContainerLayout.setLayoutParams(lp);
        calendarContainerLayout.setOrientation(LinearLayout.VERTICAL);
        //calendarContainerLayout.setPadding(16, 16, 16, 16);

        tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        /*
        mCallApiButton = new Button(getContext());
        mCallApiButton.setText(BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });
        activityLayout.addView(mCallApiButton);
        */

        mOutputText = new TextView(getContext());
        mOutputText.setTextColor(getResources().getColor(R.color.colorWit));
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Click the \'" + BUTTON_TEXT +"\' button to test the API.");
        calendarContainerLayout.addView(mOutputText);

        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Calling Google Calendar API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                ((RegioManagerActivity)getActivity()).getApplicationContext(), Arrays.asList(AppConstants.SCOPES))
                .setBackOff(new ExponentialBackOff());

        //*************
        // LOGICA
        //*************




        /* initialisatie huidige user */
        // token wordt meegegeven vanuit login
        Bundle extras = ((RegioManagerActivity)getActivity()).getIntent().getExtras();
        if(extras !=null) {
            token = extras.getString("Token");
        }

        // opvragen van huidige user : op succes, attribuut user initialiseren
        Call<User> currentUser=userService.getCurrentUser(token);
        currentUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                user=response.body();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
            }
        });


        // laden van nieuws
        laadNieuws();

        // laden van agenda
        getResultsFromApi();



        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
        //myThread.interrupt();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
       // myThread.interrupt();
    }

    @Override
    public void onResume(){
        super.onResume();
       /* if(myThread.isInterrupted()) {
            System.out.println("Thread werd geïnterrupt");
            myThread.start();
        }*/
    }


    public Thread getThread(){
        return myThread;
    }

    /**
     * nieuws inladen: init NewsArray met newsberichten als JsonObjecten
     *
     */
    private void laadNieuws(){

        // opvragen nieuws via HTTP request
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://newsapi.org/v2/everything?q=Vlaanderen&sortBy=publishedAt&language=nl&apiKey=8af0e18c22294643881c3cd35ded73a3")
                .get()
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "1e351912-e379-4686-bcf5-78f22d6e6163")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Toast.makeText(getContext(), "Nieuws laden mislukt", Toast.LENGTH_SHORT);
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

                String jsonData = response.body().string();

                try {
                    // nieuwsberichten onderscheiden en in array steken
                    JSONObject Jobject = new JSONObject(jsonData);
                    newsArray = Jobject.getJSONArray("articles");
                    try {
                        // UI nieuwspagina maken
                        setNewsPage(view, newsArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                catch(JSONException e){
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Bron uit JSONObject halen
     *
     * @param object nieuwsbericht
     * @return source
     */
    private String getSourceAsString(JSONObject object){
        try {
            return object.getJSONObject("source").get("name").toString();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * nieuwe intent opstarten naar browser
     *
     * @param url doel in browser
      */
    private void goToSource(Uri url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(url);
        startActivity(i);
    }


    /**
     * titel van nieuwsbericht uit JSONObject halen
     *
     * @param object nieuwsbericht
     * @return Titel van nieuwsbericht
     */
    private String getTitleAsString(JSONObject object){
        try {
            return object.get("title").toString();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * specifieke URI uit JSONObject halen
     *
     * @param object nieuwsbericht
     * @return URI van source bericht
     */
    private Uri getURIofNews(JSONObject object){
        try {
            Uri myUri = Uri.parse(object.get("url").toString());
            return myUri;
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     *
     * @param object nieuwsbericht
     * @return volledige beschrijving
     */
    private String getDescriptionAsString(JSONObject object){
        try {
            return object.get("description").toString();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     *
     * Byte array uit HTTP request naar bitmap omzetten
     * roept uiteindelijk "completeNews()"  aan om vervolg van newsfeed te setten en af te sluiten
     *
     * @param img ImageView waar afbeelding terecht dient te komen
     * @param nieuwsItem LinearLayout van nieuwsitem
     * @param newsContainer LinearLayout waar alle nieuwsitems in zitten
     * @param object NieuwsBericht (die onder andere afbeelding (url) bevat)
     */
    private void setImageOfNewsBitmap(ImageView img, LinearLayout nieuwsItem, LinearLayout newsContainer, JSONObject object){

        String url=null;
        try {
            if(object.get("urlToImage")!=null) {
                url = object.get("urlToImage").toString();
            }
        }
        catch(JSONException e){
            e.printStackTrace();
        }

        if(!url.equals("null")) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Postman-Token", "7224a082-3d0a-47db-b8a9-31ef53aac6e1")
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    byte[] data = response.body().bytes();
                    Bitmap bitmapNews;
                    bitmapNews = BitmapFactory.decodeByteArray(data, 0, data.length);
                    completeNews(img, nieuwsItem, newsContainer, object, bitmapNews);


                }
            });
        }
        else {

        }
    }

    /**
     *
     * tijdens deze methode dient men een afbeelding op te vragen
     *
     * @param view huidige view
     * @param newsArray nieuwsberichten
     * @throws JSONException
     */
    public void setNewsPage(View view, JSONArray newsArray) throws JSONException {

        Context mContext=((RegioManagerActivity)getActivity()).getApplicationContext();

        // container met alle nieuwsberichten
        newsContainer= view.findViewById(R.id.nieuwsContainer);

        // weergave van 20 nieuwsberichten
        for (int i = 0; i < newsArray.length(); i++) {
            JSONObject object = newsArray.getJSONObject(i);

            // nieuwsItem container + layout
            LinearLayout nieuwsItem=new LinearLayout(mContext);
            nieuwsItem.setOrientation(LinearLayout.VERTICAL);
            nieuwsItem.setPadding((int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    (int)getResources().getDimension(R.dimen.dp_350),
                    (int)getResources().getDimension(R.dimen.dp_250)
            );
            nieuwsItem.setLayoutParams(params);
            nieuwsItem.setGravity(RelativeLayout.CENTER_HORIZONTAL);




            // image bij nieuwsitem + layout
            ImageView img= new ImageView(mContext);

            // image uit nieuwsbericht halen
            // na deze methode wordt het zetten van de newsfeed vervolledigd in de methode "completeNews()" wegens mogelijke synchronisatiefouten
            setImageOfNewsBitmap(img, nieuwsItem, newsContainer, object);
            // layout horende bij afbeelding
            RelativeLayout.LayoutParams lpImage= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int)getResources().getDimension(R.dimen.dp_200));
            lpImage.setMargins((int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10),(int)getResources().getDimension(R.dimen.dp_10));
            img.setPadding((int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5),(int)getResources().getDimension(R.dimen.dp_5));
            img.setLayoutParams(lpImage);












        }

        // aangezien scrollview "snapt" naar een volgende item hebben we een custom scrollview,
        // in deze methode word de "snap" functionaliteit voorzien
        // dient uitgevoerd te worden op de UI Thread
        ((RegioManagerActivity)getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newsScroll.setFeatureItems(null);
            }
        });




    }


    /**
     *
     * bitmap wordt gekoppeld aan imageView
     * TextView met titel van nieuwsbericht
     * Button om naar source te gaan
     *
     * imageView en TextView worden toegekend aan nieuwsItem
     * nieuwsItem wordt uiteindelijk toegekend aan newsContainer
     *
     *
     * @param img ImageView
     * @param nieuwsItem LinearLayout waar nieuwsbericht in dient te komen
     * @param newsContainer LinearLayout voor alle nieuwsberichten
     * @param object huidig nieuwsbericht
     * @param bitmapNews bitmap met afbeelding bij huidig nieuwsbericht
     */
    public void completeNews(ImageView img, LinearLayout nieuwsItem, LinearLayout newsContainer, JSONObject object, Bitmap bitmapNews){
        ((RegioManagerActivity)getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // set image
                img.setImageBitmap(bitmapNews);

/*
                // set titel + layout
                TextView tv= new TextView(((RegioManagerActivity)getActivity()).getApplicationContext());
                tv.setText(getTitleAsString(object));
                tv.setLayoutParams(new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                tv.setTextColor(Color.WHITE);
                tv.setPadding(10,10,10,10);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
*/
                // set button naar source + layout
                Button goToSourceButton= new Button(((RegioManagerActivity)getActivity()).getApplicationContext());
                goToSourceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToSource(getURIofNews(object));
                    }
                });
                goToSourceButton.setBackground(getResources().getDrawable(R.drawable.ic_action_launch));
                android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(60,60);
                RelativeLayout.MarginLayoutParams marginButton= new RelativeLayout.MarginLayoutParams(60,60);

                marginButton.setMarginStart((int)getResources().getDimension(R.dimen.dp_300));
                goToSourceButton.setLayoutParams(lp);
                goToSourceButton.setLayoutParams(marginButton);


                // finalisatie
                nieuwsItem.addView(goToSourceButton);
                //nieuwsItem.addView(tv);
                nieuwsItem.addView(img);
                newsContainer.addView(nieuwsItem);



            }
        });
    }


    //********************
    // Google Calendar API
    //********************

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this.getContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = ((RegioManagerActivity)getActivity()).getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                ((RegioManagerActivity)getActivity()).getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            default:

                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) ((RegioManagerActivity)getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this.getContext());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this.getContext());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ((RegioManagerActivity)getActivity()),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Map<Integer, List<String>>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Map<Integer, List<String>> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private Map<Integer, List<String>> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Map<Integer, List<String>> eventStrings = new HashMap<>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (int i=0; i<items.size(); i++) {
                Event event=items.get(i);
                DateTime start = event.getStart().getDateTime();


                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                List<String> eigenschappen= new ArrayList<>();


                eigenschappen.add(start.toString());
                eigenschappen.add(event.getSummary());
                eigenschappen.add(event.getLocation());
                eigenschappen.add(event.getId());
                System.out.println(event.getLocation());

                eventStrings.put(i,
                        eigenschappen);
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Map<Integer, List<String>> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {

                //mOutputText.setText(TextUtils.join("\n", output));

                tlp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ViewGroup.MarginLayoutParams mlp=new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                mlp.setMargins(30,10,30,0);
                TextView eventText;
                TextView dayText;
                TextView dateText;
                TextView locatieText;
                calendarContainerLayout.setPadding(0,0,0,20);


                for(int i =0; i<output.size(); i++){
                    // volledige info DateTime format
                    String dateTime= output.get(i).get(0);
                    // splitsing dag en tijd
                    String dateAndTime[]= dateTime.split("T");
                    // datum in string zonder tijd
                    String datum= dateAndTime[0];

                    // dag in woorden
                    String dag="";
                    // vb. de 22ste
                    String dagGetal="";
                    String maand="";



                    String description= output.get(i).get(1);



                    String locatie=output.get(i).get(2);
                    System.out.println(locatie);

                    String id= output.get(i).get(3);

                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                    try {
                        Date date = format.parse(datum);
                        String datumAndDay[]= date.toString().split(" ");
                        dag=datumAndDay[0];
                        maand=datumAndDay[1];
                        dagGetal= datumAndDay[2];

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    eventText=new TextView(getContext());
                    StringBuilder sb= new StringBuilder();
                    sb.append(description);
                    eventText.setText(sb.toString());
                    eventText.setTextColor(getResources().getColor(R.color.colorWit));
                    eventText.setLayoutParams(tlp);
                    eventText.setLayoutParams(mlp);
                    eventText.setEllipsize(TextUtils.TruncateAt.END);
                    eventText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    eventText.setMaxLines(1);

                    locatieText=new TextView(getContext());
                    System.out.println("locatie: "+locatie);
                    locatieText.setText(locatie);
                    locatieText.setTextColor(getResources().getColor(R.color.colorWit));
                    locatieText.setLayoutParams(tlp);
                    locatieText.setLayoutParams(mlp);
                    locatieText.setEllipsize(TextUtils.TruncateAt.END);
                    locatieText.setMaxLines(1);
                    locatieText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


                    dayText=new TextView(getContext());
                    StringBuilder sb1= new StringBuilder();
                    sb1.append(dag);
                    dayText.setText(sb1.toString());
                    dayText.setTextColor(getResources().getColor(R.color.colorWit));
                    dayText.setLayoutParams(tlp);
                    dayText.setMovementMethod(new ScrollingMovementMethod());
                    dayText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    dateText=new TextView(getContext());
                    StringBuilder sb2= new StringBuilder();
                    sb2.append(dagGetal).append(" ").append(maand);
                    dateText.setText(sb2.toString());
                    dateText.setTextColor(getResources().getColor(R.color.colorWit));
                    dateText.setLayoutParams(tlp);
                    dateText.setMovementMethod(new ScrollingMovementMethod());
                    dateText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


                    LinearLayout.LayoutParams linksPar= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    LinearLayout.MarginLayoutParams parMar= new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.dp_60));
                    parMar.setMargins(50,0,50,50);


                    LinearLayout.LayoutParams rechtsPar= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.dp_60));
                    LinearLayout.LayoutParams overPar= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.dp_60));


                    LinearLayout overkoepelendDeel= new LinearLayout(getContext());
                    overkoepelendDeel.setOrientation(LinearLayout.HORIZONTAL);
                    overkoepelendDeel.setLayoutParams(overPar);
                    overkoepelendDeel.setLayoutParams(parMar);


                    LinearLayout linkseDeel= new LinearLayout(getContext());
                    linkseDeel.setOrientation(LinearLayout.VERTICAL);
                    linkseDeel.setLayoutParams(linksPar);
                    linkseDeel.setVerticalGravity(Gravity.CENTER_VERTICAL);
                    linkseDeel.setPadding(0,0,0,0);


                    LinearLayout rechtseDeel= new LinearLayout(getContext());
                    rechtseDeel.setBackground(getResources().getDrawable(R.drawable.round_corners_image));
                    rechtseDeel.setOrientation(LinearLayout.VERTICAL);
                    rechtseDeel.setLayoutParams(rechtsPar);
                    rechtseDeel.setLayoutParams(parMar);
                    rechtseDeel.setVerticalGravity(Gravity.CENTER_VERTICAL);


                    linkseDeel.addView(dayText);
                    linkseDeel.addView(dateText);

                    rechtseDeel.addView(eventText);
                    rechtseDeel.addView(locatieText);



                    overkoepelendDeel.addView(linkseDeel);
                    overkoepelendDeel.addView(rechtseDeel);




                    calendarContainerLayout.addView(overkoepelendDeel);
                    calendarContainerLayout.setVerticalScrollBarEnabled(true);
                    calendarContainerLayout.setNestedScrollingEnabled(true);

                }
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }


}
