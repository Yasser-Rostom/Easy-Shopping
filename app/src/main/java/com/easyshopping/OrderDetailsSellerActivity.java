package com.easyshopping;

import Adapters.AdapterOrderedItem;
import Models.ModelOrderedItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.easyshopping.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private String orderId, orderBy;
    private ImageButton backBtn, editBtn, mapBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, emailTv, phoneTv, totalItemsTv, amountTv,addressTv;
    private RecyclerView itemRv;

    //FirebaseAuth
    FirebaseAuth firebaseAuth;
    //to open destiation in map
    private String sourceLatitude, sourceLongitude,destinationLatitude, destinationLongitude;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_order_details_seller);
        //initialization View
        init ();
        // get data from intent
        orderBy = getIntent ().getStringExtra ("orderBy");
        orderId = getIntent ().getStringExtra ("orderId");

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance ();
        loadMyInfo ();
        loadBuyerInfo ();
        loadOrderDetails();
        loadOrderItems();
        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });


        mapBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                openMap();
            }
        });

        editBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //edit order status; In progress, completed, cancelled
                editOrderStatusDialog();
            }
        });

    }

    private void editOrderStatusDialog () {
        //options to display  in dialog
        final String[] options = {"In Progress","Completed","Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setTitle ("Edit Order Status :")
                .setItems (options, new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick (DialogInterface dialog, int which) {
                        //handle item clicks
                        String selectedOption = options[which];
                        editOrderStatus(selectedOption);
                    }
                })
                .show ();

    }

    private void editOrderStatus (final String selectedOption) {
        //setup data to put in firebase db
        HashMap<String, Object> hashMap = new HashMap<> ();
        hashMap.put ("delivery_state", ""+selectedOption);
        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
        reference.child (firebaseAuth.getUid ()).child ("Orders").child (orderId)
                .updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //status updated
                        Toasty.success (OrderDetailsSellerActivity.this, "Order is now >> " + selectedOption,Toasty.LENGTH_LONG).show ();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        Toasty.success (OrderDetailsSellerActivity.this, "Update order status :" +e.getMessage (),Toasty.LENGTH_LONG).show ();
                    }
                });
    }

    private void loadOrderDetails () {
        //load order details
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child ("Orders").child (orderId)
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
                        amountTv.setText ("SYP" + orderCost + "[ Including delivery Fee SYP " + deliveryFee + "]");
                        dateTv.setText (formatDate);
                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }
    private void loadOrderItems () {
        //init list
        orderedItemArrayList = new ArrayList<> ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child("Orders").child (orderId).child ("Bill_Items")
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
                        adapterOrderedItem = new AdapterOrderedItem (OrderDetailsSellerActivity.this, orderedItemArrayList);
                        //set Adapter
                        itemRv.setAdapter (adapterOrderedItem);

                        //set items count
                        totalItemsTv.setText (""+dataSnapshot.getChildrenCount ());
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




    private void loadMyInfo () {

        //get shop info
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        sourceLatitude = "" + dataSnapshot.child ("Address").child ("latitude").getValue ();
                        sourceLongitude = "" + dataSnapshot.child ("Address").child ("longitude").getValue ();

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadBuyerInfo(){
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (orderBy)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        //get buyer info
                        destinationLatitude = "" + dataSnapshot.child ("Address").child ("latitude").getValue ();
                        destinationLongitude = "" + dataSnapshot.child ("Address").child ("longitude").getValue ();
                        String email = ""+ dataSnapshot.child ("email").getValue ();
                        String phone = "" + dataSnapshot.child ("phone").getValue ();
                        //set info
                        emailTv.setText (email);
                        phoneTv.setText (phone);

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void openMap () {
        //Saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr="+sourceLatitude + ","+sourceLongitude+"&daddr=" + destinationLatitude +","+destinationLongitude;
        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse (address));
        startActivity (intent);
    }


    public void init(){
        backBtn=findViewById (R.id.backBtn);
        editBtn=findViewById (R.id.editBtn);
        mapBtn=findViewById (R.id.mapBtn);
        orderIdTv=findViewById (R.id.orderIdTv);
        dateTv =findViewById (R.id.dateTv);
        orderStatusTv=findViewById (R.id.orderStatusTv);
        emailTv=findViewById (R.id.emailTv);
        phoneTv=findViewById (R.id.phoneTv);
        totalItemsTv=findViewById (R.id.totalItemsTv);
        amountTv=findViewById (R.id.amountTv);
        itemRv=findViewById (R.id.itemsRv);
        addressTv=findViewById (R.id.addressTv);

    }
}

