package com.retailsonar.retailsonar;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.retailsonar.retailsonar.admin.AdminHome;
import com.retailsonar.retailsonar.entities.Login;
import com.retailsonar.retailsonar.expansie.ExpansieManagerHome;
import com.retailsonar.retailsonar.regio.RegioManagerActivity;
import com.retailsonar.retailsonar.entities.User;
import com.retailsonar.retailsonar.services.UserService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * support bij login
 * created by Aaron Hallaert
 */
public class LoginActivity extends AppCompatActivity {


    // initialisatie voor HTTP requests naar server
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            ;
    Retrofit retrofit=builder.build();
    UserService userService= retrofit.create(UserService.class);



    // fingerprint
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintHandler fingerprintHandler;
    private static final String FINGERPRINT_KEY = "key_name";
    private static final int REQUEST_USE_FINGERPRINT = 300;


    protected static Gson mGson;
    protected static CustomSharedPreference mPref;
    private static Login mUser;
    private static String userString;





    private static String token;

    /**
     * Create the main activity
     *
     * @param savedInstanceState previously saved instance data
     *
     * Author: Aaron Hallaert
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // "flash" tegengaan, nodig voor de sharedElementTransition smooth te laten verlopen
        getWindow().setEnterTransition(null);
        getWindow().getSharedElementEnterTransition().setDuration(2000);
        getWindow().getSharedElementReturnTransition().setDuration(2000);


        // logo animatie
        ImageView cont= findViewById(R.id.iconAnim);
        AnimatedVectorDrawable icon= (AnimatedVectorDrawable) cont.getDrawable();
        icon.start();


        // registreren van de actieve velden in het scherm

        Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        final TextView gebruikersNaamAdmin = (TextView) findViewById(R.id.editTextLogin);
        final TextView paswoordAdmin = (TextView) findViewById(R.id.editTextPassword);

        // logica als de knop ingedrukt wordt
        buttonLogin.setOnClickListener((view -> {
            login(gebruikersNaamAdmin.getText().toString(), paswoordAdmin.getText().toString());
        }
            ));



        //*****************************
        // FINGERPRINT
        //*****************************

        // vingerafdruk toevoegen starten
        TextView vingerAfdrukToevoegKnop= findViewById(R.id.addFingerprint);
        vingerAfdrukToevoegKnop.setOnClickListener((v)->{
            Intent intent= new Intent(this, AddFingerPrintActivity.class );
            startActivity(intent);
        });


        // indien er nog geen userdata is toegevoegd aan de applicatie
        // mogelijkheid geven om er toe te voegen, waardoor vingerafdruk mogelijk wordt
        if(!((CustomApplication)getApplication()).getShared().getUserData().equals("")){
            vingerAfdrukToevoegKnop.setVisibility(View.GONE);
            findViewById(R.id.fingerPrintImage).setVisibility(View.VISIBLE);
        }



        // init fingerprint attributes
        mGson = ((CustomApplication)getApplication()).getGsonObject();
        mPref = ((CustomApplication)getApplication()).getShared();

        fingerprintHandler = new FingerprintHandler(this);
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        // check support for android fingerprint on device
        checkDeviceFingerprintSupport();
        //generate fingerprint keystore
        generateFingerprintKeyStore();
        //instantiate Cipher class
        Cipher mCipher = instantiateCipher();
        if(mCipher != null){
            cryptoObject = new FingerprintManager.CryptoObject(mCipher);
        }

        // start fingerprint door op fingerprint logo te klikken
        ImageView fingerprintImage = findViewById(R.id.fingerPrintImage);
        fingerprintImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fingerprintHandler.completeFingerAuthentication(fingerprintManager, cryptoObject);
            }
        });

    }





    /**
     * inloggen van gebruiker, controle en doorverwijzing naar juiste pagina
     * aan de hand van groep waartoe user behoort
     *
     * @param name naam van gebruiker doorgegeven via textveld
     * @param password paswoord van gebruiker doorgegeven via textveld
     *
     * Author: Aaron Hallaert
     */
    private void login(String name, String password){

        // HTTP Call naar server
        Call<User> call=userService.login(name, password);
        //System.out.println(call.request().toString());


        call.enqueue(new Callback<User>(){
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // reponse.body()== ingelogde user

                if (response.isSuccessful()) {

                    // init token
                    StringBuilder sb= new StringBuilder();
                    sb.append("Bearer ").append(response.body().getToken());
                    token=sb.toString();

                    try {
                        //token opslaan in database
                        Call<User> callToken=userService.setToken(name, token);
                        //  System.out.println(callToken.request().toString());

                        // notificatie aan user bij succes of error
                        callToken.enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                Toast.makeText(LoginActivity.this, "Token Saved", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                Toast.makeText(LoginActivity.this, "Error on saving Token", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // nagaan tot welke groep de ingelogde user behoort en

                    if(response.body().getGroup().equals("Admin")) {

                        Toast.makeText(LoginActivity.this, "U bent admin", Toast.LENGTH_SHORT).show();
                        Intent startIntentNaarAdminHome = new Intent(getApplicationContext(), AdminHome.class);
                        // token nodig om in volgende activity huidige user op te vragen
                        startIntentNaarAdminHome.putExtra("Token", token);
                        startActivity(startIntentNaarAdminHome);
                    }
                    else if (response.body().getGroup().equals("Regiomanager")){

                        Toast.makeText(LoginActivity.this, "U bent regiomanager", Toast.LENGTH_SHORT).show();
                        Intent startIntentNaarRegioManagerHome = new Intent(getApplicationContext(), RegioManagerActivity.class);
                        // token nodig om in volgende activity huidige user op te vragen
                        startIntentNaarRegioManagerHome.putExtra("Token", token);
                        startActivity(startIntentNaarRegioManagerHome);
                    }
                    else if(response.body().getGroup().equals("Expansionmanager")){

                        Toast.makeText(LoginActivity.this, "U bent expansiemanager", Toast.LENGTH_SHORT).show();
                        Intent startIntentNaarExpansieManagerHome = new Intent(getApplicationContext(), ExpansieManagerHome.class);
                        // token nodig om in volgende activity huidige user op te vragen
                        startIntentNaarExpansieManagerHome.putExtra("Token", token);
                        startActivity(startIntentNaarExpansieManagerHome);
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Error bij doorverwijzing", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                    // indien authenticatie mislukt is, wordt er weergegeven aan de gebruiker dat credentials niet kloppen
                    Toast.makeText(LoginActivity.this, "Login not correct :(", Toast.LENGTH_SHORT).show();
                }
            }


            // het was niet mogelijk om verbinding te maken met de server
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(LoginActivity.this, "Error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * controle of apparaat compatibel is met fingerprint
     */
    private void checkDeviceFingerprintSupport() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, REQUEST_USE_FINGERPRINT);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!fingerprintManager.isHardwareDetected()) {
                    Toast.makeText(LoginActivity.this, "Fingerprint is not supported in this device", Toast.LENGTH_LONG).show();
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    Toast.makeText(LoginActivity.this, "Fingerprint not yet configured", Toast.LENGTH_LONG).show();
                }
            }
            if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(LoginActivity.this, "Screen lock is not secure and enable", Toast.LENGTH_LONG).show();
            }
            return;
        }
    }

    private void generateFingerprintKeyStore(){
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(FINGERPRINT_KEY, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        keyGenerator.generateKey();
    }

    private Cipher instantiateCipher(){
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyStore.load(null);
            SecretKey secretKey = (SecretKey)keyStore.getKey(FINGERPRINT_KEY, null);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnrecoverableKeyException |
                CertificateException | IOException | KeyStoreException | InvalidKeyException e) {
            throw new RuntimeException("Failed to instantiate Cipher class");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_USE_FINGERPRINT){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // check support for android fingerprint on device
                checkDeviceFingerprintSupport();
                //generate fingerprint keystore
                generateFingerprintKeyStore();
                //instantiate Cipher class
                Cipher mCipher = instantiateCipher();
                if(mCipher != null){
                    cryptoObject = new FingerprintManager.CryptoObject(mCipher);
                }
            }
            else{
                Toast.makeText(this, R.string.permission_refused, Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, getString(R.string.Unknown_permission_request), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * hulp bij fingerprint
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public class FingerprintHandler extends FingerprintManager.AuthenticationCallback{

        private final String TAG = FingerprintHandler.class.getSimpleName();

        private Context context;

        public FingerprintHandler(Context context){
            this.context = context;
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Log.d(TAG, "Error message " + errorCode + ": " + errString);
            Toast.makeText(context, context.getString(R.string.authenticate_fingerprint), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            Toast.makeText(context, R.string.auth_successful, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            userString = mPref.getUserData();
            mUser = mGson.fromJson(userString, Login.class);
            if(mUser != null){
                Toast.makeText(context, context.getString(R.string.auth_successful), Toast.LENGTH_LONG).show();


                // uiteindelijke logica indien alles succesvol was => het inloggen
                    // login with only fingerprint
                JSONObject jsonObj=new JSONObject();
                try {
                    System.out.println(((CustomApplication) getApplication()).getShared().getUserData());
                    jsonObj = new JSONObject(((CustomApplication)getApplication()).getShared().getUserData());
                    login(jsonObj.get("user").toString(), jsonObj.get("password").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }else{
                Toast.makeText(context, "You must register before login with fingerprint", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
        }

        public void completeFingerAuthentication(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            try{
                fingerprintManager.authenticate(cryptoObject, new CancellationSignal(), 0, this, null);
            }catch (SecurityException ex) {
                Log.d(TAG, "An error occurred:\n" + ex.getMessage());
            } catch (Exception ex) {
                Log.d(TAG, "An error occurred\n" + ex.getMessage());
            }
        }
    }


}



