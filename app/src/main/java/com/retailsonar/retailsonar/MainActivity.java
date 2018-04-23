package com.retailsonar.retailsonar;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    /**
     *
     * DEZE ACTIVITY WORDT OVERGESLAAN WEGENS ONNODIGE KLIK
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     *
     * Author: Aaron Hallaert
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // logo zoeken en starten
        ImageView cont= findViewById(R.id.iconAnim);
        AnimatedVectorDrawable icon= (AnimatedVectorDrawable) cont.getDrawable();
        icon.start();


        // button zoeken en functie geven
        Button naarLogin = (Button) findViewById(R.id.buttonLogin);

            //logica om naar de volgende scherm te gaan
        naarLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

                // logo samen met tranisition_name in een pair om hieronder te gebruiken in makeSceneTransitionAnimation
                Pair<View, String> p1= new Pair<>(findViewById(R.id.iconAnim), "logo_transition");

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, p1);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(intent, options.toBundle());

            }
        });















    }
}
