package com.easyshopping;

import Adapters.AdapterParticipantsAdd;
import Models.ModelSeller;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    //Views
    private TextView groupDescriptionTv,createdByTv,editGroupTv,addParticipantTv,leaveGroupTv,participantNumberTv;
    private ImageView groupIconIv;
    private RecyclerView participantsRv;

    private String groupId, myGroupRole="";

    FirebaseAuth firebaseAuth;
    //Action Bar
    private ActionBar actionBar;

    private ArrayList<ModelShops> userList;
    private AdapterParticipantsAdd adapterParticipantsAdd;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_group_info);
        init ();


        actionBar= getSupportActionBar ();
        actionBar.setDisplayHomeAsUpEnabled (true);
        actionBar.setDisplayShowHomeEnabled (true);

        groupId = getIntent ().getStringExtra ("groupId");
        myGroupRole = getIntent ().getStringExtra ("groupRole");

        firebaseAuth= FirebaseAuth.getInstance ();
        userList = new ArrayList<> ();
        loadGroupRole();
        loadGroupInfo();

        if (myGroupRole.equals ("participant")){
            addParticipantTv.setVisibility (View.GONE);
        }

        addParticipantTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (GroupInfoActivity.this, GroupParticipantsActivity.class);
                intent.putExtra ("groupId", groupId);
                startActivity (intent);
            }
        });
        editGroupTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra ("groupId", groupId);
                startActivity (intent);
            }
        });

        leaveGroupTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //if user is participant/admin: leave group
                //if user is creator:delete group
                String dialogTitle="";
                String dialogDescription="";
                String positionButtonTitle="";
                if (myGroupRole.equals ("creator")){
                    dialogTitle="Delete Group :";
                    dialogDescription = "Are you sure you want to delete group permanently ?";
                    positionButtonTitle = "DELETE";

                }
                else {
                    dialogTitle="Leave Group :";
                    dialogDescription = "Are you sure you want to Leave group permanently ?";
                    positionButtonTitle = "Leave";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder (GroupInfoActivity.this);
                builder.setTitle (dialogTitle)
                        .setMessage (dialogDescription)
                        .setPositiveButton (positionButtonTitle, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                if (myGroupRole.equals ("creator")){
                                 //I'm creator of gruop : delete group
                                 deleteGroup();
                                   }
                                else{
                                    //I'm participant/admin:leave Group
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton ("CANCEL", new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                dialog.dismiss ();
                            }
                        }).show ();
            }
        });

    }

    private void leaveGroup () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants").child (firebaseAuth.getUid ())
                .removeValue ()
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                   //group left successfully
                        Toasty.warning (GroupInfoActivity.this, "Group left successfully ...", Toasty.LENGTH_LONG).show ();
                        startActivity (new Intent (GroupInfoActivity.this, DashboardActivity.class));
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        Toasty.warning (GroupInfoActivity.this, "" + e.getMessage (), Toasty.LENGTH_LONG).show ();
                    }
                });

    }

    private void deleteGroup () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId)
                .removeValue ()
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //group left successfully
                        Toasty.warning (GroupInfoActivity.this, "Group Deleted successfully ...", Toasty.LENGTH_LONG).show ();
                        startActivity (new Intent (GroupInfoActivity.this, DashboardActivity.class));
                        finish ();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        Toasty.warning (GroupInfoActivity.this, "" + e.getMessage (), Toasty.LENGTH_LONG).show ();
                    }
                });

    }

    private void loadGroupRole () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.orderByChild ("groupId").equalTo (groupId).addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
           for(DataSnapshot ds: dataSnapshot.getChildren ()) {
               //get group Info
               String groupId = "" +ds.child ("groupId").getValue ();
               String groupTitle = "" +ds.child ("groupTitle").getValue ();
               String groupDescription = "" +ds.child ("groupDescription").getValue ();
               String groupuIcon = "" +ds.child ("groupIcon").getValue ();
               String createdBy = "" +ds.child ("createdBy").getValue ();
               String timestamp = "" +ds.child ("timestamp").getValue ();

               //Convert time stamp to dd/mm/yyyy hh:mm am/pm
               Calendar cal = Calendar.getInstance (Locale.ENGLISH);
               cal.setTimeInMillis (Long.parseLong (timestamp));
               String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
               loadCreatorInfo(dateTime, createdBy);

               //set group info
               actionBar.setTitle (groupTitle);
               groupDescriptionTv.setText (groupDescription);

               try {
                   Picasso.get ().load (groupuIcon).placeholder (R.drawable.ic_group_primary).into (groupIconIv);

               }catch (Exception e){
                   groupIconIv.setImageResource (R.drawable.ic_group_primary);
               }



           }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCreatorInfo (final String dateTime, String createdBy) {
     DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
     ref.orderByChild ("person_Id").equalTo (createdBy).addValueEventListener (new ValueEventListener () {
         @Override
         public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren ()){
            String fname = "" + ds.child ("first_name").getValue ();
            String lname = "" + ds.child ("last_name").getValue ();
            String name = fname + " " + lname;
            createdByTv.setText ("Created By : " + name + " on : " + dateTime);


        }
         }

         @Override
         public void onCancelled (@NonNull DatabaseError databaseError) {

         }
     });
    }

    private void loadGroupInfo () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants").orderByChild ("person_Id")
                .equalTo (firebaseAuth.getUid ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren ()){
                            myGroupRole = "" + ds.child ("role").getValue ();
                            actionBar.setSubtitle (firebaseAuth.getCurrentUser ().getEmail ()+ "(" + myGroupRole + ")");
                            if (myGroupRole.equals ("participant")){
                                editGroupTv.setVisibility (View.GONE);
                                addParticipantTv.setVisibility (View.GONE);
                                leaveGroupTv.setText ("Leave Group");

                            }
                            else if (myGroupRole.equals ("admin")){
                                editGroupTv.setVisibility (View.GONE);
                                addParticipantTv.setVisibility (View.VISIBLE);
                                leaveGroupTv.setText ("Leave Group");
                            }
                            else if (myGroupRole.equals ("creator")){
                                editGroupTv.setVisibility (View.VISIBLE);
                                addParticipantTv.setVisibility (View.VISIBLE);
                                leaveGroupTv.setText ("Delete Group");
                            }

                            loadParticipatns();
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadParticipatns () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        userList.clear ();
                       for (DataSnapshot ds : dataSnapshot.getChildren ()){
                           //get uid
                           String uid = "" + ds.child ("person_Id").getValue ();
                          //get Info user using uid we got above
                           DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
                           reference.orderByChild ("person_Id").equalTo (uid).addValueEventListener (new ValueEventListener () {
                               @Override
                               public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                                   for (DataSnapshot ds : dataSnapshot.getChildren ()){
                                       ModelShops modelUser = ds.getValue (ModelShops.class);
                                       userList.add (modelUser);
                                   }
                                   adapterParticipantsAdd = new AdapterParticipantsAdd (GroupInfoActivity.this, userList, groupId,myGroupRole);
                                   participantNumberTv.setText ("Participants (" + userList.size ()+")");
                                   participantsRv.setAdapter (adapterParticipantsAdd);
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
        groupDescriptionTv=findViewById (R.id.groupDescriptionTv);
        createdByTv=findViewById (R.id.createdBy);
        editGroupTv=findViewById (R.id.editGroupTv);
        addParticipantTv=findViewById (R.id.addParticipantTv);
        leaveGroupTv=findViewById (R.id.leaveGroupTv);
        participantNumberTv=findViewById (R.id.participantNumberTv);
        groupIconIv=findViewById (R.id.groupIconIv);
        participantsRv=findViewById (R.id.participantsRv);


    }
}
