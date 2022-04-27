package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.easyshopping.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    //Views
    private ImageButton backBtn;
    private EditText emailEt;
    private Button recoverBtn;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_forgot_password);
        init ();

        firebaseAuth = FirebaseAuth.getInstance ();
        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);

        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });

        recoverBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                recoverPassword();
            }
        });
    }
    private String email;
    private void recoverPassword () {
        email = emailEt.getText ().toString ().trim ();
        if (!Patterns.EMAIL_ADDRESS.matcher (email).matches ()){
            showToastError ("Invalid Email....");
            return;
        }
        progressDialog.setMessage ("Sending instructions to reset password...");
        progressDialog.show ();

        firebaseAuth.sendPasswordResetEmail (email)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //Instruction sent
                        progressDialog.dismiss ();
                        showToastSuccess ("Password reset insrtuction sent to your email...");
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        //Failed sending instructions
                        progressDialog.dismiss ();
                        showToastError ("Failed sending instructions : " + e.getMessage ());
                    }
                });
    }



    public void init (){
        backBtn = findViewById (R.id.backBtn);
        emailEt = findViewById (R.id.emailEt);
        recoverBtn = findViewById (R.id.recoverBtn);
    }

    public void showToastSuccess(String s){
        Toasty.success (ForgotPasswordActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (ForgotPasswordActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (ForgotPasswordActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
}
