package Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easyshopping.OrderDetailsUserActivity;
import com.easyshopping.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import Models.ModelOrderUser;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser>{
    private Context context;
    private ArrayList<ModelOrderUser> orderUserList;

    public AdapterOrderUser (Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from (context).inflate (R.layout.row_order_user, parent, false);
        return new HolderOrderUser (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderOrderUser holder, int position) {
        //get data
        ModelOrderUser modelOrderUser = orderUserList.get (position);
        final String orderId = modelOrderUser.getOrderId ();
        String orderBy = modelOrderUser.getCustomer_Id ();
        String orderCost = modelOrderUser.getTotal_amount ();
        String orderStatus = modelOrderUser.getDelivery_state ();
        String orderTime = modelOrderUser.getDate ();
        final String orderTo = modelOrderUser.getOrderTo ();

        //get shop info
        loadShopInfo(modelOrderUser, holder);


        //set data
        holder.amountTv.setText ("Amount SYP :" + orderCost);
        holder.statusTv.setText (orderStatus);
        holder.orderTv.setText (" Order_ID " + orderId);
        if (orderStatus.equals ("In Progress.")){
            holder.statusTv.setTextColor (context.getResources ().getColor (R.color.colorPrimaryDark));
        }
        else if (orderStatus.equals ("Completed.")){
            holder.statusTv.setTextColor (context.getResources ().getColor (R.color.colorGreen));
        }
        else if (orderStatus.equals ("Cancelled.")){
            holder.statusTv.setTextColor (context.getResources ().getColor (R.color.colorRed));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance ();
        calendar.setTimeInMillis (Long.parseLong (orderTime));
        String formateDate = DateFormat.format ("dd/MM/yyyy",calendar).toString ();//e.g. 17/5/2020
        holder.dateTv.setText (formateDate);

        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //open order details , we need to keys there orderId - orderTo
                Intent intent = new Intent (context, OrderDetailsUserActivity.class);
                intent.putExtra ("orderTo", orderTo);
                intent.putExtra ("orderId", orderId);
                context.startActivity (intent);

            }

        });
    }

    private void loadShopInfo (ModelOrderUser modelOrderUser, final HolderOrderUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference ("Persons");
        ref.child (modelOrderUser.getOrderTo ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    String ShopName = String.valueOf (dataSnapshot.child("Seller").child ("shop_name").getValue ());
                    holder.shopNameTv.setText (ShopName);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount () {
        return orderUserList.size ();
    }

    //view holder class
    class HolderOrderUser extends RecyclerView.ViewHolder{
        //views of layout
        private TextView orderTv, dateTv, shopNameTv, amountTv, statusTv;
        public HolderOrderUser(@NonNull View itemView){
            super(itemView);

            //init views of layout
            orderTv = itemView.findViewById (R.id.orderIdTv);
            dateTv = itemView.findViewById (R.id.dateTv);
            shopNameTv = itemView.findViewById (R.id.shopNameTv);
            amountTv = itemView.findViewById (R.id.amountTv);
            statusTv = itemView.findViewById (R.id.statusTv);

        }
    }
}
