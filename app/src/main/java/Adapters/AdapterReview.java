package Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.easyshopping.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import Models.ModelReview;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview>{
    private Context context;
    private ArrayList<ModelReview> reviewArrayList;

    public AdapterReview (Context context, ArrayList<ModelReview> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_reviews, parent,false);
        return new AdapterReview.HolderReview (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderReview holder, int position) {
        //get data at position
        ModelReview modelReview = reviewArrayList.get (position);
        String uid = modelReview.getUid ();
        String ratings = modelReview.getRatings ();
        String timestamp = modelReview.getTimestamp ();
        String review =modelReview.getReview ();

        //we also need info (profile image , name) of user who wrote the review: we can do it using uid of user
        loadUserDetails(modelReview, holder);

        //convert timestamp to proper dd/MM/yyyy
        Calendar calendar = Calendar.getInstance ();
        calendar.setTimeInMillis (Long.parseLong (timestamp));
        String dateFormat = DateFormat.format ("dd/MM/yyyy",calendar).toString ();

        //set Data
        holder.ratingBar.setRating (Float.parseFloat (ratings));
        holder.reviewTv.setText (review);
        holder.dateTv.setText (dateFormat);

    }

    private void loadUserDetails (final ModelReview modelReview, final HolderReview holder) {
        //uid of user who wrote review
        String uid = modelReview.getUid ();
        DatabaseReference ref= FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (uid)
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

//                        loadCustomer (modelReview,holder);
                        String profileImage = String.valueOf (dataSnapshot.child ("profileImage").getValue ());
                        String fname = String.valueOf (dataSnapshot.child("first_name").getValue ());
                        String lname = String.valueOf (dataSnapshot.child ("last_name").getValue ());
                        String name = fname + " " + lname;

                        holder.nameTv.setText (name);

                        //set data

                        try {
                            Picasso.get ().load (profileImage).placeholder (R.drawable.ic_person_gray).into (holder.profileIv);
                        }catch (Exception e){
                            // if there is no image set default one
                            holder.profileIv.setImageResource (R.drawable.ic_person_gray);
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }
    public void loadCustomer(ModelReview modelReview, final HolderReview holder){
        //uid of user who wrote review
        String uid = modelReview.getUid ();
        DatabaseReference ref= FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child ("Customer")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                        //get user info, use same key names as in firebase
                        String fname = String.valueOf (dataSnapshot.child ("first_name").getValue ());
                        String lname = String.valueOf (dataSnapshot.child ("last_name").getValue ());
                        String name = fname + " " + lname;

                        holder.nameTv.setText (name);

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount () {
        return reviewArrayList.size ();
    }

    //View holder class, holds/inits vies of recyclerView
    class HolderReview extends RecyclerView.ViewHolder{
        //ui view fo layout row_review

        private ImageView profileIv;
        private TextView nameTv, dateTv, reviewTv;
        private RatingBar ratingBar;

        public HolderReview (@NonNull View itemView) {
            super (itemView);

            //init views of row_review
            profileIv = itemView.findViewById (R.id.rowReviewprofileIv);
            nameTv = itemView.findViewById (R.id.nameTv);
            dateTv = itemView.findViewById (R.id.dateTv);
            reviewTv = itemView.findViewById (R.id.reviewTv);
            ratingBar = itemView.findViewById (R.id.ratingBar);

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
