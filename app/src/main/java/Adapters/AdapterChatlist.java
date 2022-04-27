package Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyshopping.ChatActivity;
import com.easyshopping.GroupCreateActivity;
import com.easyshopping.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterChatlist extends  RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    List<ModelShops> usersList;
    Context context;
    private HashMap<String,String> lastMessageMap;

    public AdapterChatlist ( Context context,List<ModelShops> usersList) {
        this.usersList = usersList;
        this.context = context;
        lastMessageMap=new HashMap<> ();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        //Inflate layout rew_chatList
        View view = LayoutInflater.from (context).inflate (R.layout.row_chatlist, parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyHolder holder, int position) {
        //Get Data
        final String person_Id = usersList.get (position).getPerson_Id ();
        String userImage = usersList.get (position).getProfileImage ();
        String fname = usersList.get (position).getFirst_name ();
        String lname = usersList.get (position).getLast_name ();

        String userName= fname + " " + lname;
        String lastMessage = lastMessageMap.get (person_Id);

        //Set Data
        holder.chatlistNameTv.setText (userName);
      if (lastMessage == null || lastMessage.equals ("default")){
            holder.LastMessageTv.setVisibility (View.GONE);
        }
        else {
            holder.LastMessageTv.setVisibility (View.VISIBLE);
            holder.LastMessageTv.setText (lastMessage);
       }

        try {
            Picasso.get ().load (userImage).placeholder (R.drawable.ic_person_gray).into (holder.chatlistProfileIv);
        }catch (Exception e){
            //showToastError ("Load user Image : " + e.getMessage ());
           holder.chatlistProfileIv.setImageResource (R.drawable.ic_person_gray);
        }
        //Handle Image view for online/offline status
     if (usersList.get (position).getOnlineStatus ().equals ("online")){
            //online
            holder.onlineStatusIv.setImageResource (R.drawable.circle_online);
        }
        else {
            //offline
            holder.onlineStatusIv.setImageResource (R.drawable.circle_offline);
        }
        //handle click on user in chatlist
        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //starts chat activity with that user
                Intent intent = new Intent (context, ChatActivity.class);
                intent.putExtra ("person_Id", person_Id);
                context.startActivity (intent);
            }
        });



    }


    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put (userId,lastMessage);
    }

    @Override
    public int getItemCount () {
        return usersList.size ();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        // View of row_Chatlist
       ImageView chatlistProfileIv,onlineStatusIv;
       TextView chatlistNameTv,LastMessageTv;



        public MyHolder (@NonNull View itemView){
            super (itemView);
            chatlistProfileIv=itemView.findViewById (R.id.chatlistProfileIv);
            onlineStatusIv=itemView.findViewById (R.id.onlineStatusIv);
            chatlistNameTv=itemView.findViewById (R.id.chatlistNameTv);
            LastMessageTv=itemView.findViewById (R.id.LastMessageTv);


        }


    }

    public void showToastSuccess(String s){
        Toasty.success (context,s, Toasty.LENGTH_SHORT).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (context,s, Toasty.LENGTH_SHORT).show ();
    }
    public void showToastError(String s){
        Toasty.error (context,s, Toasty.LENGTH_SHORT).show ();
    }
}
