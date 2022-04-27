package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreenActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    ImageView img, imgabove;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);


       /* Intent intent = VpnService.prepare(getApplicationContext());
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }*/


        try {
            // make fullscreen
            getWindow ().requestFeature (Window.FEATURE_NO_TITLE);
            getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView (R.layout.activity_splash_screen);


            firebaseAuth = FirebaseAuth.getInstance ();

            img = findViewById(R.id.iconIv);
            imgabove=findViewById(R.id.image1);
            //start login activity after 5 second
            new Handler ().postDelayed (new Runnable () {
                @Override
                public void run () {
                    FirebaseUser user = firebaseAuth.getCurrentUser ();
                    if (user == null) {
                        startActivity (new Intent (SplashScreenActivity.this, LoginActivity.class));
                        finish ();
                    } else {
                        //User is logger in , check user type
                        checkUserType ();
                    }
                }
            }, 2000);
            Animation animUpDown,animside;

            // load the animation
            animside = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.side);

            // start the animation
            img.startAnimation(animside);



            // load the animation
            animUpDown = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.up_down);

            // start the animation
            imgabove.startAnimation(animUpDown);

        }catch (Exception e){
            Toasty.error (this,e.getMessage (),Toasty.LENGTH_LONG).show ();
        }

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            startService(intent);
        }
    }


    private void checkUserType () {
        /**
         * If user is seller, start seller main screen
         * If user is buyer, start user main screen
         */

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ())
                .addListenerForSingleValueEvent (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        String accountType = "" + dataSnapshot.child ("accountType").getValue ();
                        String status = "" + dataSnapshot.child ("status").getValue ();
                        if (status.equals ("disactive")) {
                            //User is Seller
                            startActivity (new Intent (SplashScreenActivity.this, LoginActivity.class));
                            firebaseAuth.signOut ();
                            finish ();
                        } else {
                            if (accountType.equals ("seller")) {

                                //User is Seller
                                startActivity (new Intent (SplashScreenActivity.this, MainSellerActivity.class));
                                finish ();
                            } else if (accountType.equals ("admin")) {

                                //User is Seller
                                startActivity (new Intent (SplashScreenActivity.this, MainAdminActivity.class));
                                finish ();
                            } else {

                                //User is Seller
                                startActivity (new Intent (SplashScreenActivity.this, MainUserActivity.class));
                                finish ();
                            }
                        }
                    }
                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }



}
