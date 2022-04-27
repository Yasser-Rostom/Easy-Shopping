package com.easyshopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import es.dmoral.toasty.Toasty;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {

    private String groupId, myGroupRole="";
    FirebaseAuth firebaseAuth;
    //Action Bar
    private ActionBar actionBar;
    private ProgressDialog progressDialog;

    //Views
    private ImageView groupIconIv;
    private EditText groupTitleEt, groupDescriptionEt;
    private FloatingActionButton updateGroupInfoBtn;
    private ImageButton backBtn;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int Storage_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String cameraPermission[];
    String storagePermissions[];

    Uri image_uri;
    String profileOrCoverPhoto;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_group_edit);
        init ();


        groupId = getIntent ().getStringExtra ("groupId");

        firebaseAuth= FirebaseAuth.getInstance ();

        //Init arrays of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog (this);
        progressDialog.setTitle ("Please wait");
        progressDialog.setCanceledOnTouchOutside (false);

        chechUser();
        //pick image
        pickImage ();
        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                onBackPressed ();
            }
        });
        loadGroupInfo();

        updateGroupInfoBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                startUpdatingGroup();
            }
        });


    }

    private void startUpdatingGroup () {
        progressDialog.setMessage ("Updating Group Info");
        progressDialog.show ();


        //input title, description
        final String groupTitle = groupTitleEt.getText ().toString ().trim ();
        final String groupDescription = groupDescriptionEt.getText ().toString ().trim ();

        if (TextUtils.isEmpty (groupTitle)){
            Toasty.warning (this, "Please Enter group title",Toasty.LENGTH_LONG).show ();
            return;
        }
        progressDialog.show ();
        final String g_timeStamp = ""+ System.currentTimeMillis ();
        if (image_uri == null){
           HashMap<String,Object> hashMap = new HashMap<> ();
           hashMap.put ("groupTitle", groupTitle);
           hashMap.put ("groupDescription", groupDescription);
            hashMap.put ("groupIcon", "");
            DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
            ref.child (groupId).updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                       //updated
                        progressDialog.dismiss ();
                        Toasty.success (GroupEditActivity.this, "Group Info updated successfully...",Toasty.LENGTH_LONG).show ();
                    }
                })
                    .addOnFailureListener (new OnFailureListener () {
                        @Override
                        public void onFailure (@NonNull Exception e) {
                            progressDialog.dismiss ();
                            Toasty.success (GroupEditActivity.this,  "" + e.getMessage (),Toasty.LENGTH_LONG).show ();
                        }
                    });
        }
        else {

            String fileNameAndPath ="Group_Images/"+"images"+g_timeStamp;
            StorageReference storageReference = FirebaseStorage.getInstance ().getReference (fileNameAndPath);
            storageReference.putFile (image_uri)
                    .addOnSuccessListener (new OnSuccessListener<UploadTask.TaskSnapshot> () {
                        @Override
                        public void onSuccess (UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> p_uriTask = taskSnapshot.getStorage ().getDownloadUrl ();
                            while (!p_uriTask.isSuccessful ());
                            Uri p_downloadUri = p_uriTask.getResult ();
                            if(p_uriTask.isSuccessful ()){
                                HashMap<String,Object> hashMap = new HashMap<> ();
                                hashMap.put ("groupTitle", groupTitle);
                                hashMap.put ("groupDescription", groupDescription);
                                hashMap.put ("groupIcon", p_downloadUri+"");
                                DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
                                ref.child (groupId).updateChildren (hashMap)
                                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                                            @Override
                                            public void onSuccess (Void aVoid) {
                                                //updated
                                                progressDialog.dismiss ();
                                                Toasty.success (GroupEditActivity.this, "Group Info updated successfully...",Toasty.LENGTH_LONG).show ();
                                            }
                                        })
                                        .addOnFailureListener (new OnFailureListener () {
                                            @Override
                                            public void onFailure (@NonNull Exception e) {
                                                progressDialog.dismiss ();
                                                Toasty.success (GroupEditActivity.this,  "" + e.getMessage (),Toasty.LENGTH_LONG).show ();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener (new OnFailureListener () {
                        @Override
                        public void onFailure (@NonNull Exception e) {

                        }
                    });

        }
    }


    private void loadGroupInfo () {
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

                  //set group info

                    groupDescriptionEt.setText (groupDescription);
                    groupTitleEt.setText (groupTitle);

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

    public void pickImage(){
        groupIconIv.setOnClickListener (new View.OnClickListener () {
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
                        //showToastWarning ("onRequestPermissionsResult 2 : Please Enable Camera & Storage Permission");
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
                        // showToastWarning ("onRequestPermissionsResult 3 : Please Enable Storage Permission");
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
        AlertDialog.Builder builder = new AlertDialog.Builder (GroupEditActivity.this);
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
        values.put (MediaStore.Images.Media.TITLE,"Group Image Icon Title");
        values.put (MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");
        //put image uri
        image_uri = GroupEditActivity.this.getContentResolver ().insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
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
                groupIconIv.setImageURI (image_uri);
                // uploadProfileCoverPhoto (image_uri);


            } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                //image is picked from Camera
                groupIconIv.setImageURI (image_uri);
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
        boolean result = ContextCompat.checkSelfPermission (GroupEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        boolean result = ContextCompat.checkSelfPermission (GroupEditActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission (GroupEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission () {
        //request runtime storage permission
        requestPermissions (cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void chechUser () {

       /* FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user != null){
            actionBar.setSubtitle (user.getEmail ());
        }*/
    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed ();
        return super.onSupportNavigateUp ();
    }

    public void init(){
        groupIconIv = findViewById (R.id.groupIconIv);
        groupTitleEt = findViewById (R.id.groupTitleEt);
        groupDescriptionEt = findViewById (R.id.groupDescriptionEt);
        updateGroupInfoBtn = findViewById (R.id.createGroupBtn);
        backBtn= findViewById (R.id.backBtn);
    }
}
