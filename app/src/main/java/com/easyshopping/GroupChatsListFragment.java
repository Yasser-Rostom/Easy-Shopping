package com.easyshopping;


import android.content.Intent;
import android.os.Bundle;

import Adapters.AdapterGroupChatsList;
import Models.ModelGroupChatslist;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupChatsListFragment extends Fragment {

    private RecyclerView groupRv;
    private FirebaseAuth firebaseAuth;
    ArrayList<ModelGroupChatslist> groupChatslists;
    AdapterGroupChatsList adapterGroupChatsList;


    public GroupChatsListFragment () {
        // Required empty public constructor
    }


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate (R.layout.fragment_group_chats_list, container, false);
        groupRv = view.findViewById (R.id.groupChats_RecyclerView);
        firebaseAuth = FirebaseAuth.getInstance ();

        //Set it's properties
        groupRv.setHasFixedSize (true);
        groupRv.setLayoutManager (new LinearLayoutManager (getContext ()));

        loadGroupChatslist ();

        return view;
    }

    private void loadGroupChatslist () {
        groupChatslists = new ArrayList<> ();

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                groupChatslists.clear ();
                for (DataSnapshot ds : dataSnapshot.getChildren ()) {
                    //if Current user's uid exists in participants list of roup then show that group
                    if (ds.child ("Participants").child (firebaseAuth.getUid ()).exists ()) {

                        ModelGroupChatslist model = ds.getValue (ModelGroupChatslist.class);
                        groupChatslists.add (model);
                    }

                    adapterGroupChatsList = new AdapterGroupChatsList (getActivity (), groupChatslists);
                    groupRv.setAdapter (adapterGroupChatsList);

                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void searchGroupChatslist (final String query) {
        groupChatslists = new ArrayList<> ();

        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                groupChatslists.clear ();
                for (DataSnapshot ds : dataSnapshot.getChildren ()) {
                    //if Current user's uid exists in participants list of roup then show that group
                    if (ds.child ("Participants").child (firebaseAuth.getUid ()).exists ()) {
                        //Serach by group Title
                        if (ds.child ("groupTitle").toString ().toLowerCase ().contains (query.toLowerCase ())) {
                            ModelGroupChatslist model = ds.getValue (ModelGroupChatslist.class);
                            groupChatslists.add (model);
                        }
                    }

                    adapterGroupChatsList = new AdapterGroupChatsList (getActivity (), groupChatslists);
                    groupRv.setAdapter (adapterGroupChatsList);

                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        try {
            setHasOptionsMenu (true);//To show menu option in fragment
            super.onCreate (savedInstanceState);
        } catch (Exception e) {
            Toasty.warning (getContext (), "onCreate : " + e.getMessage (), Toasty.LENGTH_LONG).show ();
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

        inflater.inflate (R.menu.menu_main, menu);
        //hide addpost icon from this fragment

        //SearchView
        MenuItem item = menu.findItem (R.id.action_Search);
        menu.findItem (R.id.action_create_group).setVisible (false);
        menu.findItem (R.id.ic_add_participant).setVisible (false);
        menu.findItem (R.id.action_groupInfo).setVisible (false);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView (item);

        //Search listener
        searchView.setOnQueryTextListener (new SearchView.OnQueryTextListener () {
            @Override
            public boolean onQueryTextSubmit (String s) {
                //Called when user press search button from keyboard
                //if search query is not empty then search
                try {
                    if (!TextUtils.isEmpty (s.trim ())) {
                        //Search text contain text ,seart it
                        searchGroupChatslist (s);
                    } else {
                        //Search text empty, get all users
                        loadGroupChatslist ();
                    }
                } catch (Exception e) {
                    Toasty.warning (getContext (), "onCreateOptionsMenu : " + e.getMessage (), Toasty.LENGTH_LONG).show ();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange (String s) {
                try {
                    //Called whenever user press any single letter
                    //if search query is not empty then search
                    if (!TextUtils.isEmpty (s.trim ())) {
                        //Search text contain text ,seart it
                        searchGroupChatslist (s);
                    } else {
                        //Search text empty, get all users
                        loadGroupChatslist ();
                    }
                } catch (Exception e) {
                    Toasty.warning (getContext (), "onQueryTextChange : " + e.getMessage (), Toasty.LENGTH_LONG).show ();
                }

                return false;
            }
        });


        super.onCreateOptionsMenu (menu, inflater);
    }


    /* Handle menu item clicks*/

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        checkUserStatus ();        // Get item id
        int id = item.getItemId ();
        if (id == R.id.action_logout) {
            firebaseAuth.getInstance ().signOut ();
            checkUserStatus ();
            //Call check user method
        }

        return super.onOptionsItemSelected (item);
    }

    private void checkUserStatus () {
        //Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser ();
        if (user != null) {
            //user is signed in stay here
            //set email for logged in user
            // mProfileTv.setText (user.getEmail ());
            //move to
            /**
             *
             *  06-Add options menu for adding Logout option
             */
        } else {
            //user is not signed in, go to main activity
            startActivity (new Intent (getActivity (), DashboardActivity.class));
            getActivity ().finish ();
        }
    }
}
