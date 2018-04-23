package com.retailsonar.retailsonar.admin;

import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.User;
import com.retailsonar.retailsonar.services.UserService;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * create by Aaron Hallaert
 */
public class AdminHome extends AppCompatActivity {

    // HTTP Request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    User user;
    UserService userService= retrofit.create(UserService.class);


    // token
    private String token;


    /**
     * on create
     * @param savedInstanceState previously saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);


        // token wordt meegegeven vanuit login
        // token halen uit intent
        token=getIntent().getExtras().get("Token").toString();
        System.out.println(token);


        // logo met animatie
        ImageView cont= findViewById (R.id.iconAnim);
        AnimatedVectorDrawable icon= (AnimatedVectorDrawable) cont.getDrawable();
        icon.start();


        // animatie opnieuw starten van logo indien ten einde
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            icon.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    icon.start();
                }
            });
        }


        // button die doorverwijst naar site
        Button gaNaarSite=findViewById(R.id.buttonAdminSite);
        gaNaarSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder sb= new StringBuilder();
                sb.append(AppConstants.BASE_URL_SERVER).append("/ProtoWeb");
                Uri uriUrl = Uri.parse(sb.toString());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });
    }


}
