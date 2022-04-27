package com.easyshopping;

import Adapters.AdapterOrder;
import Adapters.AdapterProductSeller;
import Models.ModelOrder;
import Models.ModelProducts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
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

public class MainSellerActivity extends AppCompatActivity  implements
        NavigationView.OnNavigationItemSelectedListener{
    private TextView nameTv, shopNameTV, EmailTv, tabProductTv, tabOrdersTv,filteredProductTv,filteredOrdersTv;
    private EditText searchProductEt;
    private ImageButton logoutBtn, backBtn, editProfileBtn, addProductBtn, filterProductBtn,filterOrderBtn,reviewBtn;
    private ImageView profileIV, imageView;
    private RelativeLayout productsRl, ordersRl;
    private RecyclerView productsRv,orderRV;
    private FloatingActionButton sellerfab;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private DrawerLayout drawer;

    private ArrayList<ModelProducts> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrder> orderArrayList;
    private AdapterOrder adapterOrder;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main_seller);
        init ();

        DrawerLayout drawer = (DrawerLayout)
                findViewById (R.id.drawer_layout);

        Toolbar toolbar = findViewById (R.id.toolbar);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle (MainSellerActivity.this, drawer, toolbar,
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

        firebaseAuth = FirebaseAuth.getInstance ();

        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);

        checkUser ();
        showProductsUI ();
        loadAllProducts();
        loadAllOrders();

        //Handle products and orders tabs
        tabProductTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Load products
                showProductsUI();

            }
        });
        tabOrdersTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //load orders
                showOrdersUI();
            }
        });
        //----------- End handle tabs ---------------\\

        tabProductTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Load products
               showProductsUI();

            }
        });
        tabOrdersTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //load orders
                 showOrdersUI();
            }
        });
        filterProductBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder (MainSellerActivity.this);
                builder.setTitle ("Choose Category : ")
                        .setItems (Constants.ProductCategories1, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                //Get selected item
                                final   String selected = Constants.ProductCategories1[which];
                                filteredProductTv.setText (selected);
                                if (selected.equals ("All")){
                                    //load all data
                                    loadAllProducts ();
                                }
                                else {
                                    //load filtered data
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show ();
            }
        });
        searchProductEt.addTextChangedListener (new TextWatcher () {
            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged (CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter ().filter (s);
                }catch (Exception e){
                    e.printStackTrace ();
                }
            }

            @Override
            public void afterTextChanged (Editable s) {

            }
        });

        filterOrderBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //options to display in dialog
                final String[] options = {"In Progress","Completed","Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder (MainSellerActivity.this);
                builder.setTitle ("Filter Orders : ")
                        .setItems (options, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                //handle item clicks
                                if (which==0) {
                                    //All clicked
                                    filteredOrdersTv.setText ("Showing All Orders");
                                    adapterOrder.getFilter ().filter ("");//show all orders
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText ("Showing " + optionClicked + " Orders");//e.g showing Completed orders
                                    adapterOrder.getFilter ().filter (optionClicked);
                                }
                            }

                        }).show ();
            }
        });


        sellerfab.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (MainSellerActivity.this, DashboardActivity.class);
                startActivity (intent);
            }
        });
    }


    private void showOrdersUI () {
        //show orders ui and hide products ui
        productsRl.setVisibility (View.GONE);
        ordersRl.setVisibility (View.VISIBLE);
        tabProductTv.setTextColor(getResources ().getColor (R.color.colorWhite));
        tabProductTv.setBackgroundColor (getResources ().getColor (android.R.color.transparent));

        tabOrdersTv.setTextColor (getResources ().getColor (R.color.colorblack));
        tabOrdersTv.setBackgroundResource (R.drawable.shape_rect04);

    }

    private void showProductsUI () {
        //show products ui and hid orders ui
        productsRl.setVisibility (View.VISIBLE);
        ordersRl.setVisibility (View.GONE);

        tabProductTv.setTextColor(getResources ().getColor (R.color.colorblack));
        tabProductTv.setBackgroundResource (R.drawable.shape_rect04);

        tabOrdersTv.setTextColor (getResources ().getColor (R.color.colorWhite));
        tabOrdersTv.setBackgroundColor (getResources ().getColor (android.R.color.transparent));
    }


    //------------------------- Load Orders---------------- \\\
    private void loadAllOrders () {
        orderArrayList = new ArrayList<> ();

        //load order of shop
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child ("Orders")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        //clear list befoe adding new data in it
                        orderArrayList.clear ();
                        for (DataSnapshot ds : dataSnapshot.getChildren ()){
                            ModelOrder modelOrder = ds.getValue (ModelOrder.class);
                            //add to list
                            orderArrayList.add (modelOrder);

                        }
                        //set adapter
                        adapterOrder = new AdapterOrder (MainSellerActivity.this,orderArrayList);
                        //set adapter to recyclerview
                        orderRV.setAdapter (adapterOrder);
                        //now create a filter class, we already have one, lets copy patst and change according to our

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

//------------------------- Load Products ----------------- \\\

    private void loadAllProducts () {
        productList = new ArrayList<> ();
        //getAll products
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child ("Products")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        productList.clear ();
                        for(DataSnapshot ds : dataSnapshot.getChildren ()){
                            ModelProducts modelProducts = ds.getValue (ModelProducts.class);
                            productList.add (modelProducts);
                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller (MainSellerActivity.this, productList);
                        //set Adapter
                        productsRv.setAdapter (adapterProductSeller);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }
    private void loadFilteredProducts (final String selected) {
        productList = new ArrayList<> ();
        //getAll products
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child ("Products")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //before getting reset list
                        productList.clear ();
                        for (DataSnapshot ds : dataSnapshot.getChildren ()) {
                            String productCategory = "" + ds.child ("productCategory").getValue ();
                            //if selected category matches product category then add in list
                            if (selected.equals (productCategory)) {
                                ModelProducts modelProducts = ds.getValue (ModelProducts.class);
                                productList.add (modelProducts);
                            }
                            //setup adapter
                            adapterProductSeller = new AdapterProductSeller (MainSellerActivity.this, productList);
                            //set Adapter
                            productsRv.setAdapter (adapterProductSeller);
                        }
                    }
                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }



    private void makeMeOffline () {
        //Make user online after logging in
        progressDialog.setMessage ("Logging out...");
        HashMap<String, Object> hashMap = new HashMap<> ();
        hashMap.put ("online", "false");
        //update value to database
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child("Seller").updateChildren (hashMap)
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
            startActivity(new Intent (MainSellerActivity.this, LoginActivity.class));
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

                        for (DataSnapshot ds : dataSnapshot.getChildren ()) {
                            String fname = String.valueOf (ds.child ("first_name").getValue ());
                            String lname = String.valueOf (ds.child ("last_name").getValue ());
                            String fullName = fname + " " + lname;
                            String email = String.valueOf (ds.child ("email").getValue ());
                            String profileImage = String.valueOf (ds.child ("profileImage").getValue ());
                            String accountType = String.valueOf (ds.child ("accountType").getValue ());

                            nameTv.setText (fullName);
                            EmailTv.setText (email);
                            if (!profileImage.equals ("")) {
                                try {
                                    Picasso.get ().load (profileImage).placeholder (R.drawable.ic_store_mall).into (profileIV);

                                } catch (Exception e) {
                                   profileIV.setImageResource (R.drawable.ic_person_gray);

                                }
                            }
                            else{
                               profileIV.setImageResource (R.drawable.ic_store_mall);
                            }
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
        DatabaseReference sellerRef = FirebaseDatabase.getInstance ().getReference ("Persons");
        sellerRef.child (firebaseAuth.getUid ()).child ("Seller")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                            String shopname = "" + dataSnapshot.child ("shop_name").getValue ();
                            shopNameTV.setText (shopname);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }
    public void init(){
        nameTv = findViewById (R.id.sellerNameTV);
        shopNameTV = findViewById (R.id.shopNameTV);
        EmailTv = findViewById (R.id.sellerEmailTV);
        profileIV = findViewById (R.id.sellerProfileIV);
        tabOrdersTv = findViewById (R.id.tabOrdersTv);
        tabProductTv = findViewById (R.id.tabProductTv);
        productsRl = findViewById (R.id.productsRl);
        ordersRl = findViewById (R.id.OrderssRl);
        searchProductEt = findViewById (R.id.rowSearchProductEt);
        filterProductBtn = findViewById (R.id.filterProductionBtn);
        filteredProductTv= findViewById (R.id.filteredProductsTv);
        productsRv = findViewById (R.id.productsRV);
        filteredOrdersTv=findViewById (R.id.filteredOrdersTv);
        filterOrderBtn = findViewById (R.id.filterOrderBtn);
        orderRV=findViewById (R.id.orderRV);
        //reviewBtn=findViewById (R.id.reviewBtn);
        sellerfab=findViewById (R.id.sellerfab);
    }
    public void showToastSuccess(String s){
        Toasty.success (MainSellerActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (MainSellerActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (MainSellerActivity.this,s,Toasty.LENGTH_LONG).show ();
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById (R.id.drawer_layout);

        // Handle navigation view item clicks here.
        switch (item.getItemId ()) {
            case R.id.addProductBtnnav:
                drawer.closeDrawer (GravityCompat.START);
                //Open add product activity
                startActivity (new Intent (MainSellerActivity.this,AddProductActivity.class));

                return true;
            case R.id.editProfileBtnnav:
                drawer.closeDrawer (GravityCompat.START);
                //Open Edit profile activity
                startActivity (new Intent (MainSellerActivity.this, ProfileEditSellerActivity.class));
                return true;


            case R.id.reviewBtnnav:
                // Handle the tools action (for now display a toast).
                drawer.closeDrawer (GravityCompat.START);
                //use the same activity that we used in user view review
                Intent intent= new Intent (MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra ("shopUid", firebaseAuth.getUid ());
                startActivity (intent);
                return true;

            case R.id.contactBtnnav:
                drawer.closeDrawer (GravityCompat.START);
                Intent i= new Intent (MainSellerActivity.this, ContactActivity.class);
                i.putExtra ("shopUid", firebaseAuth.getUid ());
                startActivity (i);
                return true;

            case R.id.createGroupbtnNav:
                drawer.closeDrawer (GravityCompat.START);
                Intent intent1= new Intent (MainSellerActivity.this, GroupCreateActivity.class);
                intent1.putExtra ("shopUid", firebaseAuth.getUid ());
                startActivity (intent1);
                return true;


            case R.id.logoutBtnnav:
                // Handle logout button
                makeMeOffline ();
                drawer.closeDrawer (GravityCompat.START);
                return true;
            default:
                return false;

        }
    }
}
