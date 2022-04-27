package Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyshopping.FilterOrderShop;
import com.easyshopping.OrderDetailsSellerActivity;
import com.easyshopping.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import Models.ModelOrder;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.HolderOrder> implements Filterable {
    private Context context;
    public ArrayList<ModelOrder> orderArrayList,filterList;

    private FilterOrderShop filter;

    public AdapterOrder (Context context, ArrayList<ModelOrder> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
        this.filterList = orderArrayList;
    }

    @NonNull
    @Override
    public HolderOrder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_orders, parent,false);
        return new HolderOrder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderOrder holder, int position) {
        //get data at position
        ModelOrder modelOrder = orderArrayList.get (position);
        final String orderId = modelOrder.getOrderId ();
        final String orderBy = modelOrder.getCustomer_Id ();
        String orderCost = modelOrder.getTotal_amount ();
        String orderStatus = modelOrder.getDelivery_state ();
        String orderTime = modelOrder.getDate ();
        String orderTo = modelOrder.getOrderTo ();

        //load user/byer info
        loadUserInfo(modelOrder, holder);

        //Set Data
        holder.amountTv.setText ("Amount: SYP " + orderCost);
        holder.statusTv.setText (orderStatus);
        holder.orderIdtv.setText ("Order ID : "+ orderId);
        //change order status text color
        if (orderStatus.equals ("In Progress")){
            holder.statusTv.setTextColor (context.getResources ().getColor (R.color.colorPrimary));
        }
        else if (orderStatus.equals ("Completed")){
            holder.statusTv.setTextColor (context.getResources ().getColor (R.color.colorGreen));
        }
        else if (orderStatus.equals ("Cancelled")){
            holder.statusTv.setTextColor (context.getResources ().getColor (R.color.colorblack));
        }
        //convert time to proper format: e.g. dd/mm/yyyy
        Calendar calendar = Calendar.getInstance ();
        calendar.setTimeInMillis (Long.parseLong (orderTime));
        String formatedDate = DateFormat.format ("dd/MM/yyyy",calendar).toString ();
        holder.orderDateTv.setText(formatedDate);
        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //open order details
                Intent intent = new Intent (context, OrderDetailsSellerActivity.class);
                intent.putExtra ("orderId", orderId);//To load order information
                intent.putExtra ("orderBy", orderBy);//to load info of the user who placed order
                context.startActivity (intent);
            }
        });
    }

    private void loadUserInfo (ModelOrder modelOrder, final HolderOrder holder) {
        // to load email of the user / buyer: modelorder.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref= FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (modelOrder.getCustomer_Id ())
                .addValueEventListener (new ValueEventListener () {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                    String email = String.valueOf (dataSnapshot.child ("email").getValue ());
                    holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public int getItemCount () {
        return orderArrayList.size ();
    }

    @Override
    public Filter getFilter () {
      if (filter == null){
          //init filter
          filter = new FilterOrderShop (this,filterList);
      }
        return filter;
    }


    //View holder class for row_order_seller.xml
    class HolderOrder extends RecyclerView.ViewHolder{
        //ui view of row_order_seller.xml
        private TextView orderIdtv, orderDateTv,emailTv,amountTv,statusTv;
        private ImageView orderNextIv;
        public HolderOrder(@NonNull View itemView){
            super(itemView);
            //init ui views
            orderIdtv = itemView.findViewById (R.id.orderIdTV);
            orderDateTv = itemView.findViewById (R.id.orderDateTv);
            emailTv = itemView.findViewById (R.id.emailTv);
            amountTv = itemView.findViewById (R.id.orderAmountTv);
            statusTv = itemView.findViewById (R.id.statusTv);
            orderNextIv =itemView.findViewById (R.id.orderNextIv);
        }
    }
}
