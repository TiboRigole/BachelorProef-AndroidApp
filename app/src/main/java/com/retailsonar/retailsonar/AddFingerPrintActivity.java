package com.retailsonar.retailsonar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.retailsonar.retailsonar.admin.AdminHome;
import com.retailsonar.retailsonar.entities.Login;
import com.retailsonar.retailsonar.entities.User;
import com.retailsonar.retailsonar.expansie.ExpansieManagerHome;
import com.retailsonar.retailsonar.regio.RegioManagerActivity;
import com.retailsonar.retailsonar.services.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * toevoegen van shared pref userdata om fingerprint mogelijk te maken
 * bij inloggen via fingerprint zal dus de user ingelogd worden met de userdata die hier wordt ingevuld
 *
 * Author: Aaron Hallaert
 */
public class AddFingerPrintActivity extends AppCompatActivity {

    // initialisatie voor HTTP requests naar server
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            ;
    Retrofit retrofit=builder.build();
    UserService userService= retrofit.create(UserService.class);



    private EditText username;

    private EditText password;


    /**
     * gebeurt bij aanmaken van activity
     * @param savedInstanceState eerder opgeslagen saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_finger_print);

        setTitle("Android Fingerprint Registration");

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);




        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameValue = username.getText().toString();
                String passwordValue = password.getText().toString();

                authenticate(usernameValue, passwordValue);

            }
        });
    }


    /**
     * authenticatie van gebruiker
     *
     * @param name naam van gebruiker doorgegeven via textveld
     * @param passwordString paswoord van gebruiker doorgegeven via textveld
     *
     * Author: Aaron Hallaert
     */
    private void authenticate(String name, String passwordString){

        // HTTP Call naar server
        Call<User> call=userService.login(name, passwordString);
        //System.out.println(call.request().toString());


        call.enqueue(new Callback<User>(){
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // reponse.body()== ingelogde user

                if (response.isSuccessful()) {

                    // nagaan tot welke groep de ingelogde user behoort
                    if (response.body().getGroup().equals("Regiomanager")){

                        // opslaan van credentials
                        Gson gson = ((CustomApplication)getApplication()).getGsonObject();
                        Login userData = new Login(name, passwordString);
                        String userDataString = gson.toJson(userData);
                        CustomSharedPreference pref = ((CustomApplication)getApplication()).getShared();
                        pref.setUserData(userDataString);

                        username.setText("");
                        password.setText("");

                        // terug naar login activity
                        Intent loginIntent = new Intent(AddFingerPrintActivity.this, LoginActivity.class);
                        startActivity(loginIntent);

                    }
                    // indien user tot andere groep naast regiomanager behoort...
                    else{
                        Toast.makeText(AddFingerPrintActivity.this, "Fingerprint wordt enkel ondersteund voor regiomanagers", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                    // indien authenticatie mislukt is, wordt er weergegeven aan de gebruiker dat credentials niet kloppen
                    Toast.makeText(AddFingerPrintActivity.this, "Login not correct :(", Toast.LENGTH_SHORT).show();
                }
            }


            // het was niet mogelijk om verbinding te maken met de server
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(AddFingerPrintActivity.this, "Error: connection with server failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
