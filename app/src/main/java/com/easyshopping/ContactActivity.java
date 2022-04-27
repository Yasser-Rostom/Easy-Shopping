package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ContactActivity extends AppCompatActivity {

    private EditText mobile1ET,mobile2ET,fixedPhoneET,AdditionalEmailEt,facebookEt,twitterEt;
    private Button confirmBtn;
    private ImageButton backBtn;


    //person_Id
    private String person_Id;

    //declare varaibles to obtain view inormation
    private String mobile1, mobile2,fixedPhone, additionalEmail, facebookLink,twitterLink;
    private ProgressDialog progressDialog;

    //FirebaseAuth
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_contact);
        init ();
        //Obtain person_Id sent from MainSellerActivity.class
        Intent intent = getIntent ();
        person_Id = intent.getStringExtra ("shopUid");

        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);


        //init FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance ();


        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
       confirmBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                updateContact ();
            }
        });

        loadInfo();
    }



    public void updateContact(){
        progressDialog.setMessage ("Saving Contact Info...");
        progressDialog.show ();

        mobile1 = mobile1ET.getText ().toString ().trim ();
        mobile2 = mobile2ET.getText ().toString ().trim ();
        fixedPhone = fixedPhoneET.getText ().toString ().trim ();
        additionalEmail = AdditionalEmailEt.getText ().toString ().trim ();
        facebookLink = facebookEt.getText ().toString ().trim ();
        twitterLink = twitterEt.getText ().toString ().trim ();

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons").child (person_Id);
        HashMap<String ,Object> hashMap = new HashMap<> ();
        hashMap.put ("contact_Id",person_Id);
        hashMap.put ("mobile1",mobile1);
        hashMap.put ("mobile2",mobile2);
        hashMap.put ("alter_email",fixedPhone);
        hashMap.put ("Fixed_phone",additionalEmail);
        hashMap.put ("Facebook",facebookLink);
        hashMap.put ("twiter",twitterLink);
        ref.child ("Contact").updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        progressDialog.dismiss ();
                        showToastSuccess ("Your Contacts had been updated....");
                        loadInfo();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        showToastError ("Some thing went wrong....");
                    }
                });
    }

    public void loadInfo(){
        // Load user information and set them to views
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (person_Id)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                            String mobile1 = String.valueOf (dataSnapshot.child ("Contact").child ("mobile1").getValue ());
                            String mobile2 = String.valueOf (dataSnapshot.child ("Contact").child ("mobile2").getValue ());
                            String fixedPhone = String.valueOf (dataSnapshot.child ("Contact").child ("alter_email").getValue ());
                            String alter_email = String.valueOf (dataSnapshot.child ("Contact").child ("Fixed_phone").getValue ());
                            String facebook = String.valueOf (dataSnapshot.child ("Contact").child ("Facebook").getValue ());
                            String twitter = String.valueOf (dataSnapshot.child ("Contact").child ("twiter").getValue ());

                            String contact_Id = String.valueOf (dataSnapshot.child ("contact_Id").getValue ());

                            mobile1ET.setText (mobile1);
                            mobile2ET.setText (mobile2);
                            fixedPhoneET.setText (fixedPhone);
                            AdditionalEmailEt.setText (alter_email);
                            facebookEt.setText (facebook);
                            twitterEt.setText (twitter);


                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }


    public void init(){

        mobile1ET = findViewById (R.id.mobile1ET);
        mobile2ET = findViewById (R.id.mobile2ET);
        fixedPhoneET = findViewById (R.id.fixedPhoneET);
        AdditionalEmailEt = findViewById (R.id.AdditionalEmailEt);
        facebookEt = findViewById (R.id.facebookEt);
        twitterEt = findViewById (R.id.twitterEt);
        confirmBtn = findViewById (R.id.confirmContactBtn);
        backBtn = findViewById (R.id.backBtn);
    }

    public void showToastSuccess(String s){
        Toasty.success (ContactActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (ContactActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (ContactActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
}
