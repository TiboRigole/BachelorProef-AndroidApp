package com.retailsonar.retailsonar;

import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.UrlQuerySanitizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import static java.lang.Thread.sleep;

/**
 * Ondersteuning eerste pagina
 *
 * created by Aaron Hallaert on 4/11/2018.
 */

public class SplashActivity extends AppCompatActivity {
    /**
     * Create the main activity.
     * Logo animatie uitvoeren gedurende 3 seconden, verder gaan met sharedElementTransition
     * naar LoginActivity
     *
     * @param savedInstanceState previously saved instance data.
     *
     * Author: Aaron Hallaert
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_splash_screen);


        // "flash" tegengaan bij overgang (sharedElementTransition dient smooth te verlopen)
        this.getWindow().setExitTransition(null);


        // logo met animatie
        ImageView cont= findViewById(R.id.iconAnim);
        AnimatedVectorDrawable icon= (AnimatedVectorDrawable) cont.getDrawable();
        icon.start();


        Thread myThread= new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // overgaan naar loginActivity na 3 seconden
                    sleep(3000);


                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            Pair<View, String> p1= new Pair<>(findViewById(R.id.background), "background");
                            Pair<View, String> p2= new Pair<>(findViewById(R.id.iconAnim), "logo_transition");

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashActivity.this, p1, p2);
                            getWindow().getSharedElementEnterTransition().setDuration(2000);
                            getWindow().getSharedElementReturnTransition().setDuration(2000);

                            // "flash" tegengaan tijdens transitie naar volgende intent
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent, options.toBundle());

                            // activity sluiten
                            finish();

                            // "flash" tegengaan
                            SplashActivity.this.overridePendingTransition(R.transition.default_window_fade,R.transition.default_window_fade);
                            SplashActivity.this.getWindow().setExitTransition(null);




                        }
                    });

                }
                catch(Exception e){
                    e.printStackTrace();
                }



            }
        });



        myThread.start();

    }



}
