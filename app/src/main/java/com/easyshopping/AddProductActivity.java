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
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity implements LocationListener {

    //Views
    private ImageButton backBtn;
    private ImageView productIconIv;
    private TextView category;
    private EditText titleEt, descriptionEt, quantityEt,priceEt, discountedPriceEt, discountedNoteEt;
    private SwitchCompat discountSwicth;
    private Button addProductBtn;


    //Variables for location
    private double latitude=0.0, longitude=0.0;
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

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_add_product);
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

        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
        productIconIv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                showImagePicDialog ();
            }
        });


        category.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                categoryDialog();
            }
        });

        addProductBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                /**
                 * Flow :
                 * 1) input Data
                 * 2)Validate Data
                 * 3)Add data to our database
                 */
                inputData();

            }
        });
        //hide discount fields if discountSwitch is unchecked

        discountedPriceEt.setVisibility (View.GONE);
        discountedNoteEt.setVisibility (View.GONE);
        discountSwicth.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                if (discountSwicth.isChecked ()) {
                    discountedPriceEt.setVisibility (View.VISIBLE);
                    discountedNoteEt.setVisibility (View.VISIBLE);
                }
                else {
                    discountedPriceEt.setVisibility (View.GONE);
                    discountedNoteEt.setVisibility (View.GONE);
                }
            }
        });
    }
    private String productTitle, productDescription, productCategory, productQuanatity, originalPrice, discountPrice, dicountNote;
    private boolean discountAvailable = false;

    private void inputData () {
        // 1- input data
        productTitle = titleEt.getText ().toString ().trim ();
        productDescription = descriptionEt.getText ().toString ().trim ();
        productCategory = category.getText ().toString ().trim ();
        productQuanatity = quantityEt.getText ().toString ().trim ();
        originalPrice = priceEt.getText ().toString ().trim ();
        discountPrice = discountedPriceEt.getText ().toString ().trim ();
        dicountNote = discountedNoteEt.getText ().toString ().trim ();
        discountAvailable = discountSwicth.isChecked ();

        // 2) validate data
        //Validate data
        try{
            if(TextUtils.isEmpty (productTitle)){
                showToastWarning ("Enter Product Title ...");
                return;
            }


            if(TextUtils.isEmpty (productDescription)){
                showToastWarning ("Enter Product Description ...");
                return;
            }

            if(TextUtils.isEmpty (productCategory)){
                showToastWarning ("Enter Product Category ...");
                return;
            }
            if(TextUtils.isEmpty (productQuanatity)){
                showToastWarning ("Enter Product Quanatity ...");
                return;
            }
            if(TextUtils.isEmpty (originalPrice)){
                showToastWarning ("Enter Product Price ...");
                return;
            }
            if(discountAvailable){
                if (TextUtils.isEmpty (discountPrice)) {
                    showToastWarning ("Enter discountPrice ...");
                    return;
                }
                if(TextUtils.isEmpty (dicountNote)){
                    showToastWarning ("Enter Discount Note ...");
                    return;
                }

            }
            else {
                //product is without discount
                discountPrice="0";
                dicountNote = "";
            }
            addProduct();


        }catch (Exception e){
            showToastError ("Exmine all fields : " + e.getMessage ());
        }
    }

    private void addProduct () {
        progressDialog.setMessage ("Adding New Product ...");
        progressDialog.show ();
        final String timestamp = String.valueOf (System.currentTimeMillis ());

        if (image_uri == null){
            //save info without image
            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<> ();
            hashMap.put("product_Id", String.valueOf (timestamp));
            hashMap.put("productTitle", String.valueOf (productTitle));
            hashMap.put("productCategory", String.valueOf (productCategory));
            hashMap.put("productDescription", String.valueOf (productDescription));
            hashMap.put("productQuantity", String.valueOf (productQuanatity));
            hashMap.put("productIcon", "");
            hashMap.put("originalPrice", String.valueOf (originalPrice));
            hashMap.put("discountPrice", String.valueOf (discountPrice));
            hashMap.put("discountNote", String.valueOf (dicountNote));
            hashMap.put("discountAvailable", String.valueOf (discountAvailable));
            hashMap.put ("timestamp", ""+timestamp);
            hashMap.put ("person_Id", ""+firebaseAuth.getUid ());

            //Save to database
            DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
            ref.child (firebaseAuth.getUid ()).child ("Products").child (timestamp).setValue (hashMap)
                    .addOnSuccessListener (new OnSuccessListener<Void> () {
                        @Override
                        public void onSuccess (Void aVoid) {
                            //Our database updated
                            progressDialog.dismiss ();
                            showToastSuccess ("Product Added successfully ...");
                        }
                    })
                    .addOnFailureListener (new OnFailureListener () {
                        @Override
                        public void onFailure (@NonNull Exception e) {
                            //Failed updating db
                            progressDialog.dismiss ();
                            showToastError ("Failed to add product : " + e.getMessage ());
                        }
                    });
        }

        else {
            //Save info with image

            //name and path of image
            String filePathAndName = "product_images/"+ ""+timestamp;
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance ().getReference (filePathAndName);
            storageReference.putFile (image_uri)
                    .addOnSuccessListener (new OnSuccessListener<UploadTask.TaskSnapshot> () {
                        @Override
                        public void onSuccess (UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage ().getDownloadUrl ();
                            while (!uriTask.isSuccessful ());
                            Uri downloadImageUri = uriTask.getResult ();
                            if (uriTask.isSuccessful ()){
                                //setup data to save
                                HashMap<String, Object> hashMap = new HashMap<> ();
                                hashMap.put("productId", String.valueOf (timestamp));
                                hashMap.put("productTitle", String.valueOf (productTitle));
                                hashMap.put("productCategory", String.valueOf (productCategory));
                                hashMap.put("productDescription", String.valueOf (productDescription));
                                hashMap.put("productQuantity", String.valueOf (productQuanatity));
                                hashMap.put("productIcon", ""+downloadImageUri);
                                hashMap.put("originalPrice", String.valueOf (originalPrice));
                                hashMap.put("discountPrice", String.valueOf (discountPrice));
                                hashMap.put("discountNote", String.valueOf (dicountNote));
                                hashMap.put("discountAvailable", String.valueOf (discountAvailable));
                                hashMap.put ("timestamp", ""+timestamp);
                                hashMap.put ("person_Id", ""+firebaseAuth.getUid ());

                                //Save to database
                                DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
                                ref.child (firebaseAuth.getUid ()).child ("Products").child (timestamp).setValue (hashMap)
                                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                                            @Override
                                            public void onSuccess (Void aVoid) {
                                                //Our database updated
                                                progressDialog.dismiss ();
                                                showToastSuccess ("Product Added successfully ...");
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener (new OnFailureListener () {
                                            @Override
                                            public void onFailure (@NonNull Exception e) {
                                                //Failed updating db
                                                progressDialog.dismiss ();
                                                showToastError ("Failed to add product with picture : " + e.getMessage ());
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

    private void clearData () {
        titleEt.setText("");
        descriptionEt.setText ("");
        category.setText ("");
        quantityEt.setText ("");
        priceEt.setText ("");
        discountedPriceEt.setText ("");
        discountedNoteEt.setText ("");
        productIconIv.setImageResource (R.drawable.ic_add_shopping_red);
        image_uri = null;

    }

    private void categoryDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setTitle ("Product Category")
                .setItems (Constants.ProductCategories, new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick (DialogInterface dialog, int which) {
                        String catgorylist = Constants.ProductCategories[which];
                        category.setText (catgorylist);
                    }
                }).show ();
    }


    public void detectLocation () {
        showToastSuccess ("Please Wait !!!");
        locationManager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
          /*  scountryEt.setText (country);
            scityEt.setText (state);
            sStateEt.setText (city);
            saddressEt.setText (address);*/
        }catch (Exception e){
            showToastError ("Find Address" + e.getMessage ());
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
                        Toasty.warning (AddProductActivity.this, "Please Enable Camera & Storage Permission", Toasty.LENGTH_LONG).show ();
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
                        showToastWarning ("Please Enable Storage Permission");
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
        AlertDialog.Builder builder = new AlertDialog.Builder (AddProductActivity.this);
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
        image_uri = AddProductActivity.this.getContentResolver ().insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
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
                productIconIv.setImageURI (image_uri);
                // uploadProfileCoverPhoto (image_uri);


            } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                //image is picked from Camera
                //  uploadProfileCoverPhoto (image_uri);
                //get picked image
                image_uri = data.getData ();
                productIconIv.setImageURI (image_uri);
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
        boolean result = ContextCompat.checkSelfPermission (AddProductActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        boolean result = ContextCompat.checkSelfPermission (AddProductActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission (AddProductActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission () {
        //request runtime storage permission
        requestPermissions (cameraPermission, CAMERA_REQUEST_CODE);
    }

    public void init(){
        backBtn = findViewById (R.id.AddbackBtn);
        productIconIv = findViewById (R.id.productIconIV);
        category = findViewById (R.id.categoryTV);
        titleEt = findViewById (R.id.titleEt);
        descriptionEt = findViewById (R.id.descriptionEt);
        quantityEt = findViewById (R.id.quantityET);
        priceEt = findViewById (R.id.priceEt);
        discountedPriceEt = findViewById (R.id.discountedPriceEt);
        discountedNoteEt = findViewById (R.id.discountedNoteEt);
        discountSwicth = findViewById (R.id.discountSwitch);
        addProductBtn = findViewById (R.id.addProductButton);

    }

    public void showToastSuccess (String s){
        Toasty.success (AddProductActivity.this, s, Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning (String s){
        Toasty.warning (AddProductActivity.this, s, Toasty.LENGTH_LONG).show ();
    }
    public void showToastError (String s){
        Toasty.error (AddProductActivity.this, s, Toasty.LENGTH_LONG).show ();
    }
}
