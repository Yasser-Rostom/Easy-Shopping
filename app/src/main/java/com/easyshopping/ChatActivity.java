package com.easyshopping;



import Adapters.AdapterChat;
import Models.ModelChat;
import Models.ModelShops;

import Notifications.Data;
import Notifications.Sender;
import Notifications.Token;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
//import com.Notifications.Data;
//import com.Notifications.Sender;
//import com.Notifications.Token;
import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    //views from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    ImageButton send , attachBtn;
    EditText messageEt;
    //Firebase Auth
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    String HisUid, myUid,hisImage;

    AdapterChat adapterChat;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;

    //volley request queue for notification
    private RequestQueue requestQueue;

    private boolean notify = false;


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

        setContentView (R.layout.activity_chat);
        init ();
        requestQueue = Volley.newRequestQueue (getApplicationContext ());
        recyclerView = this.findViewById (R.id.chat_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager (this);
        linearLayoutManager.setStackFromEnd (true);
        linearLayoutManager.setOrientation (LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize (true);
        recyclerView.setLayoutManager (linearLayoutManager);

        //Init arrays of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        /**
         * On clicking user from users lis we have passed that user's UID using intetent
         * so get that uid here to get the profile picture , name and start chat with the user
         * */
        Intent intent = getIntent ();
        HisUid = intent.getStringExtra ("person_Id");

        firebaseDatabase = FirebaseDatabase.getInstance ();
        DatabaseReference DBRef = firebaseDatabase.getReference ("Persons");
        //Init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance ();

        //Search Users to optain user with id (HisUid)
        Query userQuery = DBRef.orderByChild ("person_Id").equalTo (HisUid);
        //Get user picture and name
        userQuery.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                //Check until required info is recieved
                for (DataSnapshot ds : dataSnapshot.getChildren ()){
                    String fname = String.valueOf (ds.child ("first_name").getValue ());
                    String lname = String.valueOf (ds.child ("last_name").getValue ());
                    String name = fname + " " + lname;
                    String hisImage = String.valueOf (ds.child ("profileImage").getValue ());
                    String typing = String.valueOf (ds.child ("typingTo").getValue ());
                    String timestamp = "" + ds.child ("timestamp").getValue ();
                    if (typing.equals (myUid)){
                        userStatusTv.setText ("Typing...");
                    }
                    else {
                        //Get Value of onlineStatus
                        String onlineStatus = ""+ ds.child ("onlineStatus").getValue ();
                        if(onlineStatus.equals("online")){
                            userStatusTv.setText (onlineStatus);
                        }
                        else {
                            //convert to timestamp to proper time date
                            //Convert time stamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance (Locale.ENGLISH);
                           cal.setTimeInMillis (Long.parseLong (onlineStatus));
                           String dateTime = DateFormat.format ("dd/MM/yyyy hh:mm aa",cal).toString ();
                            userStatusTv.setText ("Last seen at: " + dateTime);
                            //add any time stamp to registed uses in firebase database manually

                        }
                    }

                    //set Data
                    nameTv.setText (name);

                    Glide.with(profileIv.getContext ())
                            .load(hisImage)
                            .into(profileIv);
                 /*  Picasso.get ().load (hisImage)
                            .placeholder (R.drawable.ic_chat_img)
                            .into (profileIv);*/

                } }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });
        //Set it's properties

        toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        toolbar.setTitle ("");


        send.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                try{
                    notify = true;
                    //Get message from Edit text
                    String message = messageEt.getText ().toString ().trim ();
                    //Check if the Edit text is empty
                    if (TextUtils.isEmpty (message)){
                        Toasty.warning (ChatActivity.this,"Can not send an empty message",Toasty.LENGTH_LONG).show();
                    }
                    else {
                        //Text not empty
                        sendMessage(message);
                    }
                    //Reset Edit text to null after message is send
                    messageEt.setText ("");
                }
                catch (Exception e){
                    Toasty.warning (ChatActivity.this," send... " + e.getMessage (),Toasty.LENGTH_LONG).show ();
                }
            }
        });
        Picasso.get ().load (R.drawable.ic_person_gray).into (profileIv);

        try{
            readMessages();
            seenMessage();
            checkMessageTEChanging();
        }
        catch (Exception e){
            Toasty.warning (ChatActivity.this," send&Read... " + e.getMessage (),Toasty.LENGTH_LONG).show ();
        }

        attachBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Copy the code of pick image and handle permissions (storage/camera) from add post
                //show pick image dialog
                showImagePickDialog ();
            }
        });

    }
    //Check edit text change listener
    private void checkMessageTEChanging () {
        messageEt.addTextChangedListener (new TextWatcher () {
            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {
                checkTypingStatus ("noOne");
            }

            @Override
            public void onTextChanged (CharSequence s, int start, int before, int count) {
                if (s.toString ().trim ().length ()>0) {
                    checkTypingStatus (HisUid);//uid of receiver
                }
                else {
                    checkTypingStatus ("noOne");
                }
            }

            @Override
            public void afterTextChanged (Editable s) {

            }
        });
    }

    private void seenMessage () {
        try {


            userRefForSeen = FirebaseDatabase.getInstance ().getReference ("Chats");
            seenListener= userRefForSeen.addValueEventListener (new ValueEventListener () {
                @Override
                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren ()){
                        ModelChat chat = ds.getValue (ModelChat.class);
                        if (chat.getReciever ().equals (myUid) && chat.getSender ().equals (HisUid)) {
                            HashMap<String, Object> hasSeenHashMap = new HashMap<> ();
                            hasSeenHashMap.put ("isSeen", true);
                            ds.getRef ().updateChildren (hasSeenHashMap);
                        }
                    }
                }

                @Override
                public void onCancelled (@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){
            Toasty.warning (ChatActivity.this,"seenMessage : " +e.getMessage (),Toasty.LENGTH_LONG).show();
        }
    }

    private void readMessages () {
        try {
            chatList = new ArrayList<> ();
            DatabaseReference dbRef = FirebaseDatabase.getInstance ().getReference ("Chats");
            dbRef.addValueEventListener (new ValueEventListener () {
                @Override
                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    chatList.clear();
                    for (DataSnapshot ds:dataSnapshot.getChildren ()){
                        ModelChat chat = ds.getValue (ModelChat.class);

                        if ((chat.getReciever ().equals (myUid) && chat.getSender ().equals (HisUid)) ||
                                (chat.getReciever ().equals (HisUid) && chat.getSender ().equals (myUid))) {
                            chatList.add (chat);
                            adapterChat = new AdapterChat (ChatActivity.this, chatList, hisImage);
                            adapterChat.notifyDataSetChanged ();
                            //set adapter to recyclerView
                            recyclerView.setAdapter (adapterChat);
                        }
                        //adapter

                    }
                }

                @Override
                public void onCancelled (@NonNull DatabaseError databaseError) {

                }
            });}
        catch (Exception e){
            Toasty.warning (ChatActivity.this,"readMessages : " + e.getMessage (),Toasty.LENGTH_LONG).show();
        }

    }

    private void sendMessage (final String message) {
        /** "Chats" node will be created that will contain all chats
         * whenever user sends message it will create new child in "chats" node
         * and that child will contain the following key values
         * sender : UID of sender
         * reciever : UID of receiver
         * message : the actual message
         */
        try{
            DatabaseReference dBRef = FirebaseDatabase.getInstance ().getReference ();
            String timestamp = String.valueOf (System.currentTimeMillis ());
            HashMap<String,Object> hashMap = new HashMap<> ();
            hashMap.put("chat_Id", timestamp);
            hashMap.put("sender", myUid);
            hashMap.put("reciever", HisUid);
            hashMap.put("message",message);
            hashMap.put("timestamp",timestamp);
            hashMap.put("isSeen",false);
            hashMap.put ("type","text");
            dBRef.child ("Chats").push ().setValue (hashMap);


            final  DatabaseReference dbRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (myUid);
            dbRef.addValueEventListener (new ValueEventListener () {
                @Override
                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    ModelShops users = dataSnapshot.getValue (ModelShops.class);
                    if (notify){
                        String firstname = ""+users.getFirst_name ();
                        String lastname = ""+ users.getLast_name ();
                       sendNotification(HisUid, firstname+" "+lastname,message);
                    }

                }

                @Override
                public void onCancelled (@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e){
            Toasty.warning (ChatActivity.this,"sendMessage : " + e.getMessage (),Toasty.LENGTH_LONG).show();
        }

        //Part 26 start from here
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
        // part 26 then add this
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
        });

    }


    private void sendImageMessage (Uri image_uri) throws IOException {
        notify= true;
        //progress dialog
        final ProgressDialog progressDialog = new ProgressDialog (this);
        progressDialog.setMessage ("Sending image . . .");

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);//disable dismiss if click out side progress dialog
        progressDialog.show ();




        final String timeStamp = String.valueOf (System.currentTimeMillis ());

        String fileNameAndPath = "ChatImage/" + "post_" + timeStamp;

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
                            DatabaseReference dReference = FirebaseDatabase.getInstance ().getReference ();
                            //setup requied data
                            HashMap<String, Object> hashMap = new HashMap<> ();
                            hashMap.put ("chat_Id", timeStamp);
                            hashMap.put ("sender", myUid);
                            hashMap.put ("reciever", HisUid);
                            hashMap.put ("message", downloadUri);
                            hashMap.put ("timestamp", timeStamp);
                            hashMap.put ("type", "image");
                            hashMap.put ("isSeen", false);
                            //put this data to firebase
                            dReference.child ("Chats").push ().setValue (hashMap);
                            //send notification
                            DatabaseReference database = FirebaseDatabase.getInstance ().getReference ("Persons").child (myUid);
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
                            });

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

   private void sendNotification (String hisUid, final String name, final String message) {
        try {
            DatabaseReference allToken = FirebaseDatabase.getInstance ().getReference ("Token");
            Query query = allToken.orderByKey ().equalTo (hisUid);
            query.addValueEventListener (new ValueEventListener () {
                @Override
                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren ()) {
                        Token token = ds.getValue (Token.class);
                        Data data = new Data (myUid, name + " : " + message, "New Message", HisUid, R.drawable.ic_person_gray);

                        Sender sender = new Sender (data, token.getToken ());
                        //fcm json object request
                        try {
                            JSONObject senderJsonBbj = new JSONObject (new Gson ().toJson (sender));
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest ("https://fcm.googleapis.com/fcm/send", senderJsonBbj,
                                    new Response.Listener<JSONObject> () {
                                        @Override
                                        public void onResponse (JSONObject response) {
                                            //response of the request
                                            Log.d("JSON_RESPONSE","onResponse: " + response.toString ());
                                        }
                                    }, new Response.ErrorListener () {
                                @Override
                                public void onErrorResponse (VolleyError error) {
                                    Log.d("JSON_RESPONSE","onResponse: " + error.toString ());
                                }
                            }){
                                @Override
                                public Map<String, String> getHeaders () throws AuthFailureError {
                                    //put params
                                    Map<String, String> headers = new HashMap<> ();
                                    headers.put("Content-Type","application/json");
                                    headers.put ("Authorization", "key=AAAAGJOMakY:APA91bGmsTHsFGTKlgGYpl9yg5Ja7huKDrBK7EeT27QHwzOQ0BH7qIcVd-DCSj1bIguL2spWwjp05lbYELRIBfV9deAhsbUwGqJz7JTzEQzn70ihA8-dotoeTSAibSZHczSSD31AjMzR");

                                    return headers;
                                }
                            };
                            //add this request to queue
                            requestQueue.add (jsonObjectRequest);

                        }catch (JSONException e){
                            e.printStackTrace ();
                        }

                    }
                }


                @Override
                public void onCancelled (@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            Toasty.warning (ChatActivity.this,"send Notification : " + e.getMessage (),Toasty.LENGTH_LONG).show();
        }
    }
    private void checkOnlineStatus (String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (myUid);
        HashMap<String, Object> hashmap = new HashMap<> ();
        hashmap.put ("onlineStatus", status);
        //update value of onlineStatus of current user
        dbRef.updateChildren (hashmap);
    }
    private void checkTypingStatus (String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance ().getReference ("Persons").child (myUid);
        HashMap<String, Object> hashmap = new HashMap<> ();
        hashmap.put ("typingTo", typing);
        //update value of onlineStatus of current user
        dbRef.updateChildren (hashmap);
    }

    @Override
    protected void onStart () {
        checkUserStatus ();
        //set online
        checkOnlineStatus ("online");
        super.onStart ();
    }

    @Override
    protected void onPause () {

        //Get Timestamp
        String timestamp = String.valueOf (System.currentTimeMillis ());

        //Set offline with last  seen time stamp
        checkOnlineStatus (timestamp);
        //Set typing status
        checkTypingStatus (timestamp);
        userRefForSeen.removeEventListener (seenListener);
        super.onPause ();
    }

    @Override
    protected void onResume () {
        checkOnlineStatus ("online");
        super.onResume ();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        try {
            getMenuInflater ().inflate (R.menu.menu_main,menu);
            //hide SearchView and Addpost as we don't need it here
            menu.findItem (R.id.action_Search).setVisible (false);
            menu.findItem (R.id.action_create_group).setVisible (false);
            menu.findItem (R.id.ic_add_participant).setVisible (false);
             } catch (Exception e){
            Toasty.warning (ChatActivity.this,"onCreateOptionsMenu : " + e.getMessage (),Toasty.LENGTH_LONG).show();
        }
        return super.onCreateOptionsMenu (menu);

    }
    //Handle clicks on options of option menu
    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        try{
            int id = item.getItemId ();
            if (id == R.id.action_logout){
                firebaseAuth.signOut ();
                checkUserStatus ();
            }

        } catch (Exception e){
            Toasty.warning (ChatActivity.this,"onOptionsItemSelected : " + e.getMessage (),Toasty.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected (item);
    }

    private void checkUserStatus(){
        try{
            //Get current user
            FirebaseUser user = firebaseAuth.getCurrentUser ();
            if (user != null){
                //user is signed in stay here
                //set Name for logged in user
                //nameTv.setText (user.getDisplayName ());
                myUid = user.getUid ();

            }
            else {
                //user is not signed in, go to main activity
                startActivity (new Intent (ChatActivity.this,MainActivity.class));
                finish ();
            }
        } catch (Exception e){
            Toasty.warning (ChatActivity.this,"checkUserStatus : " + e.getMessage (),Toasty.LENGTH_LONG).show();
        }
    }
    //Initialization View in this Activity
    public void init(){

        profileIv = (ImageView) findViewById (R.id.profileIv);
        nameTv = (TextView) findViewById (R.id.receiverTV);
        userStatusTv = (TextView)findViewById (R.id.receiverStatusTV);
        send = (ImageButton) findViewById (R.id.sendBtn);
        messageEt= (EditText) findViewById (R.id.messageET);
        attachBtn = (ImageButton) findViewById (R.id.attachBtn);
    }



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
                    Toasty.error (ChatActivity.this, "SendImageMessage 1 : "+ e.getMessage ());
                }

            }
            else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                //user this image uri to upload to firebase storage
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace ();
                    Toasty.error (ChatActivity.this, "SendImageMessage 2 : "+ e.getMessage ());
                }
            }
        }
        super.onActivityResult (requestCode, resultCode, data);
    }


}

