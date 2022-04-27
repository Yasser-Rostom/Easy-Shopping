package com.easyshopping;

import Adapters.AdapterCartItem;
import Adapters.AdapterProductUser;
import Adapters.AdapterReview;
import Adapters.AdapterShops;
import Models.ModelCartItem;
import Models.ModelProducts;
import Models.ModelReview;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.icu.lang.UProperty;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class ShopDetailsActivity extends AppCompatActivity {
    //declare ui Views
    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filteredProductstv , cartCountTv;
    private ImageButton callBtn, mapBtn, cartIB,backBtn, filterProductBtn,mailbtn, reviewBtn,contactIB;
    private EditText searchProductET;
    private RecyclerView productRv;
    private RatingBar ratingBar;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    //String variable for recieve the data from AdapterShops
    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopLatitude, shopLongitude;
    public String shopName,profileImage,shopEmail,shopPhone,accountType,deliveryFee,city,shopAddress,shopOpen;

    //Declare EasyDB
    private EasyDB easyDB;


    private ArrayList<ModelProducts> productList;
    private AdapterProductUser adapterProductUser;

    //Cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    //progress dialog


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_shop_details);
        //initialization View
        init ();
        //get Uid of the shop from AdapterShop
        shopUid = getIntent ().getStringExtra ("shopUid");

        firebaseAuth = FirebaseAuth.getInstance ();
        //init progress dialog
        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait....");
        progressDialog.setCanceledOnTouchOutside (false);

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();//avg rating ,set on ratingbar



        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName ("ITEMS_TABLE")
                .addColumn (new Column ("Item_Id", new String []{"text","unique"}))
                .addColumn (new Column ("Item_PID", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Name", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Price_Each", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Price", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Quantity", new String []{"text","not null"}))
                .doneTableColumn ();
        //each shop have its own products and orders so if user add items to cart and go back and open cart in different shop then should open different cart
        //sho delete cart data whenever user open this activity
        deleteCartData();
        cartCount ();//After create this method we must update cart count in adapterProductUser.java


        searchProductET.addTextChangedListener (new TextWatcher () {
            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged (CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter ().filter (s);
                }catch (Exception e){
                    e.printStackTrace ();
                }
            }

            @Override
            public void afterTextChanged (Editable s) {

            }
        });

        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
        cartIB.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Handle cart
                showCartDialog();
            }
        });
        callBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                dialPhone();
            }
        });
        mailbtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {

            }
        });
        mapBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder (ShopDetailsActivity.this);
                builder.setTitle ("Choose Category : ")
                        .setItems (Constants.ProductCategories1, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                //Get selected item
                                final   String selected = Constants.ProductCategories1[which];
                                filteredProductstv.setText (selected);
                                if (selected.equals ("All")){
                                    //load all data
                                    loadShopProducts ();
                                }
                                else {
                                    //load filtered data
                                    adapterProductUser.getFilter().filter (selected);
                                }
                            }
                        })
                        .show ();
            }
        });
        //Handle review click, open reviews activity
        reviewBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //pass shopUid
                Intent intent = new Intent (ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra ("shopUid", shopUid);
                startActivity (intent);
            }
        });

        contactIB.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (ShopDetailsActivity.this, ShopContactInfoActivity.class);
                intent.putExtra ("shopUid", shopUid);
                startActivity (intent);
            }
        });
    }

    private float ratingSum = 0;

    private void loadReviews () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid).child ("Ratings")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        ratingSum =0;
                        for (DataSnapshot ds: dataSnapshot.getChildren ()){
                            float rating = Float.parseFloat (String.valueOf (ds.child ("ratings").getValue ()));
                            ratingSum = ratingSum +rating;//for avg rating, add (addition all ratings, later will divide it by number of reviews

                        }
                        long numberOfReviews = dataSnapshot.getChildrenCount ();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingBar.setRating (avgRating);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void deleteCartData () {

        easyDB.deleteAllDataFromTable ();

    }

    public void cartCount(){
        //keep it publice so we can access in adapter
        //get cart count
        int count = easyDB.getAllData ().getCount ();
        if (count == 0){
            //no item in cart , hide cart count textview
            cartCountTv.setVisibility (View.GONE);
        }
        else {
            //have item in cart , show cart count textview and set count
            cartCountTv.setVisibility (View.VISIBLE);
            cartCountTv.setText (String.valueOf (count)); //Concatenate with string,, because we can't set integer in TextView
        }
    }

    public double allTotalPrice = 0.00;
    //need to access these views in adapter so making public
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;

    private void showCartDialog () {
        cartItemList = new ArrayList<> ();
        //inflate cart layout
        View view = LayoutInflater.from (this).inflate (R.layout.cart_dailog, null);
        TextView shopNameTv = view.findViewById (R.id.cartShopNameTv);
        RecyclerView cartItemRv = view.findViewById (R.id.cartItemsRv);
        sTotalTv =  view.findViewById (R.id.sTotalTv);
        dFeeTv =  view.findViewById (R.id.dFeeTv);
        allTotalPriceTv =  view.findViewById (R.id.totalTv);
        Button checkoutBtn = view.findViewById (R.id.checkoutBtn);

        //Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        //set View to dialog
        builder.setView (view);
        shopNameTv.setText (shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName ("ITEMS_TABLE")
                .addColumn (new Column ("Item_Id", new String []{"text","unique"}))
                .addColumn (new Column ("Item_PID", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Name", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Price_Each", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Price", new String []{"text","not null"}))
                .addColumn (new Column ("Item_Quantity", new String []{"text","not null"}))
                .doneTableColumn ();

        //Get All records from db
        Cursor res = easyDB.getAllData ();
        while (res.moveToNext ()) {
            String id = res.getString (1);
            String pId = res.getString (2);
            String name = res.getString (3);
            String price = res.getString (4);
            String cost = res.getString (5);
            String quantity = res.getString (6);

            allTotalPrice = allTotalPrice + Double.parseDouble (cost);
            ModelCartItem modelCartItem = new ModelCartItem (
                    "" + id,
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity
            );
            cartItemList.add (modelCartItem);
        }
        //setup Adapter
        adapterCartItem = new AdapterCartItem (this,cartItemList);
        cartItemRv.setAdapter (adapterCartItem);

        dFeeTv.setText (deliveryFee+"SYP");
        sTotalTv.setText (String.format ("%.2f", allTotalPrice)+"SYP");
        allTotalPriceTv.setText ((allTotalPrice + Double.parseDouble (deliveryFee.replace ("SYP","")))+"SYP");

        //Show Dialog
        final   AlertDialog dialog = builder.create ();
        dialog.show ();

        //reset total price on dialog dimiss
        dialog.setOnCancelListener (new DialogInterface.OnCancelListener () {
            @Override
            public void onCancel (DialogInterface dialog) {
                allTotalPrice = 0.00;

            }
        });

        //Place Order
        checkoutBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //first validate delivery address
                if (myLatitude.equals ("") || myLatitude.equals ("null") || myLongitude.equals ("") || myLongitude.equals ("null")) {
                    showToastWarning ("Please enter your address in your profile before place your order....!!!");
                    return;//don't proceed further
                }
                if (myPhone.equals ("") || myPhone.equals ("null")){
                    showToastWarning ("Please Enter your Phone in your profile before place your order....!!!");
                    return;//don't proceed further
                }
                if (cartItemList.size () == 0){
                    //cart list is empty
                    showToastWarning ("No item in cart");
                }
                submitOrder();
                dialog.dismiss ();

            }
        });
    }

    private void submitOrder () {
        //show progress dialog
        progressDialog.setMessage ("Placing order.....");
        progressDialog.show ();

        //for oder id and order time
        final String timestamp = "" + System.currentTimeMillis ();
        String cost = allTotalPriceTv.getText ().toString ().trim ().replace ("SYP", "");

        //setup order data
        final HashMap<String, String> hashMap = new HashMap<> ();
        hashMap.put("orderId", String.valueOf (timestamp));
        hashMap.put("date", String.valueOf (timestamp));
        hashMap.put("delivery_state", String.valueOf ("In Progress"));
        hashMap.put("total_amount", String.valueOf (cost));
        hashMap.put("customer_Id", String.valueOf (firebaseAuth.getUid ()));
        hashMap.put("orderTo", String.valueOf (shopUid));
        hashMap.put("latitude", String.valueOf (myLatitude));
        hashMap.put("longtitude", String.valueOf (myLongitude));
        hashMap.put("deliveryFee", String.valueOf (deliveryFee));


        //add to db
        final DatabaseReference ref1 = FirebaseDatabase.getInstance ().getReference ("Persons").child(shopUid).child ("Orders");
        ref1.child (timestamp).setValue (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //Order info added now add order items
                        for (int i=0 ; i <cartItemList.size (); i++) {
                            final String pId = cartItemList.get (i).getpId ();
                            String id = cartItemList.get (i).getId ();
                            String cost = cartItemList.get (i).getCost ();
                            String name = cartItemList.get (i).getName ();
                            String price = cartItemList.get (i).getPrice ();
                            String quantity = cartItemList.get (i).getQuantity ();

                            HashMap<String, String> hashMap1 = new HashMap<> ();
                            hashMap1.put ("bill_Id", pId);
                            hashMap1.put ("item_desc", name);
                            hashMap1.put ("single_price", cost);
                            hashMap1.put ("tot_price", price);
                            hashMap1.put ("quantity", quantity);

                            ref1.child (timestamp).child ("Bill_Items").child (pId).setValue (hashMap1);
                        }
                        progressDialog.dismiss ();
                        showToastSuccess ("Order Placed Successfully...." );

                        //After placing order open order details page
                        //open order details, we need to keys there orderId, orderTo
                        Intent intent = new Intent (ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                        intent.putExtra ("orderTo", shopUid);
                        intent.putExtra ("orderId", timestamp);
                        startActivity (intent);
                    }

                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        //Faild to place holder
                        progressDialog.dismiss ();
                        showToastError ("Order is not placed successfully...." + e.getMessage ());
                    }//now lets show the placed orders, row_order_user.xml for recyclerview
                });

    }

    private void openMap () {
        //Saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr="+myLatitude + ","+myLongitude+"&daddr=" + shopLatitude +","+shopLongitude;
        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse (address));
        startActivity (intent);
    }

    private void dialPhone () {
        startActivity (new Intent (Intent.ACTION_DIAL, Uri.parse ("tel:"+Uri.encode(shopPhone))));
        showToastSuccess (shopPhone);
    }

    private void loadMyInfo () {
        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
        reference.orderByChild ("person_Id").equalTo (firebaseAuth.getUid ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds:dataSnapshot.getChildren ()){
                            String name =String.valueOf (ds.child ("first_name").getValue ());
                            String email =String.valueOf (ds.child ("email").getValue ());
                            myPhone =String.valueOf (ds.child ("phone").getValue ());
                            String profileImage =String.valueOf (ds.child ("profileImage").getValue ());
                            String accountType =String.valueOf (ds.child ("accountType").getValue ());

                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }




    private void loadShopDetails () {

        loadSeller ();
        loadAddress ();
        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
        reference.child (shopUid)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        String name = String.valueOf (dataSnapshot.child ("name").getValue ());
                        shopEmail = String.valueOf (dataSnapshot.child ("email").getValue ());
                        shopPhone = String.valueOf (dataSnapshot.child ("phone").getValue ());
                        profileImage = String.valueOf (dataSnapshot.child ("profileImage").getValue ());
                        accountType = String.valueOf (dataSnapshot.child ("accountType").getValue ());

                        //set Data
                        shopNameTv.setText (shopName);
                        emailTv.setText (shopEmail);
                        deliveryFeeTv.setText ("Delivery Fee S.P " + deliveryFee);
                        addressTv.setText (shopAddress);
                        phoneTv.setText (shopPhone);
                        try {
                            Picasso.get ().load (profileImage).into (shopIv);
                        } catch (Exception e) {
                            shopIv.setImageResource (R.drawable.ic_store_mall);
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

                      //  online = String.valueOf (dataSnapshot.child ("online").getValue ());
                        shopOpen = String.valueOf (dataSnapshot.child ("shop_open").getValue ());
                        shopName = String.valueOf (dataSnapshot.child ("shop_name").getValue ());
                        deliveryFee = String.valueOf (dataSnapshot.child ("deliveryFee").getValue ());
                       // shopNameTv.setText (sname);
                        //deliveryFeeTv.setText (deliveryFee);
                                              //check if shop is openned
                        if (shopOpen.equals ("true")){
                            openCloseTv.setVisibility (View.GONE);
                        }
                        else{
                            openCloseTv.setVisibility (View.VISIBLE);
                        }
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
    private void loadShopProducts () {
        //init list
        productList = new ArrayList<> ();

        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
        reference.child (shopUid).child ("Products")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //Clear list before adding items
                        productList.clear ();
                        for(DataSnapshot ds : dataSnapshot.getChildren ()){
                            ModelProducts modelProducts = ds.getValue (ModelProducts.class);
                            productList.add (modelProducts);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser (ShopDetailsActivity.this,productList);
                        productRv.setAdapter (adapterProductUser);
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
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Users");
        ref.child (firebaseAuth.getUid ()).updateChildren (hashMap)
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
                        showToastError ("Make Me Off line (Shop Details): " + e.getMessage ());
                    }
                });
    }
    private void checkUser () {
        FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user == null){
            startActivity(new Intent (ShopDetailsActivity.this, LoginActivity.class));
            finish ();
        }
        else {
             loadMyInfo();
        }

    }
    public void init(){
        shopIv = findViewById (R.id.shopDetailsIV);
        shopNameTv = findViewById (R.id.shopDetailsNameTv);
        phoneTv = findViewById (R.id.phoneDetailsTV);
        emailTv = findViewById (R.id.emailDetailsTV);
        openCloseTv = findViewById (R.id.openCloseTv);
        deliveryFeeTv = findViewById (R.id.deliveryFeeTV);
        addressTv = findViewById (R.id.addressDetailsTV);
        filteredProductstv = findViewById (R.id.filerProductsDetailsTV);
        callBtn = findViewById (R.id.callDetailIB);
        mailbtn = findViewById (R.id.emailDetailsIB);
        mapBtn = findViewById (R.id.locationIB);
        cartIB = findViewById (R.id.cartIB);
        backBtn = findViewById (R.id.backdetailsIB);
        filterProductBtn = findViewById (R.id.filerProductsDetailsIB);
        searchProductET = findViewById (R.id.searchProductDetailsTv);
        productRv = findViewById (R.id.productDetailsRV);
        cartCountTv = findViewById (R.id.cartCount);
        reviewBtn = findViewById (R.id.reviewsBtn);
        ratingBar = findViewById (R.id.ratingBar);
        contactIB= findViewById (R.id.contactIB);
    }

    public void showToastSuccess(String s){
        Toasty.success (ShopDetailsActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (ShopDetailsActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (ShopDetailsActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
}
