package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    //UI Views
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;
    private FirebaseAuth firebaseAuth;

    private String shopUid;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_write_review);
        init ();
        firebaseAuth = FirebaseAuth.getInstance ();

        //get shop uid from intent
        final Intent intent = getIntent ();
        shopUid = intent.getStringExtra ("orderTo");
        //load shop info: shop name , shop image
        loadShopInfo();

        //if user has written review to this shop , load it
        loadMyReview();

        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });


        //input data
        submitBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                inputData();
            }
        });

    }

    private void loadShopInfo () {
       // loadSeller();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {


                        final String sImage = String.valueOf (dataSnapshot.child ("profileImage").getValue ());
                        String shopName = String.valueOf (dataSnapshot.child ("Seller").child ("shop_name").getValue ());
                        shopNameTv.setText (shopName);


                        try {
                            Picasso.get ().load (sImage).into (profileIv);
                        }catch (Exception e){
                            Picasso.get ().load (R.drawable.ic_store_mall).into (profileIv);

                        }


                    }


                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void loadSeller () {


        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid).child ("Seller")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        String shopName = String.valueOf (dataSnapshot.child ("shop_name").getValue ());
                        shopNameTv.setText (shopName);

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadMyReview () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid).child ("Ratings").child (firebaseAuth.getUid ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists ()){
                            // my review is available in this shop
                            //get review details
                            String uid = String.valueOf (dataSnapshot.child ("uid").getValue ());
                            String ratings = String.valueOf (dataSnapshot.child ("ratings").getValue ());
                            String review = String.valueOf (dataSnapshot.child ("review").getValue ());
                            String timestamp = String.valueOf (dataSnapshot.child ("timestamp").getValue ());

                            //set review details to our ui
                            float myRating = Float.parseFloat (ratings);
                            ratingBar.setRating (myRating);
                            reviewEt.setText (review);

                        }

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void inputData () {

        String ratings = String.valueOf (ratingBar.getRating ());
        String review = reviewEt.getText ().toString ().trim ();
        // for time of review
        String timeStamp = String.valueOf (System.currentTimeMillis ());
        if (review.equals ("")){
            showToastWarning ("Please leave your review....!!!!");
        }
        //Setup data in hashmap
        HashMap<String, Object> hashMap = new HashMap<> ();
        hashMap.put ("uid", String.valueOf (firebaseAuth.getUid ()));
        hashMap.put ("ratings", String.valueOf (ratings));
        hashMap.put ("review", String.valueOf (review));
        hashMap.put ("timestamp", String.valueOf (timeStamp));

        //put to db:DB > Users > ShopUid > Ratings
        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
        reference.child (shopUid).child ("Ratings").child (firebaseAuth.getUid ()).updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        showToastSuccess ("Review published successfully.......");
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        showToastError ("Publish review :" + e.getMessage ());
                    }
                });

    }

    public void init(){
        backBtn = findViewById (R.id.backBtn);
        profileIv = findViewById (R.id.profileIv);
        shopNameTv = findViewById (R.id.shopNameTv);
        ratingBar = findViewById (R.id.ratingBar);
        reviewEt = findViewById (R.id.reviewEt);
        submitBtn = findViewById (R.id.submitBtn);

    }

    public void showToastSuccess(String s){
        Toasty.success (WriteReviewActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (WriteReviewActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (WriteReviewActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
}
