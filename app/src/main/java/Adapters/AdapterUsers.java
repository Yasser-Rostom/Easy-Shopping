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
import com.easyshopping.DashboardActivity;
import com.easyshopping.GroupInfoActivity;
import com.easyshopping.R;
import com.easyshopping.ShopDetailsActivity;
import com.easyshopping.UserAccountDetials;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import Models.ModelProducts;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.HolderUsers>{

    private Context context;
    public ArrayList<ModelShops> userslist;



    public AdapterUsers (Context context, ArrayList<ModelShops> userslist) {
        this.context = context;
        this.userslist = userslist;
    }

    @NonNull
    @Override
    public HolderUsers onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_users, parent, false);
        return new AdapterUsers.HolderUsers (view);
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
        //Handle Click listener show shop details
       /* holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (context, UserAccountDetials.class);
                intent.putExtra ("person_Id", person_Id);
                context.startActivity (intent);
                 }
        });*/

        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                try {

                    String active = "Disable";
                    String disactive = "Enable";

                    if (status.equals ("active")) {
                        //Show dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder (context);
                        builder.setItems (new String[]{"Delete", active, "Go Chating"}, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //AlerDialog for delete message
                                    final AlertDialog.Builder builder = new AlertDialog.Builder (context);
                                    builder.setTitle ("Delete Account : ");
                                    builder.setMessage ("Are you sure you want to delete this Account");
                                    //Delete button
                                    builder.setPositiveButton ("Delete", new DialogInterface.OnClickListener () {
                                        @Override
                                        public void onClick (DialogInterface dialog, int which) {
                                            //Handle click delelte
                                            deleteAccount (which, person_Id);
                                        }
                                    });
                                    builder.setNegativeButton ("Cancle", new DialogInterface.OnClickListener () {
                                        @Override
                                        public void onClick (DialogInterface dialog, int which) {
                                            //Dismiss alert dialog
                                            dialog.dismiss ();
                                        }
                                    });
                                    //Create and show dialog
                                    builder.create ().show ();

                                }

                                if (which == 1) {
                                    disableAccount (which, person_Id);
                                }
                                if (which == 2) {

                                    Intent intent = new Intent (context, ChatActivity.class);
                                    intent.putExtra ("person_Id", person_Id);
                                    context.startActivity (intent);
                                }

                            }
                        });
                        builder.create ().show ();
                    }
                    else {
                        //Show dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder (context);
                        builder.setItems (new String[]{"Delete", disactive , "Go Chating"}, new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //AlerDialog for delete message
                                    final AlertDialog.Builder builder = new AlertDialog.Builder (context);
                                    builder.setTitle ("Delete Account : ");
                                    builder.setMessage ("Are you sure you want to delete this Account");
                                    //Delete button
                                    builder.setPositiveButton ("Delete", new DialogInterface.OnClickListener () {
                                        @Override
                                        public void onClick (DialogInterface dialog, int which) {
                                            //Handle click delelte
                                            deleteAccount (which, person_Id);
                                        }
                                    });
                                    builder.setNegativeButton ("Cancle", new DialogInterface.OnClickListener () {
                                        @Override
                                        public void onClick (DialogInterface dialog, int which) {
                                            //Dismiss alert dialog
                                            dialog.dismiss ();
                                        }
                                    });
                                    //Create and show dialog
                                    builder.create ().show ();

                                }

                                if (which == 1) {
                                    enableAccount (which, person_Id);
                                }
                                if (which == 2) {

                                    Intent intent = new Intent (context, ChatActivity.class);
                                    intent.putExtra ("person_Id", person_Id);
                                    context.startActivity (intent);
                                }

                            }
                        });
                        builder.create ().show ();
                    }

                } catch (Exception e) {
                    e.printStackTrace ();
                }
            }
        });
    }

    private void enableAccount (int which, String person_id) {
        HashMap<String,Object> hashMap = new HashMap<> ();
        hashMap.put ("status", "active");
        DatabaseReference dbRef = FirebaseDatabase.getInstance ().getReference ("Persons");
        dbRef.child (person_id).updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        Toasty.success (context, "" + "Account Disactivated successfully ...", Toasty.LENGTH_LONG).show ();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        Toasty.error (context, "" + e.getMessage (), Toasty.LENGTH_LONG).show ();
                    }
                });
    }

    private void disableAccount (int position, final String person_Id) {
               HashMap<String,Object> hashMap = new HashMap<> ();
               hashMap.put ("status", "disactive");
                DatabaseReference dbRef = FirebaseDatabase.getInstance ().getReference ("Persons");
               dbRef.child (person_Id).updateChildren (hashMap)
                       .addOnSuccessListener (new OnSuccessListener<Void> () {
                           @Override
                           public void onSuccess (Void aVoid) {
                               Toasty.success (context, "" + "Account Disactivated successfully ...", Toasty.LENGTH_LONG).show ();
                           }
                       })
                       .addOnFailureListener (new OnFailureListener () {
                           @Override
                           public void onFailure (@NonNull Exception e) {
                               Toasty.error (context, "" + e.getMessage (), Toasty.LENGTH_LONG).show ();
                           }
                       });

            }

    private void deleteAccount (int position, final String person_Id) {


                DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
                ref.child (person_Id)
                        .removeValue ()
                        .addOnSuccessListener (new OnSuccessListener<Void> () {
                            @Override
                            public void onSuccess (Void aVoid) {
                                //group left successfully
                                Toasty.success (context, "" + "Account Deleted successfully ...", Toasty.LENGTH_LONG).show ();

                            }
                        })
                        .addOnFailureListener (new OnFailureListener () {
                            @Override
                            public void onFailure (@NonNull Exception e) {
                                Toasty.error (context, "" + e.getMessage (), Toasty.LENGTH_LONG).show ();
                            }
                        });

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
