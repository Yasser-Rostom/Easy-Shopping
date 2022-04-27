package Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easyshopping.R;
import com.easyshopping.ShopDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Models.ModelAddress;
import Models.ModelContact;
import Models.ModelCustomer;
import Models.ModelSeller;
import Models.ModelShops;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterShops extends RecyclerView.Adapter<AdapterShops.HolderShop> {

    private Context context;
    public ArrayList<ModelShops> shopsList;
   // public ArrayList<ModelSeller> sellerArrayList;
    //public ArrayList<ModelAddress> addressArrayList;
   // public ArrayList<ModelContact> contactArrayList;
    //public ArrayList<ModelCustomer> customerArrayList;


    public AdapterShops (Context context, ArrayList<ModelShops> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
      //  this.sellerArrayList = sellerArrayList;
      //  this.addressArrayList = addressArrayList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from (context).inflate (R.layout.row_shops, parent, false);

        return new HolderShop (view);
    }


    @Override
    public void onBindViewHolder (@NonNull HolderShop holder, int position) {
        //get data

        //animation
        holder.shopIv.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_transition_animation));
        holder.layout.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_scale_animation));
        ModelShops modelShops = shopsList.get (position);
        String firstname = modelShops.getFirst_name ();
        String lastname = modelShops.getLast_name ();
        String name = firstname + " " + lastname;
        String phone = modelShops.getPhone ();
       final String person_Id = modelShops.getPerson_Id ();
        String timestamp = modelShops.getTimestamp ();
        String accountType = modelShops.getAccountType ();
        String email = modelShops.getEmail ();
        String profileImage = modelShops.getProfileImage ();


        loadAddress (modelShops, holder);
        loadSeller (modelShops, holder);
        loadReviews(modelShops,holder);

        //Set Data
        holder.phoneTv.setText (phone);


        try{
            Picasso.get ().load (profileImage).placeholder (R.drawable.ic_store_gray).into (holder.shopIv);
        }catch (Exception e){
            Picasso.get ().load (R.drawable.ic_store_gray).into (holder.shopIv);
        }
        //Handle Click listener show shop details
        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (context, ShopDetailsActivity.class);
                intent.putExtra ("shopUid", person_Id);
                context.startActivity (intent);
;            }
        });

    }


    String online,shopOpen,sname;
    private void loadSeller (ModelShops modelShops, final HolderShop holder) {

        String person_Id = modelShops.getPerson_Id ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (person_Id).child ("Seller")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {

                           online = String.valueOf (dataSnapshot.child ("online").getValue ());
                            shopOpen = String.valueOf (dataSnapshot.child ("shop_open").getValue ());
                            sname = String.valueOf (dataSnapshot.child ("shop_name").getValue ());

                        holder.shopNameTv.setText (sname);
                        if (online.equals ("true")){
                            //shop online
                            holder.onlineIv.setVisibility (View.VISIBLE);
                        }else{
                            //Shop offline
                            holder.onlineIv.setVisibility (View.GONE);
                        }
                        //check if shop is openned
                        if (shopOpen.equals ("true")){
                            holder.shopClosedTv.setVisibility (View.GONE);
                        }
                        else{
                            holder.shopClosedTv.setVisibility (View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void loadAddress (ModelShops modelShops, final HolderShop holder) {
            String person_Id = modelShops.getPerson_Id ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (person_Id).child ("Address")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                     String address = null;
                                                 address = String.valueOf (dataSnapshot.child ("address").getValue ());
                                               holder.addressTv.setText (address);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private float ratingSum = 0;
    private void loadReviews (ModelShops modelShops, final HolderShop holder) {

        String shopUid = modelShops.getPerson_Id ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (shopUid).child ("Ratings")
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        ratingSum =0;
                        for (DataSnapshot ds: dataSnapshot.getChildren ()){
                            float rating = Float.parseFloat (String.valueOf (ds.child ("ratings").getValue ()));
                            ratingSum = ratingSum +rating;//for avg rating, add (addition all ratings, later will divide it by number of reviews

                        }
                        long numberOfReviews = dataSnapshot.getChildrenCount ();
                        float avgRating = ratingSum/numberOfReviews;

                        holder.ratingBar.setRating (avgRating);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount () {
        return shopsList.size ();

        }

    class HolderShop extends RecyclerView.ViewHolder{

        //UI views row_shop.xml
        private ImageView shopIv, onlineIv;
        private TextView shopClosedTv, shopNameTv, phoneTv, addressTv;
        private RatingBar ratingBar;

        LinearLayout layout;
        public HolderShop (@NonNull View itemView) {
            super (itemView);
            shopIv = itemView.findViewById (R.id.shopIv);
            onlineIv = itemView.findViewById (R.id.onlineIv);
            shopClosedTv = itemView.findViewById (R.id.shopClosedTv);
            shopNameTv = itemView.findViewById (R.id.rowshopNameTv);
            phoneTv = itemView.findViewById (R.id.rowPhoneTv);
            addressTv = itemView.findViewById (R.id.rowAddressTv);
            ratingBar = itemView.findViewById (R.id.ratingBar);
             layout = itemView.findViewById(R.id.container);


        }
    }
}
