package Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyshopping.GroupChatActivity;
import com.easyshopping.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import Models.ModelGroupChatslist;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterGroupChatsList extends RecyclerView.Adapter<AdapterGroupChatsList.HolderGroupChatList> {
    //Contect
    private Context context;
    ArrayList<ModelGroupChatslist> groupChatslists;

    public AdapterGroupChatsList (Context context, ArrayList<ModelGroupChatslist> groupChatslists) {
        this.context = context;
        this.groupChatslists = groupChatslists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_group_chats, parent, false);;
        return new AdapterGroupChatsList .HolderGroupChatList (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderGroupChatList holder, int position) {

        ModelGroupChatslist modelGroupChatslist = groupChatslists.get (position);

        final String groupId = modelGroupChatslist.getGroupId ();
        String groupIcon = modelGroupChatslist.getGroupIcon ();
        String groupTitle = modelGroupChatslist.getGroupTitle ();

        holder.nameTv.setText ("");
        holder.timeTv.setText ("");
        holder.messageTv.setText ("");

        loadLastMessage(modelGroupChatslist, holder);
        holder.groupTitleTv.setText (groupTitle);
        try{
            Picasso.get ().load (groupIcon).placeholder (R.drawable.ic_group_primary).into (holder.groupIconIv);

        }catch (Exception e){
            holder.groupIconIv.setImageResource (R.drawable.ic_group_primary);
        }
        //handle group click
        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //move to GroupChatActivity
                Intent intent = new Intent (context, GroupChatActivity.class);
                intent.putExtra ("groupId", groupId);
                context.startActivity (intent);

            }
        });
    }

    private void loadLastMessage (ModelGroupChatslist modelGroupChatslist, final HolderGroupChatList holder) {
        //get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (modelGroupChatslist.getGroupId ()).child ("Messages").limitToLast (1)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren ()){
                            //get data
                            String message = "" + ds.child ("message").getValue ();
                            String timestamp = "" + ds.child ("timestamp").getValue ();
                            String sender = "" + ds.child ("sender").getValue ();
                            String type = "" + ds.child ("type").getValue ();

                            //Convert time stamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance (Locale.ENGLISH);
                            cal.setTimeInMillis (Long.parseLong (timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            if (type.equals ("text")) {
                                holder.messageTv.setText (message);
                            }
                            else {
                                holder.messageTv.setText ("Send photo...");
                            }
                            holder.timeTv.setText (dateTime);

                            //get info of sender of last message
                            DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
                            ref.orderByChild ("person_Id").equalTo (sender)
                                    .addValueEventListener (new ValueEventListener () {
                                        @Override
                                        public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                                           for (DataSnapshot ds:dataSnapshot.getChildren ()){
                                               String firstName = ""+ds.child ("first_name").getValue ();
                                               String lastName = ""+ds.child ("last_name").getValue ();
                                               String name = firstName + " " + lastName;
                                               holder.nameTv.setText (name);

                                           }
                                        }

                                        @Override
                                        public void onCancelled (@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public int getItemCount () {
        return groupChatslists.size ();
    }

    //Vies holder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{
        private ImageView groupIconIv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;
        public HolderGroupChatList(@NonNull View itemView){
            super(itemView);
            groupIconIv = itemView.findViewById (R.id.groupIconIv);
            groupTitleTv = itemView.findViewById (R.id.groupTitleTv);
            nameTv = itemView.findViewById (R.id.nameTv);
            messageTv = itemView.findViewById (R.id.messageTv);
            timeTv = itemView.findViewById (R.id.timeTv);
        }
    }
}
