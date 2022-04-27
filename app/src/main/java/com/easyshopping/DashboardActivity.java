package com.easyshopping;



//import Notifications.Token;
import Notifications.Token;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import es.dmoral.toasty.Toasty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {
    //Firebase Auth
    FirebaseAuth firebaseAuth;
    // TextView mProfileTv;
    ActionBar actionBar;
    //For update token
    String mUID;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_dashboard);
        initializationViews ();
        //Actionbar and its title
        actionBar = getSupportActionBar ();
        actionBar.setTitle ("Profile");

        BottomNavigationView navigationView = findViewById (R.id.navigationBar);
        navigationView.setOnNavigationItemSelectedListener (selectedListener);
        //Set home fragment the default fragment when luanch our app

        actionBar.setTitle ("Users...");
        UsersFragment usersFragment = new UsersFragment ();
        FragmentTransaction mUS = getSupportFragmentManager ().beginTransaction ();
        mUS.replace (R.id.content, usersFragment, "");
        mUS.commit ();



        checkUserStatus ();

        //update Token
        updateToken (FirebaseInstanceId.getInstance ().getToken ());


    }


    @Override
    protected void onResume () {
        checkUserStatus ();
        super.onResume ();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener () {
                @Override
                public boolean onNavigationItemSelected (@NonNull MenuItem item) {
                    //Handle item clicks
                    switch (item.getItemId ()) {


                        case R.id.nav_users:
                            //Users fragment transaction
                            actionBar.setTitle ("Users...");
                            UsersFragment usersFragment = new UsersFragment ();
                            FragmentTransaction mUS = getSupportFragmentManager ().beginTransaction ();
                            mUS.replace (R.id.content, usersFragment, "");
                            mUS.commit ();
                            return true;
                        case R.id.nav_Chat:
                            //Users fragment transaction
                            actionBar.setTitle ("Chats...");
                            ChatListFragment chatListFragment= new ChatListFragment ();
                            FragmentTransaction Ch = getSupportFragmentManager ().beginTransaction ();
                            Ch.replace (R.id.content, chatListFragment, "");
                            Ch.commit ();
                            return true;
                        case R.id.nav_GroupChats:
                            //Users fragment transaction
                            actionBar.setTitle ("Group Chats...");
                            GroupChatsListFragment groupChatsListFragment= new GroupChatsListFragment ();
                            FragmentTransaction Ch1 = getSupportFragmentManager ().beginTransaction ();
                            Ch1.replace (R.id.content, groupChatsListFragment, "");
                            Ch1.commit ();
                            return true;
                    }
                    return false;
                }
            };

    /**
     * Part 2
     */
    private void checkUserStatus(){
        //Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user != null){
            //user is signed in stay here
            //set email for logged in user
            //mProfileTv.setText (user.getEmail ());
            //move to
            mUID = user.getUid ();
            //Toasty.warning (DashboardActivity.this, mUID ,Toasty.LENGTH_LONG).show ();
            //Save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences ("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit ();
            editor.putString ("Current_USERID", mUID);
            editor.apply ();
            /**
             *
             *  06-Add options menu for adding Logout option
             */
        }
        else {
            //user is not signed in, go to main activity
            startActivity (new Intent (DashboardActivity.this,LoginActivity.class));
            finish ();
        }
    }

    /**
     * Part2 ---2
     */
    @Override
    protected void onStart () {
        //Check on start of app
        checkUserStatus ();
        super.onStart ();

    }

    @Override
    public void onBackPressed () {

        super.onBackPressed ();
        finish ();
    }

    public void initializationViews (){
        firebaseAuth = FirebaseAuth.getInstance ();
        //mProfileTv = (TextView) findViewById (R.id.profileTv);

    }

   public void updateToken(String token){


        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Token");
        Token mToken = new Token (token);
        ref.child (mUID).setValue (mToken);

    }
    /** Mooooooove this methods to fragments because if we leave them here
     * the action bar will duplicated

     * Part 2 6
     * Inflate options menu

     @Override
     public boolean onCreateOptionsMenu (Menu menu) {
     //inflating menu
     getMenuInflater ().inflate (R.menu.menu_main,menu);
     return super.onCreateOptionsMenu (menu);
     }
     /* Handle menu item clicks

     @Override
     public boolean onOptionsItemSelected (@NonNull MenuItem item) {
     // Get item id
     int id = item.getItemId ();
     if (id == R.id.action_logout){
     firebaseAuth.getInstance ().signOut ();

     //Call check user method
     checkUserStatus ();
     }
     return super.onOptionsItemSelected (item);
     }*/
}
