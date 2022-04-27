package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easyshopping.R;
import com.easyshopping.ShopDetailsActivity;

import java.util.ArrayList;

import Models.ModelCartItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {
    private Context context;
    private ArrayList<ModelCartItem> cartItems;


    public AdapterCartItem (Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_cartitem, parent,false);
        return new HolderCartItem (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderCartItem holder, final int position) {
        //get Data
        ModelCartItem modelCartItem = cartItems.get(position);
        final String id, getpId, title, cost, price, quantity;
        id = modelCartItem.getId ();
        getpId=modelCartItem.getpId ();
        title=modelCartItem.getName();
        cost = modelCartItem.getCost ();
        price = modelCartItem.getPrice ();
        quantity = modelCartItem.getQuantity ();

        //Set Data
        holder.itemTitleTv.setText (""+title);
        holder.itemPriceTv.setText (""+cost);
        holder.itemQuantityTv.setText (""+quantity); //e.g. [3]
        holder.itemPriceEachTv.setText (""+price);

        //Handle remove click listener, delete item from cat
        holder.itemRemoveTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //Will create table if not exists, but in that cast will must exist
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName ("ITEMS_TABLE")
                        .addColumn (new Column ("Item_Id", new String []{"text","unique"}))
                        .addColumn (new Column ("Item_PID", new String []{"text","not null"}))
                        .addColumn (new Column ("Item_Name", new String []{"text","not null"}))
                        .addColumn (new Column ("Item_Price_Each", new String []{"text","not null"}))
                        .addColumn (new Column ("Item_Price", new String []{"text","not null"}))
                        .addColumn (new Column ("Item_Quantity", new String []{"text","not null"}))
                        .doneTableColumn ();
                easyDB.deleteRow (1,id);
                showToastSuccess ("Removed from cart ......");

                //Refresh list
                cartItems.remove (position);
                notifyItemChanged (position);
                notifyDataSetChanged ();

                double tx = Double.parseDouble ((((ShopDetailsActivity)context).allTotalPriceTv.getText ().toString ().trim ().replace ("SYP","")));
                double totalPrice = tx - Double.parseDouble (cost.replace ("S.P",""));
                double deliveryFee = Double.parseDouble ((((ShopDetailsActivity)context).dFeeTv.getText ().toString ().trim ().replace ("SYP","")));
                double sTotalPrice = Double.parseDouble (String.format ("%.2f",totalPrice))-Double.parseDouble(String.format ("%.2f",deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice = 0.00;
                ((ShopDetailsActivity)context).sTotalTv.setText (String.format ("%.2f", sTotalPrice)+"SYP");
                ((ShopDetailsActivity)context).allTotalPriceTv.setText (String.format ("%.2f", sTotalPrice)+"SYP");

                //after removing item from cart updat cart count
                ((ShopDetailsActivity)context).cartCount ();



            }
        });

    }

    @Override
    public int getItemCount () {
        return cartItems.size ();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        //ui views of row_cartitem.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;
        public HolderCartItem(@NonNull View itemView){
            super(itemView);
            itemTitleTv = itemView.findViewById (R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById (R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById (R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById (R.id.itemQuantityTv);
            itemRemoveTv= itemView.findViewById (R.id.itemRemoveTv);
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
