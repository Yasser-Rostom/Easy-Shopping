package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easyshopping.R;

import java.util.ArrayList;

import Models.ModelOrderedItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOderedItem>{

    Context context;
    ArrayList<ModelOrderedItem> cartItemArrayList;
    String getpId, name, cost, price, quantity;

    public AdapterOrderedItem (Context context, ArrayList<ModelOrderedItem> cartItemArrayList) {
        this.context = context;
        this.cartItemArrayList = cartItemArrayList;
    }

    @NonNull
    @Override
    public HolderOderedItem onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from (context).inflate (R.layout.row_orderitem, parent,false);
        return new HolderOderedItem (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderOderedItem holder, int position) {
    //get data at position
        ModelOrderedItem modelOrderedItem = cartItemArrayList.get(position);
        getpId = modelOrderedItem.getBill_Id ();
        name = modelOrderedItem.getItem_desc ();
        cost = modelOrderedItem.getTot_price ();
        price = modelOrderedItem.getSingle_price ();
        quantity = modelOrderedItem.getQuantity ();

        //set data
        holder.itemTitleTv.setText (name);
        holder.itemPriceEachTv.setText ("SYP " + price);
        holder.itemPriceTv.setText ("SYP " + cost);
        holder.itemQuantityTv.setText ("[ " + quantity + " ]");
    }

    @Override
    public int getItemCount () {
        return cartItemArrayList.size ();
    }


    //views holder class
    class HolderOderedItem extends RecyclerView.ViewHolder {

        //Views of row_OrderedItem.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv;

        public HolderOderedItem (@NonNull View itemView){
            super(itemView);

            //init Views
            itemTitleTv = itemView.findViewById (R.id.itemTitleTv);
            itemPriceEachTv =  itemView.findViewById (R.id.itemPriceEachPriceTv);
            itemPriceTv =  itemView.findViewById (R.id.itemPriceTv);
            itemQuantityTv = itemView.findViewById (R.id.itemQuantityTv);
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
