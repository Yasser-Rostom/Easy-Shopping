package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    //UI Views
    private EditText emailEt, passwordEt,phoneEt,phonePassword;
    private TextView forgotTv, noAccountTv, viaEmailTv, viaPhoneNumEt,phoneForgetTv;
    private Button logingBtn;
    ImageButton powerOff ,globalAdminBtn;
    private RelativeLayout cardlayout, phoneloginlayout;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    RelativeLayout layout, toolbar;
    //Declare SignInButton
    SignInButton mGoogleLoginBtn;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 100;
    String timestamp;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_login);

        // Necessary For Google log in
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken (getString(R.string.default_web_client_id))
                .requestEmail ()
                .build ();
        mGoogleSignInClient=  GoogleSignIn.getClient (this,gso);
        init ();



        firebaseAuth = FirebaseAuth.getInstance ();
        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);

        noAccountTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                startActivity (new Intent (LoginActivity.this, RegisterUserActivity.class));
            }
        });
        forgotTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                startActivity (new Intent (LoginActivity.this,ForgotPasswordActivity.class ));
            }
        });
        powerOff.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                finish ();

            }
        });

        globalAdminBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (LoginActivity.this, GlobalAdminCodeActivity.class);
                startActivity (intent);
            }
        });

        logingBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                loginUser();
            }
        });
        GoogleLogin();

        viaEmailTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                cardlayout.setVisibility (View.VISIBLE);
                phoneloginlayout.setVisibility (View.GONE);
            }
        });
        viaPhoneNumEt.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                phoneloginlayout.setVisibility (View.VISIBLE);
                cardlayout.setVisibility (View.GONE);
            }
        });
        //fade in animation
        Animation fadein = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        layout.startAnimation(fadein);

        //from above to below
        Animation animUpDown = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.up_down);

        // start the animation
        toolbar.startAnimation(animUpDown);
    }
    private String email, password;
    private void loginUser () {
        email = emailEt.getText ().toString ().trim ();
        password = passwordEt.getText ().toString ().trim ();
        if (!Patterns.EMAIL_ADDRESS.matcher (email).matches ()){
            showToastWarning ("Invalid email pattern");
            emailEt.setError ("Invalid Email");
            emailEt.setFocusable (true);
            return;
        }
        if (TextUtils.isEmpty (password)){
            showToastWarning ("Please Enter you password");
            passwordEt.setError ("Incorrect password");
            passwordEt.setFocusable (true);
            return;
        }
        progressDialog.setMessage ("Logging in ...");
        progressDialog.show ();

        firebaseAuth.signInWithEmailAndPassword (email,password)
                .addOnSuccessListener (new OnSuccessListener<AuthResult> () {
                    @Override
                    public void onSuccess (AuthResult authResult) {
                        //logged in successfully
                        makeMeOnline();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        // Failed logging in
                        progressDialog.dismiss ();
                        showToastWarning ("Failed logging in : " + e.getMessage ());
                    }
                });
    }
    String personType, status;
    String SellerOrCustomer;
    private void makeMeOnline () {

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.orderByChild ("person_Id").equalTo (firebaseAuth.getUid ())
                .addListenerForSingleValueEvent (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren ()){
                            personType = "" + ds.child ("accountType").getValue ();
                            status = ""+ ds.child ("status").getValue ();
                           if (status.equals ("disactive")) {
                                progressDialog.dismiss ();
                                showToastError ("This Account isn't Active or has been deleted !!!!! " );
                                firebaseAuth.signOut ();
                            }
                            else if (personType.equals ("seller") && status.equals ("active")){
                                //Make user online after logging in
                                progressDialog.setMessage ("Checking User....");
                                HashMap<String, Object> hashMap = new HashMap<> ();
                                hashMap.put ("online", "true");
                                //update value to database
                                DatabaseReference ref2 = FirebaseDatabase.getInstance ().getReference ("Persons");
                                ref2.child (firebaseAuth.getUid ()).child ("Seller").updateChildren (hashMap)
                                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                                            @Override
                                            public void onSuccess (Void aVoid) {
                                                //Updating is successfully
                                                checkUserType();
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
                            else if (personType.equals ("customer") && status.equals ("active")){
                                //Make user online after logging in
                                progressDialog.setMessage ("Checking User....");
                                HashMap<String, Object> hashMap = new HashMap<> ();
                                hashMap.put ("online", "true");
                                //update value to database
                                DatabaseReference ref1 = FirebaseDatabase.getInstance ().getReference ("Persons");
                                ref1.child (firebaseAuth.getUid ()).child ("Customer").updateChildren (hashMap)
                                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                                            @Override
                                            public void onSuccess (Void aVoid) {
                                                //Updating is successfully
                                                checkUserType();
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
                            else if (personType.equals ("admin")  && status.equals ("active")){
                                //Make user online after logging in
                                progressDialog.setMessage ("Checking User....");
                                HashMap<String, Object> hashMap = new HashMap<> ();
                                hashMap.put ("online", "true");
                                //update value to database
                                DatabaseReference ref1 = FirebaseDatabase.getInstance ().getReference ("Persons");
                                ref1.child (firebaseAuth.getUid ()).child ("Manager").updateChildren (hashMap)
                                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                                            @Override
                                            public void onSuccess (Void aVoid) {
                                                //Updating is successfully
                                                checkUserType();
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

                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });




      /*  //Make user online after logging in
        progressDialog.setMessage ("Checking User....");
        HashMap<String, Object> hashMap = new HashMap<> ();
        hashMap.put ("online", "true");
        //update value to database
        DatabaseReference ref1 = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child (SellerOrCustomer).updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //Updating is successfully
                        checkUserType();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        //Failed updateing
                        progressDialog.dismiss ();
                        showToastError ("Failed updating : " + e.getMessage ());
                    }
                });*/

    }

    private void checkUserType () {
        /**
         * If user is seller, start seller main screen
         * If user is buyer, start user main screen
         */

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.orderByChild ("person_Id").equalTo (firebaseAuth.getUid ())
                .addListenerForSingleValueEvent (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren ()){
                            String accountType = "" + ds.child ("accountType").getValue ();

                            if (accountType.equals ("admin")) {
                                progressDialog.dismiss ();
                                //User is admin
                                startActivity(new Intent (LoginActivity.this, MainAdminActivity.class));
                                finish ();
                            }
                            if (accountType.equals ("seller")){
                                progressDialog.dismiss ();
                                //User is Seller
                                startActivity(new Intent (LoginActivity.this, MainSellerActivity.class));
                                finish ();
                            }
                            if (accountType.equals ("customer")){
                                progressDialog.dismiss ();
                                //User is customer
                                startActivity(new Intent (LoginActivity.this, MainUserActivity.class));
                                finish ();
                            }

                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed (); // Go previous activity
        return super.onSupportNavigateUp ();
    }

    private void GoogleLogin () {
        mGoogleLoginBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                progressDialog = new ProgressDialog (LoginActivity.this);
                progressDialog.setMessage ("Google log in  ....");
                progressDialog.show ();
                //begin Google login proess
                //mGoogleSignInClient is independent method
                Intent signIntent =  mGoogleSignInClient.getSignInIntent();

                startActivityForResult (signIntent,RC_SIGN_IN);
            }
        });
        }


    //For google sign in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                // Google Sign In failed, update UI appropriately
                Toasty.error (LoginActivity.this,"Faild google : "+  e.getMessage (),Toasty.LENGTH_LONG).show();
                // ...
            }
        }
    }


   private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        progressDialog.setMessage ("Google Logging in ....");
        progressDialog.show ();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss ();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            //If the use is the First time login then he will show his information
                            if (task.getResult ().getAdditionalUserInfo ().isNewUser ()){
                                // 05 step 5
                                // 05 step 4
                                String email = user.getEmail ();
                                String uid = user.getUid ();
                                String name = user.getDisplayName ();
                                String Phone = user.getPhoneNumber ();
                                timestamp = String.valueOf (System.currentTimeMillis ());

                                //When user is registered store user info in firebase database too
                                //using Hashmap
                                HashMap<Object, String> hashMap = new HashMap<> ();
                                //put info in hashmap
                                hashMap.put ("person_Id", uid);
                                hashMap.put ("first_name", name);
                                hashMap.put ("last_name", "");
                                hashMap.put ("birthdate", "");
                                hashMap.put ("email", email);
                                hashMap.put ("profileImage", "");//Will add later (e.g. edit profile
                                hashMap.put ("phone", Phone);//Will add later (e.g. edit profile
                                hashMap.put ("accountType", "customer");
                                hashMap.put ("timestamp", timestamp);//Will add later (e.g. edit profile
                                hashMap.put ("status", "active");
                                hashMap.put ("onlineStatus", "online");
                                hashMap.put ("typingTo", "noOne");
                                //05 step 6
                                //firebase database instance
                                //Path to store user data named "persons"
                                DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Persons");
                                //put data within hashmap in database
                                reference.child (uid).setValue (hashMap);

                            }

                            //create Address Entity
                            createAddress ();
                            //Create contact Entity
                            createContact ();
                            createCustomer ();
                            //Our database updated
                            progressDialog.dismiss ();
                            startActivity (new Intent (LoginActivity.this, MainUserActivity.class));
                            finish ();
                        } else {
                            progressDialog.dismiss ();
                            // If sign in fails, display a message to the user.
                            Toasty.warning (LoginActivity.this,"signInWithCredential:failure",Toasty.LENGTH_LONG).show();

                            //updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure (@NonNull Exception e) {
                progressDialog.dismiss ();
                //Show Error message
                Toasty.error (LoginActivity.this,e.getMessage (),Toasty.LENGTH_LONG).show();
            }
        });
    }




    private void createAddress () {

        HashMap<String, Object> address = new HashMap<> ();
        address.put("address_Id", String.valueOf (firebaseAuth.getUid ()));
        address.put("country", "");
        address.put("state", "");
        address.put("city", "");
        address.put("address", "");
        address.put("latitude","");
        address.put("longitude", "");
        DatabaseReference addressRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (firebaseAuth.getUid ()).child ("Address");
        addressRef.setValue (address)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {

                    }
                }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure (@NonNull Exception e) {

            }
        });

    }

    private void createContact () {
        HashMap<String, Object> contact = new HashMap<> ();
        contact.put("contact_Id", String.valueOf (firebaseAuth.getUid ()));
        contact.put("mobile1", "");
        contact.put("mobile2","");
        contact.put("alter_email", "");
        contact.put("Fixed_phone", "");
        contact.put("Facebook", "");
        contact.put("twiter", "");
        DatabaseReference contactRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (firebaseAuth.getUid ()).child ("Contact");
        contactRef.setValue (contact)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {

                    }
                }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure (@NonNull Exception e) {

            }
        });
    }

    private void createCustomer(){


        HashMap<String, Object> customer = new HashMap<> ();
        customer.put("cust_Id", String.valueOf (firebaseAuth.getUid ()));
        customer.put("person_Id", String.valueOf (firebaseAuth.getUid ()));
        customer.put("address_Id",String.valueOf (firebaseAuth.getUid ()));
        customer.put("contact_Id", String.valueOf (firebaseAuth.getUid ()));
        customer.put("timestamp",timestamp);
        customer.put("online","true");
        customer.put("order_Id", "");
        customer.put("card_Id", "");
        DatabaseReference customerRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (firebaseAuth.getUid ()).child ("Customer");
        customerRef.setValue (customer)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {

                    }
                }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure (@NonNull Exception e) {

            }
        });
    }


    public void init(){
        emailEt = findViewById (R.id.emailEt);
        passwordEt = findViewById (R.id.passwordEt);
        forgotTv = findViewById (R.id.forgetTv);
        noAccountTv = findViewById (R.id.noAccount);
        logingBtn = findViewById (R.id.LoginBtn);
        powerOff = findViewById (R.id.backBtn);
        mGoogleLoginBtn = (SignInButton) findViewById (R.id.googleLoginBtn);
        viaEmailTv = findViewById (R.id.ViaEmailTV);
        viaPhoneNumEt=findViewById (R.id.ViaPhoneTV);
        cardlayout=findViewById (R.id.loginRl);
        phoneloginlayout=findViewById (R.id.phonelogin);
        phoneForgetTv=findViewById (R.id.phoneForgetTv);
        phoneEt=findViewById (R.id.phoneEt);
        phonePassword=findViewById (R.id.phonePasswordEt);
        globalAdminBtn=findViewById (R.id.globalAdminBtn);

        layout = findViewById(R.id.loginLL);
        toolbar = findViewById(R.id.toobarRl);
    }

    public void showToastSuccess(String s){
        Toasty.success (LoginActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (LoginActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (LoginActivity.this,s,Toasty.LENGTH_LONG).show ();
    }

}

