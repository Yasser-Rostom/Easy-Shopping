package com.easyshopping;

import Adapters.AdapterShops;
import Adapters.AdapterUsers;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MainAdminActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    private ImageButton logoutBtn,editProfileBtn;
    private TextView UserNameTV,userEmailTV,userPhoneTv;
    private ImageView UserProfileIV;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private RecyclerView shopsRv;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private ArrayList<ModelShops> userslist;
    private AdapterUsers adapterUsers;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        // ... Rest of onCreate code.
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main_admin);
        //init views
        init ();

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle (MainAdminActivity.this, drawer, toolbar,
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
        userslist = new ArrayList<> ();
        //Check user
        checkUser ();
        //load user
        loadUsers();

    }



    private void loadUsers () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding
                        userslist.clear ();
                        for (DataSnapshot ds:dataSnapshot.getChildren ()){
                            ModelShops modelUsers = ds.getValue (ModelShops.class);
                            userslist.add (modelUsers);

                            }
                        //setup adapter
                        adapterUsers= new AdapterUsers (MainAdminActivity.this, userslist);
                        //set adapter to recyclerView
                        shopsRv.setAdapter (adapterUsers);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }


    //Make me off line when log out
    private void checkUser () {
        FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user == null){
            startActivity(new Intent (MainAdminActivity.this, LoginActivity.class));
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
                            UserNameTV.setText(name);
                            userEmailTV.setText(email);
                            userPhoneTv.setText(phone);
                            try {
                                Picasso.get ().load (profileImage).placeholder (R.drawable.ic_store_mall)
                                        .into (UserProfileIV);
                            }catch (Exception e){
                                Picasso.get ().load (R.drawable.ic_store_mall).into (UserProfileIV);
                            }

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
        ref.child (firebaseAuth.getUid ()).child ("Manager").updateChildren (hashMap)
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
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById (R.id.drawer_layout);
        // Handle navigation view item clicks here.
        switch (item.getItemId ()) {

            case R.id.editProfileBtnUnav:
                drawer.closeDrawer (GravityCompat.START);
                //Open Edit profile activity
                startActivity (new Intent (MainAdminActivity.this, ProfileEditAdminActivity.class));
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

        public void showToastSuccess (String s){
            Toasty.success (MainAdminActivity.this, s, Toasty.LENGTH_LONG).show ();
        }
        public void showToastWarning (String s){
            Toasty.warning (MainAdminActivity.this, s, Toasty.LENGTH_LONG).show ();
        }
        public void showToastError (String s){
            Toasty.error (MainAdminActivity.this, s, Toasty.LENGTH_LONG).show ();
        }

        public void init(){

            drawer = findViewById (R.id.drawer_layout);
            toolbar = findViewById (R.id.toolbar);

            UserNameTV = findViewById (R.id.UserNameTV);
            userEmailTV = findViewById (R.id.userEmailTV);
            userPhoneTv = findViewById (R.id.userPhoneTv);
            UserProfileIV = findViewById (R.id.UserProfileIV);
            shopsRv = findViewById (R.id.shopsRv);
        }
}

