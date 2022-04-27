package com.easyshopping;

import Adapters.AdapterOrderedItem;
import Models.ModelOrderedItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.content.Intent;
import android.icu.util.ULocale;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUserActivity extends AppCompatActivity {

    private String orderTo, orderId;

    private ImageButton backBtn, writeReviewBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, shopNameTv, totalItemsTv, amountTv,addressTv;
    private RecyclerView itemsRV;
    FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_order_details_user);
        init ();
        firebaseAuth = FirebaseAuth.getInstance ();
        final Intent intent = getIntent ();
        orderId = intent.getStringExtra ("orderId");
        orderTo = intent.getStringExtra ("orderTo");
        loadShopInfo();
        loadOrderDetails();
        loadOrderItems();

        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
        try {
            //Handle writeReviewBtn click, start write review activity
            writeReviewBtn.setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick (View v) {
                    Intent i = new Intent (OrderDetailsUserActivity.this, WriteReviewActivity.class);
                    i.putExtra ("orderTo", orderTo);
                    startActivity (i);
                }
            });
        }catch (Exception e){
            Toasty.warning (this, e.getMessage (), Toasty.LENGTH_LONG).show ();
        }


    }

    private void loadOrderItems () {
        //init list
        orderedItemArrayList = new ArrayList<> ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (orderTo).child("Orders").child (orderId).child ("Bill_Items")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        orderedItemArrayList.clear ();
                        for (DataSnapshot ds: dataSnapshot.getChildren ()){
                            ModelOrderedItem modelOrderedItem = ds.getValue (ModelOrderedItem.class);
                            //add to list
                            orderedItemArrayList.add (modelOrderedItem);
                        }
                        //all items added to list
                        //setup adapter
                        adapterOrderedItem = new AdapterOrderedItem (OrderDetailsUserActivity.this, orderedItemArrayList);
                        //set Adapter
                        itemsRV.setAdapter (adapterOrderedItem);

                        //set items count
                        totalItemsTv.setText (""+dataSnapshot.getChildrenCount ());
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadOrderDetails () {

        //load order details
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (orderTo).child ("Orders").child (orderId)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //get Data
                        String orderBy, orderCost, orderId, orderStatus, orderTime,orderTo, deliveryFee,latitude,longitude;
                        orderBy = String.valueOf (dataSnapshot.child ("customer_Id").getValue ());
                        orderCost = String.valueOf (dataSnapshot.child ("total_amount").getValue ());
                        orderId = String.valueOf (dataSnapshot.child ("orderId").getValue ());
                        orderStatus = String.valueOf (dataSnapshot.child ("delivery_state").getValue ());
                        orderTime = String.valueOf (dataSnapshot.child ("date").getValue ());
                        orderTo = String.valueOf (dataSnapshot.child ("orderTo").getValue ());
                        deliveryFee = String.valueOf (dataSnapshot.child ("deliveryFee").getValue ());
                        latitude = String.valueOf (dataSnapshot.child ("latitude").getValue ());
                        longitude = String.valueOf (dataSnapshot.child ("longtitude").getValue ());

                        //conver timestamp to proper format
                        Calendar calendar = Calendar.getInstance ();
                        calendar.setTimeInMillis (Long.parseLong(orderTime));
                        String formatDate = DateFormat.format ("dd/MM/yyyy hh:mm a", calendar ).toString ();

                        if (orderStatus.equals ("In Progress")){
                            orderStatusTv.setTextColor (getResources ().getColor (R.color.colorPrimary));

                        }
                        else if (orderStatus.equals ("Completed")){
                            orderStatusTv.setTextColor (getResources ().getColor (R.color.colorGreen));
                        }
                        else if (orderStatus.equals ("Cancelled")){
                            orderStatusTv.setTextColor (getResources ().getColor (R.color.colorRed2));

                        }
                        orderIdTv.setText (orderId);
                        orderStatusTv.setText (orderStatus);
                        amountTv.setText ("S.P" + orderCost + "[Including delivery Fee S.P" + deliveryFee);
                        dateTv.setText (formatDate);
                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void findAddress (String latitude, String longitude) {
        double lat = Double.parseDouble (latitude);
        double lon = Double.parseDouble (longitude);

        //find address, country , state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder (this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation (lat,lon, 1);
            String address = addresses.get(0).getAddressLine (0); //complete address
            addressTv.setText (address);

        } catch (IOException e) {
            e.printStackTrace ();

        }
    }

    private void loadShopInfo () {

        //get shop info
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (orderTo)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        String shopName = String.valueOf (dataSnapshot.child ("Seller").child ("shop_name").getValue ());
                        shopNameTv.setText (shopName);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void init(){
        backBtn = findViewById (R.id.backBtn);
        orderIdTv = findViewById (R.id.orderIdTv);
        dateTv = findViewById (R.id.dateTv);
        orderStatusTv = findViewById (R.id.orderStatusTv);
        shopNameTv = findViewById (R.id.shopNameTv);
        amountTv = findViewById (R.id.amountTv);
        addressTv = findViewById (R.id.addressTv);
        totalItemsTv = findViewById (R.id.totalItemsTv);
        itemsRV = findViewById (R.id.itemsRv);
        writeReviewBtn = findViewById (R.id.writeReviewBtn);

    }
}
