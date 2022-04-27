package com.easyshopping;

import Adapters.AdapterOrderUser;
import Adapters.AdapterProductSeller;
import Adapters.AdapterShops;
import Adapters.AdapterSuggestions;
import Models.ModelAddress;
import Models.ModelContact;
import Models.ModelCustomer;
import Models.ModelOrderUser;
import Models.ModelProducts;
import Models.ModelSeller;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import es.dmoral.toasty.Toasty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity  implements
        NavigationView.OnNavigationItemSelectedListener{

    private TextView userNameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv, nameTvnav,phoneTvnav, emailTvnav;
    private ImageButton ulogoutBtn,backBtn, editProfileBtn;
    private RelativeLayout shopsRl, ordersRl;
    private ImageView profileIv,userProfileIvnav;
    private RecyclerView shopsRv,ordersRV,suggestionRv;
    private FloatingActionButton chatFBtn;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private ArrayList<ModelShops> shopsList;
    private AdapterShops adapterShops;
    public ArrayList<ModelSeller> sellerArrayList;
    public ArrayList<ModelAddress> addressArrayList;
    public ArrayList<ModelContact> contactArrayList;
    public ArrayList<ModelCustomer> customerArrayList;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    private AdapterSuggestions adapterSuggestions;
    private ArrayList<ModelProducts> productsArrayList;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main_user);
        init();

        shopsList = new ArrayList<> ();
        ordersList = new ArrayList<> ();

        DrawerLayout drawer = (DrawerLayout)
                findViewById (R.id.drawer_layout);

        Toolbar toolbar = findViewById (R.id.toolbar);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle (MainUserActivity.this, drawer, toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener (toggle);
        }
        toggle.syncState ();

        NavigationView navigationView = (NavigationView)
                findViewById (R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener (this);
        }

        View navHeaderView = navigationView.getHeaderView(0);
        phoneTvnav = navHeaderView.findViewById  (R.id.userPhoneTv);
        userProfileIvnav = navHeaderView.findViewById  (R.id.UserProfileIV);
        nameTvnav = navHeaderView.findViewById (R.id.UserNameTV);
        emailTvnav = navHeaderView.findViewById (R.id.userEmailTV);
        //Check user



        firebaseAuth = FirebaseAuth.getInstance ();
        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);
        //init list
        shopsList = new ArrayList<> ();
        sellerArrayList = new ArrayList<> ();
        addressArrayList = new ArrayList<> ();
        contactArrayList = new ArrayList<> ();
        customerArrayList = new ArrayList<> ();
        productsArrayList = new ArrayList<> ();
        loadSuggestions();
        checkUser ();
        showShopsUI ();



        tabShopsTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Load products
                showShopsUI();

            }
        });
        tabOrdersTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //load orders
                showOrdersUI();
            }
        });
        chatFBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Handle floating button
                Intent intent = new Intent (MainUserActivity.this, DashboardActivity.class);
                startActivity (intent);
            }
        });
    }


    private void showOrdersUI () {
        //show orders ui and hide products ui
        shopsRl.setVisibility (View.GONE);
        ordersRl.setVisibility (View.VISIBLE);
        tabShopsTv.setTextColor(getResources ().getColor (R.color.colorWhite));
        tabShopsTv.setBackgroundColor (getResources ().getColor (android.R.color.transparent));

        tabOrdersTv.setTextColor (getResources ().getColor (R.color.colorblack));
        tabOrdersTv.setBackgroundResource (R.drawable.shape_rect04);

    }
    private void showShopsUI () {
        //show products ui and hid orders ui
        shopsRl.setVisibility (View.VISIBLE);
        ordersRl.setVisibility (View.GONE);

        tabShopsTv.setTextColor(getResources ().getColor (R.color.colorblack));
        tabShopsTv.setBackgroundResource (R.drawable.shape_rect04);

        tabOrdersTv.setTextColor (getResources ().getColor (R.color.colorWhite));
        tabOrdersTv.setBackgroundColor (getResources ().getColor (android.R.color.transparent));
    }


    private void makeMeOffline () {
        //Make user online after logging in
        progressDialog.setMessage ("Logging out...");
        HashMap<String, Object> hashMap = new HashMap<> ();
        hashMap.put ("online", "false");
        //update value to database
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child ("Customer").updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //Updating is successfully
                        firebaseAuth.signOut ();
                        checkUser();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        //Failed updateing
                        progressDialog.dismiss ();
                        showToastError ("Failed updating : " + e.getMessage ());
                    }
                });
    }

    private void checkUser () {
        FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user == null){
            startActivity(new Intent (MainUserActivity.this, LoginActivity.class));
            finish ();
        }
        else {
            loadMyInfo();
        }

    }

      private void loadMyInfo () {
        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
        reference.orderByChild ("person_Id").equalTo (firebaseAuth.getUid ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds:dataSnapshot.getChildren ()){
                            String firstname =String.valueOf (ds.child ("first_name").getValue ());
                            String lastname = String.valueOf (ds.child ("last_name").getValue ());
                            String email =String.valueOf (ds.child ("email").getValue ());
                            String phone =String.valueOf (ds.child ("phone").getValue ());
                            String profileImage =String.valueOf (ds.child ("profileImage").getValue ());
                            String accountType =String.valueOf (ds.child ("accountType").getValue ());
                            String city =String.valueOf (ds.child ("city").getValue ());
                            String name = firstname + " " + lastname;

                            nameTvnav.setVisibility (View.VISIBLE);
                            phoneTvnav.setVisibility (View.VISIBLE);
                            emailTvnav.setVisibility (View.VISIBLE);
                            userProfileIvnav.setVisibility (View.VISIBLE);

                           // userNameTv.setText(name);
                           // emailTv.setText(email);
                           // phoneTv.setText(phone);
                            nameTvnav.setText (name);
                            phoneTvnav.setText (phone);
                            emailTvnav.setText (email);
                            try {
                                Picasso.get ().load (profileImage).placeholder (R.drawable.ic_store_mall)
                                        .into (profileIv);
                                Picasso.get ().load (profileImage).placeholder (R.drawable.ic_store_mall)
                                        .into (userProfileIvnav);
                            }catch (Exception e){
//                                Picasso.get ().load (R.drawable.ic_store_mall).into (profileIv);
                            }
                            //load only those shops that are in the city of user
                            loadCityShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadCityShops (final String myCity) {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.orderByChild ("accountType").equalTo ("seller")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding
                        shopsList.clear ();
                        for (DataSnapshot ds:dataSnapshot.getChildren ()){
                            ModelShops modelShops = ds.getValue (ModelShops.class);
                            String shopCity = String.valueOf (ds.child ("city").getValue ());
                            //show only user city shops
                            shopsList.add (modelShops);
                      /*  if (shopCity.equals (myCity)){
                            shopsList.add (modelShops);
                        }*/

                            //if you want to display all shops, skip the if statement
                            //shopsList.add (modelShops);
                        }
                        //setup adapter
                        adapterShops= new AdapterShops (MainUserActivity.this, shopsList);
                        //set adapter to recyclerView
                        shopsRv.setAdapter (adapterShops);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }
    private void loadOrders () {
        //init order list
        ordersList = new ArrayList<> ();
        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                ordersList.clear ();
                for (DataSnapshot ds : dataSnapshot.getChildren ()){
                    String uid = String.valueOf (ds.getRef ().getKey ());
                    DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons").child (uid).child ("Orders");
                    ref.orderByChild ("customer_Id").equalTo (firebaseAuth.getUid ())
                            .addValueEventListener (new ValueEventListener () {
                                @Override
                                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists ()){
                                        for (DataSnapshot ds : dataSnapshot.getChildren ()) {

                                            ModelOrderUser modelOrderUser = ds.getValue (ModelOrderUser.class);
                                            //add to list
                                            ordersList.add (modelOrderUser);
                                        }
                                    }
                                    //setup Adapter
                                    adapterOrderUser = new AdapterOrderUser (MainUserActivity.this, ordersList);
                                    //set Adapter to recyclerView
                                    ordersRV.setAdapter (adapterOrderUser);


                                }

                                @Override
                                public void onCancelled (@NonNull DatabaseError databaseError) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });
    }




    public void init(){
        userNameTv = findViewById (R.id.UserNameTV);
        emailTv = findViewById (R.id.userEmailTV);
        tabShopsTv = findViewById (R.id.tabShopTv);
        tabOrdersTv = findViewById (R.id.tabOrdersTv);
        shopsRl = findViewById (R.id.shopRl);
       ordersRl = findViewById (R.id.OrdersRl);
       phoneTv = findViewById (R.id.userPhoneTv);
       profileIv = findViewById (R.id.UserProfileIV);
       shopsRv = findViewById (R.id.shopsRv);
       ordersRV= findViewById (R.id.ordersRV);
        chatFBtn = findViewById (R.id.fab);
        suggestionRv= findViewById (R.id.suggestionRv);

    }

    public void showToastSuccess(String s){
        Toasty.success (MainUserActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (MainUserActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (MainUserActivity.this,s,Toasty.LENGTH_LONG).show ();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById (R.id.drawer_layout);
        // Handle navigation view item clicks here.
        switch (item.getItemId ()) {

            case R.id.editProfileBtnUnav:
                drawer.closeDrawer (GravityCompat.START);
                //Open Edit profile activity
                startActivity (new Intent (MainUserActivity.this, ProfileEditUserActivity.class));
                return true;

            case R.id.contactBtnnav:
                drawer.closeDrawer (GravityCompat.START);
                Intent i= new Intent (MainUserActivity.this, ContactActivity.class);
                i.putExtra ("shopUid", firebaseAuth.getUid ());
                startActivity (i);
                return true;


            case R.id.logoutBtnUnav:
                // Handle logout button
                drawer.closeDrawer (GravityCompat.START);
                makeMeOffline ();

                return true;
            default:
                return false;

        }
    }




    private void loadSuggestions () {

        //getAll products
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        productsArrayList.clear ();
                        for(DataSnapshot ds : dataSnapshot.getChildren ()){
                           String uid = ds.getRef ().getKey ()+"";
                            DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons").child (uid).child ("Products");
                            reference.addValueEventListener (new ValueEventListener () {
                                @Override
                                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren ()){
                                        String discount = dataSnapshot1.child ("discountAvailable").getValue ()+"";

                                        ModelProducts modelProducts = dataSnapshot1.getValue (ModelProducts.class);

                                        if (discount.equals ("true")) {
                                            productsArrayList.add (modelProducts);
                                        }

                                    }
                                    //setup adapter
                                    adapterSuggestions = new AdapterSuggestions (MainUserActivity.this, productsArrayList);
                                    //set Adapter
                                    suggestionRv.setAdapter (adapterSuggestions);

                                }

                                @Override
                                public void onCancelled (@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

}
