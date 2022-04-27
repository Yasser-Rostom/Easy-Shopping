package com.easyshopping;

import Adapters.AdapterReview;
import Models.ModelReview;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.ColorSpace;
import android.media.Rating;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    //ShopUid pass from shopDetailsActivity
    private String shopUid;

    //UI views
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv, ratingTv;
    private RatingBar  ratingBar;
    RecyclerView reviewRv;


    //Firebase
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReview> reviewArrayList; //will contain list of all reviews
    private AdapterReview adapterReview;

    String city,shopAddress,shopLatitude,myLongitudem,shopLongitude,myLatitude,myLongitude;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_shop_reviews);
        init ();

        //get shop uid from intent
        shopUid = getIntent ().getStringExtra ("shopUid");
        //init firebaseauth
        firebaseAuth = FirebaseAuth.getInstance ();
        loadShopDetails();
        loadReviews();

        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
    }

    private float ratingSum = 0;

    private void loadReviews () {
        //init list
        reviewArrayList = new ArrayList<> ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid).child ("Ratings")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding data into it
                        reviewArrayList.clear ();
                        ratingSum=0;
                        for (DataSnapshot ds: dataSnapshot.getChildren ()){
                            float rating = Float.parseFloat (String.valueOf (ds.child ("ratings").getValue ()));
                            ratingSum = ratingSum +rating;//for avg rating, add (addition all ratings, later will divide it by number of reviews

                            ModelReview modelReview = ds.getValue (ModelReview.class);
                            reviewArrayList.add (modelReview);

                        }
                        //setup adapter
                        adapterReview = new AdapterReview (ShopReviewsActivity.this, reviewArrayList);
                        //set to recyclerView
                        reviewRv.setAdapter (adapterReview);

                        long numberOfReviews = dataSnapshot.getChildrenCount ();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingTv.setText (String.format ("%.2f", avgRating)+ "[" + numberOfReviews + "]");//e.g 4.7
                        ratingBar.setRating (avgRating);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadShopDetails () {
        loadAddress ();
        loadSeller ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        String profileImage = String.valueOf (dataSnapshot.child ("profileImage").getValue ());

                        try {
                            Picasso.get ().load (profileImage).placeholder (R.drawable.ic_person_gray).into (profileIv);
                        }catch (Exception e){
                            profileIv.setImageResource (R.drawable.ic_store_gray);
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
    private void loadAddress () {

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid).child ("Address")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        String address = null;
                        city = String.valueOf (dataSnapshot.child ("city").getValue ());
                        shopAddress = String.valueOf (dataSnapshot.child ("address").getValue ());
                        shopLatitude = String.valueOf (dataSnapshot.child ("latitude").getValue ());
                        shopLongitude = String.valueOf (dataSnapshot.child ("longitude").getValue ());
                        myLongitude = shopLongitude;
                        myLatitude=shopLatitude;

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void init(){
        backBtn = findViewById (R.id.BackBtnReview);
        profileIv = findViewById (R.id.profileIV);
        shopNameTv = findViewById (R.id.shopNameTvR);
        ratingTv= findViewById (R.id.ratingTv);
        ratingBar = findViewById (R.id.ratingBar);
        reviewRv = findViewById (R.id.reviewRV);

    }
}
