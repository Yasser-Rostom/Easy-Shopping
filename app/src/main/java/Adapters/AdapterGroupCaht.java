package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.easyshopping.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Models.ModelGroupChat;
import Notifications.Data;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterGroupCaht extends  RecyclerView.Adapter<AdapterGroupCaht.GroupChatHolder>{

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    Context context;
    String imageURL;
    List<ModelGroupChat> chatList;
    FirebaseUser firebaseUser;



    public AdapterGroupCaht (Context context, List<ModelGroupChat> chatList) {
        this.context = context;
       // this.imageURL = imageURL;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public AdapterGroupCaht.GroupChatHolder onCreateViewHolder (@NonNull ViewGroup parent, int i) {
        //inflate layouts :row_chat_left.xml for receiver, row_chat_xml for sender

        if (i== MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new AdapterGroupCaht.GroupChatHolder (view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new AdapterGroupCaht.GroupChatHolder (view);
        }


    }

    @Override
    public void onBindViewHolder (@NonNull AdapterGroupCaht.GroupChatHolder holder, final int i) {
        ModelGroupChat modelGroupChat = chatList.get (i);

        //Get Data
        String message = chatList.get (i).getMessage ();
        String senderUid = chatList.get (i).getSender ();
        String type = chatList.get (i).getType ();
        String timeStamp = chatList.get (i).getTimestamp ();

        //Convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance (Locale.ENGLISH);
        cal.setTimeInMillis (Long.parseLong (timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
        //set data
       if (type.equals ("text")){
            //text message
            holder.messageTv.setVisibility (View.VISIBLE);
            holder.messageIv.setVisibility (View.GONE);

            holder.messageTv.setText (message);
        }
        else{
            //image message
            holder.messageTv.setVisibility (View.GONE);
            holder.messageIv.setVisibility (View.VISIBLE);
            try{
                Picasso.get ().load (message).placeholder (R.drawable.ic_person_gray).into (holder.messageIv);
            }
            catch (Exception e){
                holder.messageIv.setImageResource (R.drawable.ic_person_gray);
            }

        }
       // holder.messageTv.setText (message);
        holder.timeTv.setText (dateTime);

        setUserName(modelGroupChat, holder);

       try{
          /* Picasso.get ().load (imageURL).placeholder (R.drawable.ic_default_img)
                    .into (holder.profileIv);*/
            if (imageURL != null ){
                Glide.with(holder.profileIv.getContext ())
                        .load(imageURL)
                        .into(holder.profileIv);}
            else{
                holder.profileIv.setImageResource (R.drawable.ic_group_primary);
            }
        }
        catch (Exception e){
            Toasty.warning (context," Picasso Adapter chat  : " +e.getMessage (), Toasty.LENGTH_LONG).show ();
        }

       /* holder.messageLayout.setOnLongClickListener (new View.OnLongClickListener () {
            @Override
            public boolean onLongClick (View v) {
                //AlerDialog for delete message
                final AlertDialog.Builder builder = new AlertDialog.Builder (context);
                builder.setTitle ("Delete Message : ");
                builder.setMessage ("Are you sure you want to delete this message");
                //Delete Button
                builder.setPositiveButton ("Delete ", new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick (DialogInterface dialog, int which) {
                        //Handle click delelte
                        deleteMessage(i);
                    }
                });
                builder.setNegativeButton ("Cancle", new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick (DialogInterface dialog, int which) {
                        //Dismiss alert dialog
                        dialog.dismiss();
                    }
                });
                //Create and show dialog
                builder.create ().show ();
                return true;
            }
        });*/
       /* try {
            //Set seen/deliverd status
            if (i == chatList.size ()-1){
                if (chatList.get (i).isSeen ()){
                    holder.isSeenTv.setText ("Seen");
                }
                else{
                    holder.isSeenTv.setText ("Delivered");
                }
            }else{
                holder.isSeenTv.setVisibility (View.GONE);
            }
        }
        catch (Exception e){
            Toasty.warning (context," onBindViewHolder : " +e.getMessage (), Toasty.LENGTH_LONG).show ();
        }*/
    }

    private void setUserName (ModelGroupChat modelGroupChat, final GroupChatHolder holder) {
        //get Sender info from  uid in model
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.orderByChild ("person_Id").equalTo (modelGroupChat.getSender ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren ()){
                        String firstName = ""+ds.child ("first_name").getValue ();
                        String lastName = ""+ds.child ("last_name").getValue ();
                        String imageProfile= "" + ds.child ("profileImage").getValue ();
                        String name = firstName + " " + lastName;
                        holder.nameTv.setText (name);
                        try{
                            Picasso.get ().load (imageProfile).placeholder (R.drawable.ic_person_gray).into (holder.profileIv);
                        }catch (Exception e){
                            holder.profileIv.setImageResource (R.drawable.ic_person_gray);
                        }
                    }

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void deleteMessage (int position) {
        final String myUId = FirebaseAuth.getInstance ().getCurrentUser ().getUid ();
        /**Logic :
         * Get timestamp of clicked message
         * Compare the timestamp of the clicked message with all messages in Chats
         * Where both values matches delete that message
         */
        String msgTimeStamp = chatList.get (position).getTimestamp ();
        DatabaseReference dbRef = FirebaseDatabase.getInstance ().getReference ("Groups").child ("Chats");
        Query query = dbRef.orderByChild ("timestamp").equalTo (msgTimeStamp);
        query.addListenerForSingleValueEvent (new ValueEventListener () {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren ()) {
                    /**
                     * if you want to allow sender to delete only his message then compare sender value with current user's uid if they match means it the message of sender that is trying to delete
                     */
                    if (ds.child ("sender").getValue ().equals (myUId)){


                        /**We can do one of two things here
                         * 1)Remove the message from  Chats
                         * 2)Set the value of message "This message was deleted"
                         * so do whatever you want
                         */
                        //1)Remove the message from Chats
                        ds.getRef ().removeValue ();
                        //2) Set the value of message "This message was deleted?
                  /*  HashMap<String,Object> hashMap = new HashMap<> ();
                    hashMap.put("message","This message was deleted...");
                    ds.getRef ().updateChildren (hashMap);*/
                        Toasty.success (context, "Message was deleted...", Toasty.LENGTH_LONG).show ();

                    }
                    else {
                        Toasty.warning (context, "You can only delete your messages only...", Toasty.LENGTH_LONG).show ();
                    }
                }

            }

            @Override
            public void onCancelled (@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount () {
        return chatList.size ();
    }

    @Override
    public int getItemViewType (int position) {
        try {
            //get currently signed in user
            firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        }
        catch (Exception e){
            Toasty.warning (context," 3 " + e.getMessage (), Toasty.LENGTH_LONG).show ();
        }
        if (chatList.get (position).getSender ().equals (firebaseUser.getUid ())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    //view holder class
    class GroupChatHolder extends RecyclerView.ViewHolder{

        //views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv, nameTv;
        LinearLayout messageLayout;
        public GroupChatHolder (@NonNull View itemView){
            super(itemView);
            try {
                //init views
                profileIv = itemView.findViewById (R.id.profileIv);
                nameTv = itemView.findViewById (R.id.nameTv);
                messageTv = itemView.findViewById (R.id.messageTV);
                timeTv = itemView.findViewById (R.id.timeTv);
                messageIv=itemView.findViewById (R.id.messageIv);
                //isSeenTv = itemView.findViewById (R.id.isSeenTv);
               // messageLayout = itemView.findViewById (R.id.messageLayout);


            }catch (Exception e){
                Toasty.error (context, " GroupChatHolder " + e.getMessage (), Toasty.LENGTH_LONG).show ();
            }
        }

    }

}

