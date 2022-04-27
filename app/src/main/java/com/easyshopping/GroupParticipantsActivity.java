package com.easyshopping;

import Adapters.AdapterParticipantsAdd;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantsActivity extends AppCompatActivity {

    private RecyclerView userRv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupId;
    private String myGroupRole;

    private ArrayList<ModelShops> userlist;
    private AdapterParticipantsAdd adapterParticipantsAdd;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_group_participants);
        init ();

        actionBar = getSupportActionBar ();
        actionBar.setTitle ("Add Participants");
        actionBar.setDisplayShowHomeEnabled (true);
        actionBar.setDisplayHomeAsUpEnabled (true);

        Intent intent = getIntent ();
        groupId = intent.getStringExtra ("groupId");

        firebaseAuth = FirebaseAuth.getInstance ();
        loadGroupInfo();
        userlist = new ArrayList<> ();

    }

    private void getAllUsers () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
            userlist.clear ();
            for (DataSnapshot ds: dataSnapshot.getChildren ()){
                ModelShops modelUser = ds.getValue (ModelShops.class);
              //get All users accept currently signed in
                if (!firebaseAuth.getUid ().equals (modelUser.getPerson_Id ())){
                    //not my uid
                    userlist.add (modelUser);
                }

            }
                adapterParticipantsAdd = new AdapterParticipantsAdd (GroupParticipantsActivity.this, userlist,groupId,myGroupRole);
                userRv.setAdapter (adapterParticipantsAdd);
            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadGroupInfo () {

        final DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");

        DatabaseReference ref1 = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref1.orderByChild ("groupId").equalTo (groupId).addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
           for (DataSnapshot ds : dataSnapshot.getChildren ()){
               String groupId = String.valueOf (ds.child ("groupId").getValue ());
               final String groupTitle = String.valueOf (ds.child ("groupTitle").getValue ());
               String groupDescription = String.valueOf (ds.child ("groupDescription").getValue ());
               String groupIcon = String.valueOf (ds.child ("groupIcon").getValue ());
               String createdBy = String.valueOf (ds.child ("createdBy").getValue ());
               String timestamp = String.valueOf (ds.child ("timestamp").getValue ());
               actionBar.setTitle ("Add Participants...");
               ref.child (groupId).child ("Participants").child (firebaseAuth.getUid ())
                       .addValueEventListener (new ValueEventListener () {
                           @Override
                           public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists ()){
                                myGroupRole = "" + dataSnapshot.child ("role").getValue ();
                                actionBar.setTitle (groupTitle + "[ " + myGroupRole + " ]");
                                getAllUsers();
                            }
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

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed ();
        return super.onSupportNavigateUp ();
    }

    public void init(){

        userRv = findViewById (R.id.userRv);

    }
}
