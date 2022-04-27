package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import es.dmoral.toasty.Toasty;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditAdminActivity extends AppCompatActivity implements LocationListener {
    private ImageButton backBtn,gpsBtn;
    private ImageView profileIv;
    private EditText firstNameEt, lastNameEt, countryEt,cityEt, StateEt,addressEt, phoneEt;
    private Button Update;

    //Variables for location
    private double latitude, longitude;
    //Location Manager
    private LocationManager locationManager;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 50;
    //PERMISSION ARRAYS
    private String[] locationPermissions;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int Storage_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;
    //uri of picked image
    Uri image_uri;
    String profileOrCoverPhoto;

    //Arrays of permissions to be requested
    String cameraPermission[];
    String storagePermissions[];

    private String fullName,firstName,lastName , phoneNum, country, state, city, address;
    FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_profile_edit_user);
        init ();


        //initialization permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        //Init arrays of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance ();
        checkUser();
        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);


        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });


        gpsBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //detect current location
                if (checkLocationPermission ()) {
                    // you are allowed to access location
                    detectLocation ();
                } else {
                    // You need permission
                    requestLocationPermission ();
                }

            }
        });

        pickImage();
        Update.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Update data
                UpdateMyData();
            }
        });
    }

    private void UpdateMyData () {
        //Input data
        firstName = firstNameEt.getText ().toString ().trim ();
        lastName =lastNameEt.getText ().toString ().trim ();
        phoneNum = phoneEt.getText ().toString ().trim ();
        country = countryEt.getText ().toString ().trim ();
        state = StateEt.getText ().toString ().trim ();
        city = cityEt.getText ().toString ().trim ();
        address = addressEt.getText ().toString ().trim ();

        try{
            if(TextUtils.isEmpty (firstName)){
                firstNameEt.setError ("Enter First Name ...");
                firstNameEt.setFocusable (true);
                return;
            }

            if(TextUtils.isEmpty (lastName)){
                lastNameEt.setError ("Enter last Name ...");
                lastNameEt.setFocusable (true);
                return;
            }
            if(TextUtils.isEmpty (phoneNum)){
                showToastWarning ("Enter Phone Number ...");
                phoneEt.setError ("Enter Phone");
                return;
            }

            if(TextUtils.isEmpty (country)){
                showToastWarning ("Enter country ...");
                return;
            }
            if(TextUtils.isEmpty (state)){
                showToastWarning ("Enter state ...");
                return;
            }
            if(TextUtils.isEmpty (city)){
                showToastWarning ("Enter city ...");
                return;
            }
            if(TextUtils.isEmpty (address)){
                showToastWarning ("Enter address ...");
                return;
            }

            if(latitude==0.0 || longitude ==0.0){
                showToastWarning ("Please click GPS button to detect you location...");
                return;
            }
        }catch (Exception e){
            showToastError ("Exmine all fields : " + e.getMessage ());
        }
        UpdateDataintoDababase();
    }
    private String timestamp;
    private void UpdateDataintoDababase () {
        progressDialog.setMessage ("Saving Account Info...");
        progressDialog.show ();
        timestamp = String.valueOf (System.currentTimeMillis ());

        if (image_uri == null) {
            //save info without image

            //setup data to save
            HashMap<String, Object> person = new HashMap<> ();
            person.put ("first_name", String.valueOf (firstName));
            person.put ("last_name", String.valueOf (lastName));
            person.put ("phone", String.valueOf (phoneNum));
            person.put ("profileImage", "");
            //Save to database
            DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
            ref.child (firebaseAuth.getUid ()).updateChildren (person)
                    .addOnSuccessListener (new OnSuccessListener<Void> () {
                        @Override
                        public void onSuccess (Void aVoid) {
                            //create Address Entity
                            updateAddress ();
                            //Create contact Entity
                            // updateContact ();

                            updateCustomer ();
                            //Our database updated
                            progressDialog.dismiss ();
                            startActivity (new Intent (ProfileEditAdminActivity.this, MainUserActivity.class));
                            finish ();

                        }
                    })
                    .addOnFailureListener (new OnFailureListener () {
                        @Override
                        public void onFailure (@NonNull Exception e) {
                            //Failed updating db
                            progressDialog.dismiss ();
                            startActivity (new Intent (ProfileEditAdminActivity.this, MainUserActivity.class));
                            finish ();
                        }
                    });
        } else {
            //Save info with image

            //name and path of image
            String filePathAndName = "profile_images/" + "" + firebaseAuth.getUid ();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance ().getReference (filePathAndName);
            storageReference.putFile (image_uri)
                    .addOnSuccessListener (new OnSuccessListener<UploadTask.TaskSnapshot> () {
                        @Override
                        public void onSuccess (UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage ().getDownloadUrl ();
                            while (!uriTask.isSuccessful ()) ;
                            Uri downloadImageUri = uriTask.getResult ();
                            if (uriTask.isSuccessful ()) {
                                //setup data to save
                                HashMap<String, Object> person = new HashMap<> ();
                                person.put ("first_name", String.valueOf (firstName));
                                person.put ("last_name", String.valueOf (lastName));
                                person.put ("phone", String.valueOf (phoneNum));
                                person.put ("profileImage",""+ downloadImageUri);
                                //Save to database
                                DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
                                ref.child (firebaseAuth.getUid ()).updateChildren (person)
                                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                                            @Override
                                            public void onSuccess (Void aVoid) {
                                                //create Address Entity
                                                updateAddress ();
                                                //Create contact Entity
                                                // updateContact ();
                                                updateCustomer ();
                                                //Our database updated
                                                progressDialog.dismiss ();
                                                startActivity (new Intent (ProfileEditAdminActivity.this, MainAdminActivity.class));
                                                finish ();

                                            }
                                        })
                                        .addOnFailureListener (new OnFailureListener () {
                                            @Override
                                            public void onFailure (@NonNull Exception e) {
                                                //Failed updating db
                                                progressDialog.dismiss ();
                                                startActivity (new Intent (ProfileEditAdminActivity.this, MainAdminActivity.class));
                                                finish ();
                                            }
                                        });
                            }

                        }
                    })
                    .addOnFailureListener (new OnFailureListener () {
                        @Override
                        public void onFailure (@NonNull Exception e) {
                            showToastError ("Create user : " + e.getMessage ());
                        }
                    });
        }
    }



    private void updateContact () {
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

    boolean isShopOpen = false;
    private void updateCustomer () {

        HashMap<String, Object> customer = new HashMap<> ();

        customer.put("cust_Id",  firebaseAuth.getUid ());
        customer.put("address_Id", firebaseAuth.getUid ());
        customer.put("card_ID",  firebaseAuth.getUid ());
        customer.put("order_Id", "");
        customer.put("contact_Id", firebaseAuth.getUid ());
        customer.put("person_Id", firebaseAuth.getUid ());
        customer.put("timestamp", timestamp);
        customer.put("online", "true");
        DatabaseReference sellerRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (firebaseAuth.getUid ()).child ("Customer");
        sellerRef.updateChildren (customer)
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


    private void checkUser () {
        FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user == null) {

            startActivity (new Intent (getApplicationContext (), LoginActivity.class));
            finish ();
        }
        else{
            try {
                loadMyInfo ();
            }catch (Exception e){
                showToastError ("Load Data : " + e.getMessage ());
            }
        }

    }

    private void loadMyInfo () {
        // Load user information and set them to views
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.orderByChild ("person_Id").equalTo (firebaseAuth.getUid ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren ()){
                            String accountType = String.valueOf (ds.child ("accountType").getValue ());
                            String email = String.valueOf (ds.child ("email").getValue ());
                            String firstname = String.valueOf (ds.child ("first_name").getValue ());
                            String lastname = String.valueOf (ds.child ("last_name").getValue ());
                            String phone = String.valueOf (ds.child ("phone").getValue ());
                            String profileImage = String.valueOf (ds.child ("profileImage").getValue ());

                            String person_Id = String.valueOf (ds.child ("person_Id").getValue ());

                            firstNameEt.setText (firstname);
                            lastNameEt.setText (lastname);
                            phoneEt.setText (phone);

                            try {
                                Picasso.get ().load (profileImage).placeholder (R.drawable.ic_store_gray).into(profileIv);
                            }catch (Exception e){
                                profileIv.setImageResource (R.drawable.ic_person_gray);
                            }

                        }
                        loadAddress();
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }



    public void detectLocation(){

        showToastSuccess ("Please Wait !!!");
        locationManager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, 0, 0, this);

    }
    private  boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission ( this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions (this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged (Location location) {
        //Location detected
        latitude = location.getLatitude ();
        longitude = location.getLongitude ();

        findAddress ();
    }
    private void findAddress(){
        //find address, country, state,city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder (this, Locale.getDefault ());
        try{
            addresses = geocoder.getFromLocation (latitude,longitude, 1);
            //Get Complete Address
            String address = addresses.get(0).getAddressLine (0);
            String city = addresses.get(0).getLocality ();
            String state = addresses.get (0).getAdminArea ();
            String country = addresses.get (0).getCountryName ();

            //Set Address
            countryEt.setText (country);
            cityEt.setText (state);
            StateEt.setText (city);
            addressEt.setText (address);
        }catch (Exception e){
            showToastError ("Find Address : " + e.getMessage ());
        }
    }

    @Override
    public void onStatusChanged (String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled (String provider) {

    }

    @Override
    public void onProviderDisabled (String provider) {
        //GPS Location disabled
        showToastWarning ("Please Turn On Location GPS ...");
    }

    private void updateAddress () {

        HashMap<String, Object> address = new HashMap<> ();
        address.put("address_Id", String.valueOf (firebaseAuth.getUid ()));
        address.put("country", String.valueOf (country));
        address.put("state", String.valueOf (state));
        address.put("city", String.valueOf (city));
        address.put("address", String.valueOf (address));
        address.put("latitude", String.valueOf (latitude));
        address.put("longitude", String.valueOf (longitude));
        DatabaseReference addressRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (firebaseAuth.getUid ()).child ("Address");
        addressRef.updateChildren (address)
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

    private void loadAddress () {
        // Load user information and set them to views
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child ("Address")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        String address = String.valueOf (dataSnapshot.child ("address").getValue ());
                        String city = String.valueOf (dataSnapshot.child ("city").getValue ());
                        String state = String.valueOf (dataSnapshot.child ("state").getValue ());
                        String country = String.valueOf (dataSnapshot.child ("country").getValue ());
                        String Alatitude =String.valueOf (dataSnapshot.child ("latitude").getValue ());
                        String Alongitude = String.valueOf (dataSnapshot.child ("longitude").getValue ());

                        if ((Alatitude.equals ("")||Alongitude.equals ("")))
                        {
                            latitude = 0.0;
                            longitude=0.0;
                        }
                        else{
                            latitude =Double.parseDouble (Alatitude);
                            longitude =Double.parseDouble (Alongitude);
                        }

                        countryEt.setText (country);
                        cityEt.setText (city);
                        StateEt.setText (state);
                        addressEt.setText (address);



                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }
    public void pickImage(){
        profileIv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //pick image
                showImagePicDialog ();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission allowed
                        detectLocation ();
                    }
                    else{
                        //permission denied
                        showToastWarning ("Location Permission is necessary.....");
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE: {
                //Picking from camera first check if camera and storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera ();
                    } else {
                        //Permission denied
                        showToastWarning ("onRequestPermissionsResult 2 : Please Enable Camera & Storage Permission");
                    }

                }
            }
            break;
            case Storage_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    //Picking from gallery if permissions are granted
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permission enabled
                        pickFromGallery ();
                    } else {
                        //Permission denied
                        showToastWarning ("onRequestPermissionsResult 3 : Please Enable Storage Permission");
                    }

                }
            }
            break;

        }
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
    }



    private void showImagePicDialog () {
        //Show dialog Containing options camera and Gallery to pick the image
        //Array containing Edit options
        String options[] = {"Camera", "Gallery"};
        //Set items to dialog
        AlertDialog.Builder builder = new AlertDialog.Builder (ProfileEditAdminActivity.this);
        //set Title
        builder.setTitle ("Pick Image From ..");
        //Set items to dialog
        builder.setItems (options, new DialogInterface.OnClickListener () {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick (DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //Camera
                    if (!checkCameraPermission ()){
                        requestCameraPermission ();
                    }else {
                        pickFromCamera ();
                    }
                } else {
                    //Gallery
                    if (!checkStoragePermission ()){
                        requestStoragePermission ();
                    }else{
                        pickFromGallery ();
                    }
                }
            }
        });
        //Create and show dialog
        builder.create ().show ();

    }
    private void pickFromCamera(){
        //Intent of picking image from device camera
        ContentValues values = new ContentValues ();
        values.put (MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put (MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        image_uri = ProfileEditAdminActivity.this.getContentResolver ().insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra (MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult (cameraIntent,IMAGE_PICK_CAMERA_REQUEST_CODE);
    }
    private void pickFromGallery () {
        //Pick from gallery
        Intent galleryIntent = new Intent (Intent.ACTION_PICK);
        galleryIntent.setType ("image/*");
        startActivityForResult (galleryIntent, IMAGE_PICK_REQUEST_CODE);

    }


    @Override
    public void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        // this method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_REQUEST_CODE) {
                //image is picked from Gallery, get uri of image
                image_uri = data.getData ();
                profileIv.setImageURI (image_uri);
                // uploadProfileCoverPhoto (image_uri);


            } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                //image is picked from Camera
                //  uploadProfileCoverPhoto (image_uri);
                //get picked image
                image_uri = data.getData ();
                profileIv.setImageURI (image_uri);
            }
        }
        super.onActivityResult (requestCode, resultCode, data);
    }

    private boolean checkStoragePermission () {
        /**
         * Check if storage permission is enabled or not
         * return true if enabled
         * return false if not enabled
         */
        boolean result = ContextCompat.checkSelfPermission (ProfileEditAdminActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission () {
        //request runtime storage permission
        requestPermissions (storagePermissions, Storage_REQUEST_CODE);
    }

    private boolean checkCameraPermission () {
        /**
         * Check if storage permission is enabled or not
         * return true if enabled
         * return false if not enabled
         */
        boolean result = ContextCompat.checkSelfPermission (ProfileEditAdminActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission (ProfileEditAdminActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission () {
        //request runtime storage permission
        requestPermissions (cameraPermission, CAMERA_REQUEST_CODE);
    }



    public void init(){
        backBtn = findViewById (R.id.eubackBtn);
        gpsBtn = findViewById (R.id.eugpsBtn);
        profileIv = findViewById (R.id.uprofileIv);
        firstNameEt = findViewById (R.id.firstNameEt);
        lastNameEt=findViewById (R.id.lastNameET);
        countryEt = findViewById (R.id.ucountryEt);
        cityEt = findViewById (R.id.ucityEt);
        StateEt = findViewById (R.id.ustatEt);
        phoneEt = findViewById (R.id.uphoneEt);
        addressEt = findViewById (R.id.uaddressEt);
        Update = findViewById (R.id.uUpdateBtn);
    }

    public void showToastSuccess(String s){
        Toasty.success (ProfileEditAdminActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (ProfileEditAdminActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (ProfileEditAdminActivity.this,s,Toasty.LENGTH_LONG).show ();
    }

}
