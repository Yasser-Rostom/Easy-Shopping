package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyshopping.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterParticipantsAdd extends RecyclerView.Adapter<AdapterParticipantsAdd.HolderParticipantAdd>{

    private Context context;
    private ArrayList<ModelShops> userArrayList;
    private String groupId, myGroupRole; //Creator/admin/participants

    public AdapterParticipantsAdd (Context context, ArrayList<ModelShops> userArrayList, String groupId, String myGroupRole) {
        this.context = context;
        this.userArrayList = userArrayList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from (context).inflate (R.layout.row_participant_add,parent,false);
        return new AdapterParticipantsAdd.HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderParticipantAdd holder, int position) {
        //get Data
        final ModelShops modeluser = userArrayList.get (position);
        String fname = modeluser.getFirst_name ();
        String lname = modeluser.getLast_name ();
        String image = modeluser.getProfileImage ();
        String email = modeluser.getEmail ();
        final String uid = modeluser.getPerson_Id ();
        String name = fname + " " + lname;
        //set Data
        holder.nameTv.setText (name);
        holder.emailTv.setText (email);
        try{
            Picasso.get ().load (image).placeholder (R.drawable.ic_person_gray).into (holder.avatarTv);
        }catch (Exception e){
            holder.avatarTv.setImageResource (R.drawable.ic_person_gray);
        }

        //Check if this user is already exist
        checkIfAlreadyExists(modeluser, holder);

        //handle click
        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                /** check if user already added or not
                 * if added: show remove-participants/make-admin/ remove-admin option (admin will not able to change role of creator
                 * if not add , show add participant optio                 *
                 */
             DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
             ref.child (groupId).child ("Participants").child (uid)
                     .addListenerForSingleValueEvent (new ValueEventListener () {
                         @Override
                         public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                             if (dataSnapshot.exists ()){
                                 //user exists
                                 String hisPreviousRole ="" + dataSnapshot.child ("role").getValue ();

                                 //options to display in dialog
                                 String[] options;

                                 AlertDialog.Builder builder = new AlertDialog.Builder (context);
                                 builder.setTitle ("Choose Option:");
                                 if(myGroupRole.equals ("creator")){
                                     if (hisPreviousRole.equals ("admin")){
                                         //if creator he is admin
                                         options = new String[]{ "Remove Admin", "Remove User"};
                                         builder.setItems (options, new DialogInterface.OnClickListener () {
                                             @Override
                                             public void onClick (DialogInterface dialog, int which) {

                                                 //handle item clicks
                                                 if (which==0){
                                                     //Remove Admin clicked
                                                     removeAdmin(modeluser);
                                                 }
                                                 else {
                                                     //Remove user clicked
                                                     removeParticipants(modeluser);
                                                 }
                                             }
                                         }).show ();

                                     }
                                     else if (hisPreviousRole.equals ("participant")){
                                         //if creator he is participants
                                         options = new String[]{ "Make Admin", "Remove User"};
                                         builder.setItems (options, new DialogInterface.OnClickListener () {
                                             @Override
                                             public void onClick (DialogInterface dialog, int which) {

                                                 //handle item clicks
                                                 if (which==0){
                                                     //Make Admin clicked
                                                    makeAdmin(modeluser);
                                                 }
                                                 else {
                                                     //Remove user clicked
                                                     removeParticipants(modeluser);
                                                 }
                                             }
                                         }).show ();
                                     }
                                 }
                                 else if (hisPreviousRole.equals ("admin")){
                                     //im admin he is admin too
                                     options = new String[]{ "Remove Admin", "Remove User"};
                                     builder.setItems (options, new DialogInterface.OnClickListener () {
                                         @Override
                                         public void onClick (DialogInterface dialog, int which) {

                                             //handle item clicks
                                             if (which==0){
                                                 //Remove Admin clicked
                                                 removeAdmin(modeluser);
                                             }
                                             else {
                                                 //Remove user clicked
                                                 removeParticipants(modeluser);
                                             }
                                         }
                                     }).show ();

                                 }
                                 else if (hisPreviousRole.equals ("participant")){
                                     //im admin he is admin too
                                     options = new String[]{ "Make Admin", "Remove User"};
                                     builder.setItems (options, new DialogInterface.OnClickListener () {
                                         @Override
                                         public void onClick (DialogInterface dialog, int which) {

                                             //handle item clicks
                                             if (which==0){
                                                 //Remove Admin clicked
                                                 makeAdmin(modeluser);
                                             }
                                             else {
                                                 //Remove user clicked
                                                 removeParticipants(modeluser);
                                             }
                                         }
                                     }).show ();

                                 }

                             }
                             //user is not participants
                             else  {
                                AlertDialog.Builder builder = new AlertDialog.Builder (context);
                                builder.setTitle ("Add Participant")
                                        .setMessage ("Add this user in this group ?")
                                        .setPositiveButton ("Add", new DialogInterface.OnClickListener () {
                                            @Override
                                            public void onClick (DialogInterface dialog, int which) {
                                             addParticipants(modeluser);   
                                            }
                                        })
                                        .setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
                                            @Override
                                            public void onClick (DialogInterface dialog, int which) {
                                            dialog.dismiss ();
                                            }
                                        }).show ();
                             }
                         }

                         @Override
                         public void onCancelled (@NonNull DatabaseError databaseError) {

                         }
                     });
            }
        });

    }

    private void addParticipants (ModelShops modeluser) {
        //setup user Data
        String timestamp = "" + System.currentTimeMillis ();
        HashMap<String,String> hashMap = new HashMap<> ();
        hashMap.put ("person_Id",modeluser.getPerson_Id ());
        hashMap.put ("role", "participant");
        hashMap.put ("timestamp",""+timestamp);
        //add that user in Groups > groupID > participants
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants").child (modeluser.getPerson_Id ()).setValue (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //added Successfully
                        Toasty.success (context,"Participant added successfully...!", Toasty.LENGTH_LONG).show ();
                    }
                }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure (@NonNull Exception e) {
                Toasty.error (context,"Failed Add participant : "+ e.getMessage (), Toasty.LENGTH_LONG).show ();
            }
        });
    }

    private void makeAdmin (ModelShops modeluser) {
        //setup user Data
        String timestamp = "" + System.currentTimeMillis ();
        HashMap<String,Object> hashMap = new HashMap<> ();

        hashMap.put ("role", "admin");

        //add that user in Groups > groupID > participants
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants").child (modeluser.getPerson_Id ()).updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //added Successfully
                        Toasty.success (context,"Participant is now Admin...!", Toasty.LENGTH_LONG).show ();
                    }
                }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure (@NonNull Exception e) {
                Toasty.error (context,"Make Admin : "+ e.getMessage (), Toasty.LENGTH_LONG).show ();
            }
        });

    }

    private void removeParticipants (ModelShops modeluser) {
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants").child (modeluser.getPerson_Id ()).removeValue ()
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                   //Removed Successfully
                        Toasty.success (context,"This user had been removed successfully...!", Toasty.LENGTH_LONG).show ();
                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                        //Removed failed
                        Toasty.error (context,"Remove Participant : "+ e.getMessage (), Toasty.LENGTH_LONG).show ();
                    }
                });
    }

    private void removeAdmin (ModelShops modeluser) {

        HashMap<String,Object> hashMap = new HashMap<> ();

        hashMap.put ("role", "participant");

        //add that user in Groups > groupID > participants
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants").child (modeluser.getPerson_Id ()).updateChildren (hashMap)
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //added Successfully
                        Toasty.success (context,"This Admin is now normal user...!", Toasty.LENGTH_LONG).show ();
                    }
                }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure (@NonNull Exception e) {
                Toasty.error (context,"Remove Admin : "+ e.getMessage (), Toasty.LENGTH_LONG).show ();
            }
        });
    }

    private void checkIfAlreadyExists (ModelShops modeluser, final HolderParticipantAdd holder) {
        //get data
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Groups");
        ref.child (groupId).child ("Participants").child (modeluser.getPerson_Id ())
                .addListenerForSingleValueEvent (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists ()){
                            //Already exists
                            String hisRole = "" + dataSnapshot.child ("role").getValue ();
                            holder.statusTv.setText (hisRole);
                        }
                        else {
                            //Doesn't exists
                            holder.statusTv.setText ("");
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount () {
        return userArrayList.size ();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{
        private ImageView avatarTv;
        private TextView nameTv, emailTv, statusTv;

        public HolderParticipantAdd  (@NonNull View itemView){
            super(itemView);
            avatarTv = itemView.findViewById (R.id.avatarIv);
            nameTv =  itemView.findViewById (R.id.nameTv);
            emailTv =  itemView.findViewById (R.id.emailTv);
            statusTv =  itemView.findViewById (R.id.statusTv);
        }
    }
}
