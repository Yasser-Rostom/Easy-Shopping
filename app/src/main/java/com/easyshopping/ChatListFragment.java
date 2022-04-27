package com.easyshopping;

import android.content.Intent;
import android.os.Bundle;

import Adapters.AdapterChatlist;
import Models.ModelChat;
import Models.ModelChatlist;
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
public class ChatListFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    //Declare in part 26
    RecyclerView chatListRecyclerView;

    List<ModelChatlist> chatlistList;
    ArrayList<ModelShops> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;



    public ChatListFragment () {
        // Required empty public constructor
    }


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate (R.layout.fragment_chat_list, container, false);
        firebaseAuth = FirebaseAuth.getInstance ();
        currentUser= FirebaseAuth.getInstance ().getCurrentUser ();
        chatListRecyclerView= view.findViewById (R.id.chatListRecyclerView);

        //Set it's properties
        chatListRecyclerView.setHasFixedSize (true);
        chatListRecyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));

        chatlistList = new ArrayList<> ();

        reference= FirebaseDatabase.getInstance ().getReference ("Chatlist").child (currentUser.getUid ());
        reference.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear ();
                for (DataSnapshot ds : dataSnapshot.getChildren ()){
                    ModelChatlist chatlist = ds.getValue (ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void loadChats () {
        usersList = new ArrayList<> ();
        reference = FirebaseDatabase.getInstance ().getReference ("Persons");
        reference.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                usersList.clear ();
                for (DataSnapshot ds: dataSnapshot.getChildren ()){
                    ModelShops user = ds.getValue (ModelShops.class);
                    for (ModelChatlist chatlist1: chatlistList){
                        if (user.getPerson_Id () !=null && user.getPerson_Id ().equals (chatlist1.getId ())){
                            usersList.add (user);
                            break;
                        };

                    }
                }
                //adapter
                adapterChatlist = new AdapterChatlist (getContext (), usersList);
                //set Adapter
                chatListRecyclerView.setAdapter (adapterChatlist);
                //set Last message
                for (int i = 0; i<usersList.size (); i++){
                    lastMessage(usersList.get (i).getPerson_Id ());

                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void lastMessage (final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Chats");
        reference.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: dataSnapshot.getChildren ()){
                    ModelChat chat = ds.getValue (ModelChat.class);
                    if (chat == null ){
                        continue;
                    }
                    String sender = chat.getSender ();
                    String receiver = chat.getReciever ();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if ((chat.getReciever ().equals (currentUser.getUid ()) &&
                            chat.getSender ().equals (userId)) ||
                            (chat.getReciever ().equals (userId) &&
                                    chat.getSender ().equals (currentUser.getUid ()))){
                        //Instead of displaying url in message show "sent photo"
                        //This if add in part 29
                        if (chat.getType ().equals ("image")){
                            theLastMessage = "Sent a Photo...";
                        }
                        else {
                            theLastMessage = chat.getMessage ();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap (userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged ();

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
        menu.findItem (R.id.ic_add_participant).setVisible (false);
        menu.findItem (R.id.action_groupInfo).setVisible (false);
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
            startActivity (new Intent(getActivity (), GroupCreateActivity.class));
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
            startActivity (new Intent (getActivity (),MainActivity.class));
            getActivity ().finish ();
        }
    }
}
