package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShopContactInfoActivity extends AppCompatActivity {

    private TextView mobile1ET,mobile2ET,fixedPhoneET,AdditionalEmailEt,facebookEt,twitterEt;
    private ImageButton backBtn;

    private String person_Id;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_shop_contact_info);

        init();

        Intent intent = getIntent ();
        person_Id = intent.getStringExtra ("shopUid");

        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
        loadInfo ();
    }


    public void loadInfo() {
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
        backBtn = findViewById (R.id.backBtn);
    }

    public void showToastSuccess(String s){
        Toasty.success (ShopContactInfoActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (ShopContactInfoActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (ShopContactInfoActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
}
