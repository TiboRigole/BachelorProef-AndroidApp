package com.retailsonar.retailsonar.expansie;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
 * created by Aaron Hallaert
 */
public class ExpansieManagerHome extends AppCompatActivity {

    // HTTP Request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    User user;
    UserService userService= retrofit.create(UserService.class);


    /**
     * on create
     * @param savedInstanceState previously saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expansiemanager_home);


        // button die doorverwijst naar site
        Button gaNaarSite=findViewById(R.id.buttonRegioSite);
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
