package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyshopping.ChatActivity;
import com.easyshopping.GroupCreateActivity;
import com.easyshopping.R;
import com.easyshopping.UserAccountDetials;
import com.easyshopping.UsersFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterUsersChat extends RecyclerView.Adapter<AdapterUsersChat.HolderUsers>{

    private Context context;
    public ArrayList<ModelShops> userslist;


    public AdapterUsersChat (Context context, ArrayList<ModelShops> userslist) {
        this.context = context;
        this.userslist = userslist;
    }

    @NonNull
    @Override
    public HolderUsers onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_users, parent, false);
        return new AdapterUsersChat.HolderUsers (view);
    }


    @Override
    public void onBindViewHolder (@NonNull HolderUsers holder, int position) {

        //get data
        ModelShops modelShops = userslist.get (position);
        String firstname = modelShops.getFirst_name ();
        String lastname = modelShops.getLast_name ();
        String phone = modelShops.getPhone ();
        final String person_Id = modelShops.getPerson_Id ();
        String timestamp = modelShops.getTimestamp ();
        String accountType = modelShops.getAccountType ();
        String email = modelShops.getEmail ();
        final String status = modelShops.getStatus ();
        String profileImage = modelShops.getProfileImage ();


       //loadReviews(modelShops,holder);

        //Set Data
        holder.nameTv.setText (firstname + " " + lastname);
        holder.roleTv.setText (accountType);
        holder.statusTv.setText (status);


        try{
            Picasso.get ().load (profileImage).placeholder (R.drawable.ic_store_gray).into (holder.usersprofileIv);
        }catch (Exception e){
            Picasso.get ().load (R.drawable.ic_store_gray).into (holder.usersprofileIv);
        }


        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {

                try {

                    if (status.equals ("disactive")) {
                        Toasty.warning (context, "This Account is disactive..... ", Toasty.LENGTH_LONG).show ();
                    } else {
                        //Show dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder (context);
                        builder.setItems (new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //Proifle clicked
                                    /** Click to go to ThereProfileActivity with uid,this uid is of clicked user
                                     * which will be user to show user specific data/posts*/
                             /*   Intent intent = new Intent (context, UsersFragment.class);
                                intent.putExtra ("uid",person_Id);
                                context.startActivity (intent);*/
                                }
                                if (which == 1) {

                                    Intent intent = new Intent (context, ChatActivity.class);
                                    intent.putExtra ("person_Id", person_Id);
                                    context.startActivity (intent);
                                }

                            }
                        });
                        builder.create ().show ();

                    }
                }
                catch (Exception e){
                    Toasty.warning (context,"Adapterusers >> itemview.setOnclicklistener : "+ e.getMessage (), Toasty.LENGTH_LONG).show ();
                }


            }  });


    }




    @Override
    public int getItemCount () {
        return userslist.size ();
    }


    class HolderUsers extends RecyclerView.ViewHolder{
        //holds views of recyclerView
        private ImageView usersprofileIv;
        private TextView nameTv, roleTv, statusTv;


        public HolderUsers (@NonNull View itemView) {

            super (itemView);
            usersprofileIv = itemView.findViewById (R.id.usersprofileIv);
            nameTv = itemView.findViewById (R.id.nameTv);
            roleTv = itemView.findViewById (R.id.roleTv);
            statusTv = itemView.findViewById (R.id.statusTv);

        }

    }
    public void showToastSuccess(String s){
        Toasty.success (context,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastWarning(String s){
        Toasty.warning (context,s,Toasty.LENGTH_LONG).show ();
    }
    public void showToastError(String s){
        Toasty.error (context,s,Toasty.LENGTH_LONG).show ();
    }


}
