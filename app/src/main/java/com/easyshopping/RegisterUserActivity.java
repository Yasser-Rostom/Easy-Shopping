package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import es.dmoral.toasty.Toasty;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    //views
    private EditText firstNameEt, lastNameET, shopNameEt, sphoneEt, deliveryFeeEt,
            saddressEt, scountryEt, scityEt, sstatEt, sEmailEt, spasswordEt, scPasswordEt;
    private TextInputLayout firstNameTILL,lastNameTILL,shopNameTIL,phoneTIL,deliveryFeeTIL,emailTIL,passwordTIL,cpasswordTIL;
    private ImageButton sbackBtn, sgpsBtn;
    private ImageView sprofileIv;
    private SwitchCompat userSellerSwitch;
    private Button RegisterBtn;
    private TextView birthdayET;

    private TextView RegisterTV;

    //Variables for location
    private double latitude = 0.0, longitude = 0.0;
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

    //Firebase
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String firstName, lastName, birthDate, shopName, phoneNum, deliveryFee, country, state, city, address, email,
            password, confirmPassword;
    private String typeofAccount = "seller" ;
    private String Bdate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_register_user);
        init ();

        //initialization permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        //Init arrays of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //initialization firebase auth
        firebaseAuth = FirebaseAuth.getInstance ();
        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);

        sbackBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
        sgpsBtn.setOnClickListener (new View.OnClickListener () {
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

        userSellerSwitch.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                if (userSellerSwitch.isChecked ()) {
                    deliveryFeeTIL.setVisibility (View.VISIBLE);
                    shopNameTIL.setVisibility (View.VISIBLE);
                    userSellerSwitch.setText ("Seller Account");
                    RegisterTV.setText ("Register Seller");


                } else {
                    deliveryFeeTIL.setVisibility (View.GONE);
                    shopNameTIL.setVisibility (View.GONE);
                    userSellerSwitch.setText ("User Account");
                    RegisterTV.setText ("Register Customer");

                }
            }
        });
        //pick image
        pickImage ();

        birthdayET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterUserActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable (Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Bdate = month + "/" + day + "/" + year;
                birthdayET.setText(Bdate);
            }
        };

        RegisterBtn.setOnClickListener (new View.OnClickListener () {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick (View v) {
                validateInputedData ();
            }
        });
    }

    //--------------- Get information from views and validate it ----------------\\
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void validateInputedData(){
        //Input data
        firstName = firstNameEt.getText ().toString ().trim ();
        lastName = lastNameET.getText ().toString ().trim ();
        phoneNum = sphoneEt.getText ().toString ().trim ();
        country = scountryEt.getText ().toString ().trim ();
        state = sstatEt.getText ().toString ().trim ();
        city = scityEt.getText ().toString ().trim ();
        address = saddressEt.getText ().toString ().trim ();
        email = sEmailEt.getText ().toString ().trim ();
        password = spasswordEt.getText ().toString ().trim ();
        confirmPassword = scPasswordEt.getText ().toString ().trim ();
        deliveryFee = deliveryFeeEt.getText ().toString ().trim ();
        shopName = shopNameEt.getText ().toString ().trim ();
        birthDate = birthdayET.getText ().toString ().trim ();
        //Validate data
        try{
            if(TextUtils.isEmpty (firstName)){
                firstNameEt.setError ("Enter First Name ...");
                firstNameTILL.setFocusable (true);
                return;
            }
            if(TextUtils.isEmpty (lastName)){
                lastNameET.setError ("Enter last Name ...");
                lastNameET.setFocusable (true);
                return;
            }

            if (userSellerSwitch.isChecked ()){

                if(shopName.equals ("")){
                    showToastError ("Enter shop Name please ..... or convert your account to customer account");
                    shopNameEt.setError ("Enter shop Name please ..... or convert your account to customer account");
                    shopNameEt.setFocusable (true);
                    userSellerSwitch.setFocusable (true);
                    return;
                }
                if (deliveryFee.equals ("") ){

                    deliveryFeeEt.setError ("Enter delivery Fee please ..... or convert your account to customer account");
                    deliveryFeeEt.setFocusable (true);
                    userSellerSwitch.setFocusable (true);
                    return;
                }
            }

            if(TextUtils.isEmpty (phoneNum)){
                sphoneEt.setError ("Enter Phone Number ...");
                sphoneEt.setFocusable (true);
                return;
            }
            if (!phoneNum.matches ("[0-9]{10}")){
                sphoneEt.setError ("تنسيق خاطئ لرقم الموبايل");
                sphoneEt.setFocusable (true);
                return;
            }


            if(TextUtils.isEmpty (country)){
                showToastWarning ("Enter country ...");
                scountryEt.setFocusable (true);
                return;
            }
            if(TextUtils.isEmpty (state)){
                showToastWarning ("Enter state ...");
                sstatEt.setFocusable (true);
                return;
            }
            if(TextUtils.isEmpty (city)){
                showToastWarning ("Enter city ...");
                scityEt.setFocusable (true);
                return;
            }
            if(TextUtils.isEmpty (address)){
                showToastWarning ("Enter address ...");
                saddressEt.setFocusable (true);
                return;
            }
            if(TextUtils.isEmpty (email)){

                sEmailEt.setError ("Enter email ...");
                sEmailEt.setFocusable (true);
                return;
            }
            if(latitude==0.0 || longitude ==0.0){
                showToastWarning ("Please click GPS button to detect you location...");
                sgpsBtn.setFocusable (true);
                sgpsBtn.setTooltipText ("GPS tool");
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher (email).matches ()){

                sEmailEt.setError ("Invalid email pattern ...");
                sEmailEt.setFocusable (true);
                return;
            }
            if(TextUtils.isEmpty (password) || TextUtils.isEmpty (confirmPassword)){
                spasswordEt.setError ("Enter password and confirming it ...");
                spasswordEt.setFocusable (true);
                return;
            }

            if (!PASSWORD_PATTERN.matcher(password).matches()){

                spasswordEt.setError ("Password must be at least 6 characters long and must contain small , capital and special characters..");
                spasswordEt.setFocusable (true);
                return;

            }
            if (!password.equals (confirmPassword)){
                scPasswordEt.setError ("Password and confirm password must be equals ...!!!");
                scPasswordEt.setFocusable (true);
                spasswordEt.setFocusable (true);
                return;
            }

            createAccount();
        }catch (Exception e){
            showToastError ("Exmine all fields : " + e.getMessage ());
        }

    }

       //------- Location information ---------\\
    public void detectLocation () {

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
            scountryEt.setText (country);
            scityEt.setText (state);
            sstatEt.setText (city);
            saddressEt.setText (address);
        }catch (Exception e){
            showToastError ("Find Address : " + e.getMessage ());
        }
    }
    //------ End location Information --------\\

    //============ Pick image from storage or camera--------\\
    public void pickImage(){
        sprofileIv.setOnClickListener (new View.OnClickListener () {
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
        AlertDialog.Builder builder = new AlertDialog.Builder (RegisterUserActivity.this);
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
        image_uri = RegisterUserActivity.this.getContentResolver ().insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
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
                sprofileIv.setImageURI (image_uri);
                // uploadProfileCoverPhoto (image_uri);


            } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                //image is picked from Camera
                sprofileIv.setImageURI (image_uri);
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
        boolean result = ContextCompat.checkSelfPermission (RegisterUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        boolean result = ContextCompat.checkSelfPermission (RegisterUserActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission (RegisterUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission () {
        //request runtime storage permission
        requestPermissions (cameraPermission, CAMERA_REQUEST_CODE);
    }

    //-------------- End Picking Image ------------\\

    //------------------------ Creating Account ----------------------\\
    private void createAccount () {
        progressDialog.setMessage ("Creating Account...");
        progressDialog.show ();

        //Create account
        firebaseAuth.createUserWithEmailAndPassword (email, password)
                .addOnSuccessListener (new OnSuccessListener<AuthResult> () {
                    @Override
                    public void onSuccess (AuthResult authResult) {
                        //Account created
                        saverFirebaseData();

                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        //Failed creating account
                        progressDialog.dismiss ();
                        showToastError ("Fialed Creating Account : " + e.getMessage ());
                    }
                });
    }

    private boolean selleraccount = false;
    private String timestamp;
    private void saverFirebaseData () {
        progressDialog.setMessage ("Saving Account Info...");
        timestamp = String.valueOf (System.currentTimeMillis ());

        if (image_uri == null) {
            //save info without image

            //setup data to save
            HashMap<String, Object> person = new HashMap<> ();
            person.put ("person_Id", String.valueOf (firebaseAuth.getUid ()));
            person.put ("first_name", String.valueOf (firstName));
            person.put ("last_name", String.valueOf (lastName));
            person.put ("birthdate", String.valueOf (birthDate));
            person.put ("phone", String.valueOf (phoneNum));
            person.put ("email", String.valueOf (email));
            person.put ("timestamp", String.valueOf (timestamp));
            person.put ("status", "active");
            person.put ("onlineStatus","online");
            person.put ("typingTo","noOne");
            selleraccount = userSellerSwitch.isChecked ();
            if (selleraccount) {
                typeofAccount = "seller";
            } else {
                typeofAccount = "customer";

            }
            person.put ("accountType", typeofAccount);
            person.put ("profileImage", "");
            //Save to database
            DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
            ref.child (firebaseAuth.getUid ()).setValue (person)
                    .addOnSuccessListener (new OnSuccessListener<Void> () {
                        @Override
                        public void onSuccess (Void aVoid) {
                            //create Address Entity
                            createAddress ();
                            //Create contact Entity
                            createContact ();

                            if (!typeofAccount.equals ("customer")) {
                                createSeller ();
                                //Our database updated
                                progressDialog.dismiss ();
                                startActivity (new Intent (RegisterUserActivity.this, MainSellerActivity.class));
                                finish ();

                            } else {

                                createCustomer ();
                                //Our database updated
                                progressDialog.dismiss ();
                                startActivity (new Intent (RegisterUserActivity.this, MainUserActivity.class));
                                finish ();
                            }


                        }
                    })
                    .addOnFailureListener (new OnFailureListener () {
                        @Override
                        public void onFailure (@NonNull Exception e) {
                            //Failed updating db
                            progressDialog.dismiss ();
                            startActivity (new Intent (RegisterUserActivity.this, RegisterUserActivity.class));
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
                                HashMap<String, Object> hashMap = new HashMap<> ();
                                hashMap.put ("person_Id", String.valueOf (firebaseAuth.getUid ()));
                                hashMap.put ("first_name", String.valueOf (firstName));
                                hashMap.put ("last_name", String.valueOf (lastName));
                                hashMap.put ("birthdate", String.valueOf (birthDate));
                                hashMap.put ("phone", String.valueOf (phoneNum));
                                hashMap.put ("email", String.valueOf (email));
                                hashMap.put ("timestamp", String.valueOf (timestamp));
                                hashMap.put ("status", "active");
                                hashMap.put ("onlineStatus","online");
                                hashMap.put ("typingTo","noOne");
                                selleraccount = userSellerSwitch.isChecked ();
                                if (selleraccount) {
                                    typeofAccount = "seller";
                                } else {
                                    typeofAccount = "customer";

                                }
                                hashMap.put ("accountType", typeofAccount);
                                hashMap.put ("profileImage", String.valueOf (downloadImageUri));

                                //Save to database
                                //Save to database
                                DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
                                ref.child (firebaseAuth.getUid ()).setValue (hashMap)
                                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                                            @Override
                                            public void onSuccess (Void aVoid) {
                                                //create Address Entity
                                                createAddress ();
                                                //Create contact Entity
                                                createContact ();

                                                if (!typeofAccount.equals ("customer")) {
                                                    createSeller ();
                                                    //Our database updated
                                                    progressDialog.dismiss ();
                                                    startActivity (new Intent (RegisterUserActivity.this, MainSellerActivity.class));
                                                    finish ();

                                                } else {

                                                    createCustomer ();
                                                    //Our database updated
                                                    progressDialog.dismiss ();
                                                    startActivity (new Intent (RegisterUserActivity.this, MainUserActivity.class));
                                                    finish ();
                                                }


                                            }
                                        })
                                        .addOnFailureListener (new OnFailureListener () {
                                            @Override
                                            public void onFailure (@NonNull Exception e) {
                                                //Failed updating db
                                                progressDialog.dismiss ();
                                                startActivity (new Intent (RegisterUserActivity.this, RegisterUserActivity.class));
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

    private void createAddress () {

        HashMap<String, Object> address = new HashMap<> ();
        address.put("address_Id", String.valueOf (firebaseAuth.getUid ()));
        address.put("country", String.valueOf (country));
        address.put("state", String.valueOf (state));
        address.put("city", String.valueOf (city));
        address.put("address", String.valueOf (address));
        address.put("latitude", String.valueOf (latitude));
        address.put("longitude", String.valueOf (longitude));
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
    private void createSeller () {

        HashMap<String, Object> seller = new HashMap<> ();
        seller.put("seller_Id", String.valueOf (firebaseAuth.getUid ()));
        seller.put("person_Id", String.valueOf (firebaseAuth.getUid ()));
        seller.put("address_Id",String.valueOf (firebaseAuth.getUid ()));
        seller.put("contact_Id", String.valueOf (firebaseAuth.getUid ()));
        seller.put("shop_name", String.valueOf (shopName));
        seller.put("deliveryFee", String.valueOf (deliveryFee));
        seller.put("timestamp", timestamp);
        seller.put("card_Id", "");
        seller.put("online", "true");
        seller.put("shop_open", "true");
        DatabaseReference sellerRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (firebaseAuth.getUid ()).child ("Seller");
        sellerRef.setValue (seller)
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
    //------------------- End Creating Account ----------------------\\

    public void init(){
        firstNameEt=findViewById (R.id.firstNameEt);
        lastNameET=findViewById (R.id.lastNameET);
        birthdayET=findViewById (R.id.birthdayET);
        shopNameEt=findViewById (R.id.shopNameEt);
        sphoneEt=findViewById (R.id.sphoneEt);
        deliveryFeeEt=findViewById (R.id.deliveryFeeEt);
        saddressEt=findViewById (R.id.saddressEt);
        scountryEt=findViewById (R.id.scountryEt);
        scityEt=findViewById (R.id.scityEt);
        sstatEt=findViewById (R.id.sstatEt);
        sEmailEt=findViewById (R.id.sEmailEt);
        spasswordEt=findViewById (R.id.spasswordEt);
        scPasswordEt=findViewById (R.id.scPasswordEt);
        sbackBtn=findViewById (R.id.sbackBtn);
        sgpsBtn=findViewById (R.id.sgpsBtn);
        sprofileIv=findViewById (R.id.sprofileIv);
        userSellerSwitch=findViewById (R.id.userSellerSwitch);
        RegisterBtn= findViewById (R.id.sregisterBtn);
        RegisterTV = findViewById (R.id.RegisterTV);
        firstNameTILL = findViewById (R.id.firstNameTILL);
        lastNameTILL = findViewById (R.id.lastNameTILL);
        shopNameTIL = findViewById (R.id.shopNameTIL);
        phoneTIL = findViewById (R.id.phoneTIL);
        deliveryFeeTIL = findViewById (R.id.deliveryFeeTIL);
        emailTIL = findViewById (R.id.emailTIL);
        passwordTIL = findViewById (R.id.passwordTIL);
        cpasswordTIL = findViewById (R.id.cpasswordTIL);
    }

    public void showToastSuccess(String s){
        Toasty.success (RegisterUserActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (RegisterUserActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (RegisterUserActivity.this,s,Toasty.LENGTH_LONG).show ();
    }
}
