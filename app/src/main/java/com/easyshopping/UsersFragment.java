package com.easyshopping;



import android.content.Intent;
import android.os.Bundle;

import Adapters.AdapterUsers;
import Adapters.AdapterUsersChat;
import Models.ModelShops;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    //Declare RecyclerView
    RecyclerView recyclerView;

    ArrayList<ModelShops> userslist;
    AdapterUsersChat adapterUsers;
    FirebaseAuth firebaseAuth;


    public UsersFragment () {
        // Required empty public constructor
    }


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate (R.layout.fragment_users, container, false);

        //init Recyclerview
        recyclerView = view.findViewById (R.id.users_RecyclerView);

        //Set it's properties
        recyclerView.setHasFixedSize (true);
        recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));
        //We need to create java class for recyclerview (ModelUsers.java) to handle the recyclerview

        //initailization userlist
        userslist = new ArrayList<> ();
        //Init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance ();
        getAllUsers ();
        try {
            return view;
        } catch (Exception e) {
            Toasty.warning (getContext (), "On create View : " + e.getMessage (), Toasty.LENGTH_LONG).show ();
        }



        return view;
    }



    private void getAllUsers () {
        try {
            final FirebaseUser fUser = FirebaseAuth.getInstance ().getCurrentUser ();
            DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
            ref.addValueEventListener (new ValueEventListener () {
                @Override
                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    //clear list before adding
                    userslist.clear ();

                    for (DataSnapshot ds:dataSnapshot.getChildren ()){
                        ModelShops modelShops = ds.getValue (ModelShops.class);
                        String shopCity = String.valueOf (ds.child ("Address").child ("city").getValue ());

                            userslist.add (modelShops);



                    }
                    //setup adapter
                    adapterUsers= new AdapterUsersChat (getActivity (), userslist);
                    //set adapter to recyclerView
                    recyclerView.setAdapter (adapterUsers);
                }

                @Override
                public void onCancelled (@NonNull DatabaseError databaseError) {

                }
            });



        }catch (Exception e){
            Toasty.warning (getContext (),"Get All users : " + e.getMessage (),Toasty.LENGTH_LONG).show ();
        }
    }

    private void searchUser (final String query) {
        try {
            //Get current user
            final FirebaseUser fUser = FirebaseAuth.getInstance ().getCurrentUser ();
            //Get path of database named "Users containing users info
            DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
            //get all data from path
            ref.addValueEventListener (new ValueEventListener () {
                @Override
                public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    userslist.clear ();
                    for (DataSnapshot ds : dataSnapshot.getChildren ()) {
                        ModelShops modelUsers = ds.getValue (ModelShops.class);
                        /**Conditions to fulfil search :
                         * 1) User not current user
                         * 2)Them user name or email contains text entered
                         * in SearchView (case insensitive
                         *
                         */

                        //Get All Searched users except currently signed in user
                        if (!modelUsers.getPerson_Id ().equals (fUser.getUid ())) {
                            if ((modelUsers.getFirst_name ().toLowerCase ().contains (query.toLowerCase ()))
                                    || modelUsers.getLast_name ().toLowerCase ().contains (query.toLowerCase ())
                                    || modelUsers.getEmail ().toLowerCase ().contains (query.toLowerCase ())){
                                userslist.add (modelUsers);
                            }

                        }
                        //Adapter
                        adapterUsers = new AdapterUsersChat (getActivity (), userslist);
                        //refresh adapter
                        adapterUsers.notifyDataSetChanged ();
                        //set Adapter to recyclerView
                        recyclerView.setAdapter (adapterUsers);
                    }
                }

                @Override
                public void onCancelled (@NonNull DatabaseError databaseError) {

                }
            });

        }catch (Exception e){
            Toasty.warning (getContext (),e.getMessage (),Toasty.LENGTH_LONG).show ();
        }
    }

    /***********
     * From here for search option
     * @param savedInstanceState
     */
    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        try {
            setHasOptionsMenu (true);//To show menu option in fragment
            super.onCreate (savedInstanceState);
        }catch (Exception e){
            Toasty.warning (getContext (),"onCreate : " + e.getMessage (),Toasty.LENGTH_LONG).show ();
        }

    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        //inflating menu
        /**
         * Note : when copy this method from Dashboard there is many error
         * 1-add MenuInflater inflater in onCreateOptionsMenu
         * inflater.inflate (R.menu.menu_main,menu); instead of previous method
         * add parameter to (super.onCreateOptionsMenu (menu,inflater);) and remove return
         */

        inflater.inflate (R.menu.menu_main,menu);
        //hide addpost icon from this fragment
       //SearchView
        MenuItem item = menu.findItem (R.id.action_Search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView (item);
        menu.findItem (R.id.ic_add_participant).setVisible (false);
        menu.findItem (R.id.action_groupInfo).setVisible (false);

        //Search listener
        searchView.setOnQueryTextListener (new SearchView.OnQueryTextListener () {
            @Override
            public boolean onQueryTextSubmit (String s) {
                //Called when user press search button from keyboard
                //if search query is not empty then search
                try{
                    if (!TextUtils.isEmpty (s.trim ())){
                        //Search text contain text ,seart it
                        searchUser(s);
                    }
                    else {
                        //Search text empty, get all users
                        getAllUsers ();
                    }}
                catch (Exception e){
                    Toasty.warning (getContext (),"onCreateOptionsMenu : " + e.getMessage (),Toasty.LENGTH_LONG).show ();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange (String s) {
                try {
                    //Called whenever user press any single letter
                    //if search query is not empty then search
                    if (!TextUtils.isEmpty (s.trim ())){
                        //Search text contain text ,seart it
                        searchUser(s);
                    }
                    else {
                        //Search text empty, get all users
                        getAllUsers ();
                    }
                }catch (Exception e){
                    Toasty.warning (getContext (),"onQueryTextChange : " + e.getMessage (),Toasty.LENGTH_LONG).show ();
                }

                return false;
            }
        });


        super.onCreateOptionsMenu (menu,inflater);
    }


    /* Handle menu item clicks*/

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        checkUserStatus ();        // Get item id
        int id = item.getItemId ();
        if (id == R.id.action_logout){
            firebaseAuth.getInstance ().signOut ();
            checkUserStatus ();
            //Call check user method
        }
        else if(id==R.id.action_create_group){
            //go to create Activity
            Intent intent = new Intent (getActivity (), GroupCreateActivity.class);
            startActivity (intent);
        }


        return super.onOptionsItemSelected (item);
    }

    private void checkUserStatus(){
        //Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user != null){
            //user is signed in stay here
            //set email for logged in user
            // mProfileTv.setText (user.getEmail ());
            //move to
            /**
             *
             *  06-Add options menu for adding Logout option
             */
        }
        else {
            //user is not signed in, go to main activity
            startActivity (new Intent (getActivity (),LoginActivity.class));
            getActivity ().finish ();
        }
    }
}
