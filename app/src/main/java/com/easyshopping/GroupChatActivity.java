package com.easyshopping;

import Adapters.AdapterGroupCaht;
import Models.ModelGroupChat;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    //Group Id
    private String groupId;

    //views from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView groupTitleTv;
    ImageButton send , attachBtn;
    EditText messageEt;

    //Firebase Auth
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    private ArrayList<ModelGroupChat> groupChatArrayList;
    private AdapterGroupCaht adapterGroupCaht;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int Storage_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    //Arrays of permissions to be requested
    String cameraPermission[];
    String storagePermissions[];
    Uri image_uri;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_group_chat);
        init ();
        setSupportActionBar (toolbar);

        //Init arrays of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //Obtain group Id sending from  AdapterChatList
        Intent intent = getIntent ();
        groupId = intent.getStringExtra ("groupId");

        groupChatArrayList = new ArrayList<> ();

        firebaseAuth = FirebaseAuth.getInstance ();
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();

        send.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //input Data
                String message = messageEt.getText ().toString ().trim ();
                //validate
                if (TextUtils.isEmpty (message)){
                    Toasty.warning (GroupChatActivity.this, "Can't send empty message....",Toasty.LENGTH_LONG).show ();

                }
                else {
                    //send message
                    sendMessage(message);
                }
            }
        });

        attachBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Copy the code of pick image and handle permissions (storage/camera) from add post
                //show pick image dialog
                showImagePickDialog ();
            }
        });


    }

    private String myGroupRole = "";
    private void loadMyGroupRole () {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants")
                .orderByChild ("uid").equalTo (firebaseAuth.getUid ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds:dataSnapshot.getChildren ()){

                        myGroupRole = ""+ds.child ("role").getValue ();
                        //refresh main menu
                        invalidateOptionsMenu ();
                    }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void loadGroupMessages () {
        //get Sender info from  uid in model
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Messages")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        groupChatArrayList.clear ();
                        for (DataSnapshot ds : dataSnapshot.getChildren ()){
                            ModelGroupChat model = ds.getValue (ModelGroupChat.class);
                            groupChatArrayList.add (model);
                        }
                            adapterGroupCaht = new AdapterGroupCaht (GroupChatActivity.this, groupChatArrayList);
                        //set to recyclerView
                        recyclerView.setAdapter (adapterGroupCaht);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });



    }

    //---------------- start send message ----------------------\\
    private void sendMessage (String message) {
        //timestamp
        String timestamp = "" + System.currentTimeMillis ();

        //setup message data
        HashMap<String, Object> hashMap = new HashMap<> ();
        hashMap.put ("sender", "" + firebaseAuth.getUid ());
        hashMap.put ("message", message);
        hashMap.put ("timestamp", "" + timestamp);
        hashMap.put ("type", "text" );

        //addd in db
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Messages").child (timestamp)
                .setValue (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                    //message sent
                        messageEt.setText ("");
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        //message sending failed
                        Toasty.warning (GroupChatActivity.this, "" + e.getMessage (),Toasty.LENGTH_LONG).show ();

                    }
                });







    }
//---------------- End send message ----------------------\\
    private void loadGroupInfo () {

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.orderByChild ("groupId").equalTo (groupId)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren ()){
                            String groupTitle = ""+ds.child ("groupTitle").getValue ();
                            String groupDescription = ""+ds.child ("groupDescription").getValue ();
                            String grouIcon = ""+ds.child ("grouIcon").getValue ();
                            String timestamp = ""+ds.child ("timestamp").getValue ();
                            String createdBy = ""+ds.child ("createdBy").getValue ();
                            groupTitleTv.setText (groupTitle);
                            try{
                                Picasso.get ().load (grouIcon).placeholder (R.drawable.ic_group_primary).into (profileIv);
                            }catch (Exception e){
                                profileIv.setImageResource (R.drawable.ic_group_primary);
                            }


                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater ().inflate (R.menu.menu_main, menu);
        menu.findItem (R.id.action_create_group).setVisible (false);
        menu.findItem (R.id.action_Search).setVisible (false);
        if (myGroupRole.equals ("creator") || myGroupRole.equals ("admin")){
            //show add member btn
            menu.findItem (R.id.ic_add_participant).setVisible (true);
        }
        else {
            menu.findItem (R.id.ic_add_participant).setVisible (false);
        }

        return super.onCreateOptionsMenu (menu);
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {

        int id = item.getItemId ();
        if (id == R.id.ic_add_participant){
            Intent intent = new Intent (this, GroupParticipantsActivity.class);
            intent.putExtra ("groupId", groupId);
            startActivity(intent);
        }
        else if (id == R.id.action_groupInfo){
            Intent intent = new Intent (this, GroupInfoActivity.class);
            intent.putExtra ("groupId", groupId);
            intent.putExtra ("groupRole", myGroupRole);
            startActivity(intent);
        }


        return super.onOptionsItemSelected (item);
    }

    public void init(){

        profileIv = (ImageView) findViewById (R.id.profileIv);
        groupTitleTv = (TextView) findViewById (R.id.groupTitleTv);
        send = (ImageButton) findViewById (R.id.sendBtn);
        messageEt= (EditText) findViewById (R.id.messageET);
        attachBtn = (ImageButton) findViewById (R.id.attachBtn);
        recyclerView = findViewById (R.id.chat_recyclerView);
        toolbar = findViewById (R.id.toolbar);

    }


    //For image picking
    private void showImagePickDialog () {
        String options[] = {"Camera", "Gallery"};
        //Set items to dialog
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        //set Title
        builder.setTitle ("Choose Image From ..");
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

    private boolean checkStoragePermission () {
        /**
         * Check if storage permission is enabled or not
         * return true if enabled
         * return false if not enabled
         */
        boolean result = ContextCompat.checkSelfPermission (this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        boolean result = ContextCompat.checkSelfPermission (this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission (this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission () {
        //request runtime storage permission
        requestPermissions (cameraPermission, CAMERA_REQUEST_CODE);
    }
    private void pickFromCamera(){
        //Intent of picking image from device camera
        ContentValues values = new ContentValues ();
        values.put (MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put (MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        image_uri = getContentResolver ().insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
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
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        /**
         * This method called when user press Allow or Deny from permission request dialog
         * here we will handle permission cases (allowed & denied)
         */
        switch (requestCode) {
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
                        Toasty.warning (this, "Please Enable Camera & Storage Permission", Toasty.LENGTH_LONG).show ();
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
                        Toasty.warning (this, "Please Enable Storage Permission", Toasty.LENGTH_LONG).show ();
                    }

                }
            }
            break;
        }

    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        // this method will be called after picking image from camera or gallery
        if(resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_REQUEST_CODE){
                //image is picked from Gallery, get uri of image
                image_uri=data.getData ();
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace ();
                    Toasty.error (GroupChatActivity.this, "SendImageMessage 1 : "+ e.getMessage ());
                }

            }
            else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                //user this image uri to upload to firebase storage
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace ();
                    Toasty.error (GroupChatActivity.this, "SendImageMessage 2 : "+ e.getMessage ());
                }
            }
        }
        super.onActivityResult (requestCode, resultCode, data);
    }


    private void sendImageMessage (Uri image_uri) throws IOException {
       // notify= true;
        //progress dialog
        final ProgressDialog progressDialog = new ProgressDialog (this);
        progressDialog.setMessage ("Sending image . . .");

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);//disable dismiss if click out side progress dialog
        progressDialog.show ();




        final String timeStamp = String.valueOf (System.currentTimeMillis ());

        String fileNameAndPath = "GroupChatImage/" + "post_" + timeStamp;

        //Cahts node will be created that will contain all images sent via chat

        //get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap (this.getContentResolver (), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        bitmap.compress (Bitmap.CompressFormat.PNG,100,baos);
        byte[] data= baos.toByteArray ();
        StorageReference ref = FirebaseStorage.getInstance ().getReference ().child (fileNameAndPath);
        ref.putBytes (data)
                .addOnSuccessListener (new OnSuccessListener<UploadTask.TaskSnapshot> () {
                    @Override
                    public void onSuccess (UploadTask.TaskSnapshot taskSnapshot) {

                        //image uploaded
                        progressDialog.dismiss ();
                        //get uri of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage ().getDownloadUrl ();
                        while (!uriTask.isSuccessful ());
                        String downloadUri = uriTask.getResult ().toString ();
                        if (uriTask.isSuccessful ()){
                            //add image uri and other info to database
                            DatabaseReference dReference = FirebaseDatabase.getInstance ().getReference ("Groups");
                            //setup requied data
                            String timestamp = ""+ System.currentTimeMillis ();
                            HashMap<String, Object> hashMap = new HashMap<> ();
                            hashMap.put ("sender", "" + firebaseAuth.getUid ());
                            hashMap.put ("message", downloadUri);
                            hashMap.put ("timestamp", "" + timestamp);
                            hashMap.put ("type", "image" );
                            //put this data to firebase

                            dReference.child (groupId).child ("Messages").child (timeStamp).setValue (hashMap)
                                    .addOnSuccessListener (new OnSuccessListener<Void> () {
                                        @Override
                                        public void onSuccess (Void aVoid) {
                                        //message sent clear messageET
                                            messageEt.setText ("");
                                            progressDialog.dismiss ();
                                        }
                                    })
                                    .addOnFailureListener (new OnFailureListener () {
                                        @Override
                                        public void onFailure (@NonNull Exception e) {
                                            Toasty.error (GroupChatActivity.this, "Send Image : "+ e.getMessage (), Toasty.LENGTH_LONG).show ();
                                        }
                                    });
                            //send notification
                           /* DatabaseReference database = FirebaseDatabase.getInstance ().getReference ("Persons").child (myUid);
                            database.addValueEventListener (new ValueEventListener () {
                                @Override
                                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                                    ModelShops users = dataSnapshot.getValue (ModelShops.class);
                                    if (notify){
                                        String firstname = ""+users.getFirst_name ();
                                        String lastname = ""+ users.getLast_name ();
                                        sendNotification (HisUid, firstname+""+lastname,"Send you a photo");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled (@NonNull DatabaseError databaseError) {

                                }
                            });

                            //Part 30 start from here
                            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance ().getReference ("Chatlist")
                                    .child (myUid)
                                    .child (HisUid);
                            chatRef1.addValueEventListener (new ValueEventListener () {
                                @Override
                                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists ()){
                                        chatRef1.child ("id").setValue (HisUid);
                                    }
                                }

                                @Override
                                public void onCancelled (@NonNull DatabaseError databaseError) {

                                }
                            });
                            // part 30 then add this
                            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance ().getReference ("Chatlist")
                                    .child (HisUid)
                                    .child (myUid);
                            chatRef2.addValueEventListener (new ValueEventListener () {
                                @Override
                                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists ()){
                                        chatRef2.child ("id").setValue (myUid);
                                    }
                                }

                                @Override
                                public void onCancelled (@NonNull DatabaseError databaseError) {

                                }
                            });*/

                        }


                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        // Failed
                        progressDialog.dismiss ();

                    }
                });



    }

}
