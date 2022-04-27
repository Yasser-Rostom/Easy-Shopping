package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyshopping.FilterProdcutUser;
import com.easyshopping.R;
import com.easyshopping.ShopDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Models.ModelProducts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterSuggestions extends RecyclerView.Adapter<AdapterSuggestions.HolderProductUser> {

    Context context;
    public ArrayList<ModelProducts> productsList;


    public AdapterSuggestions (Context context, ArrayList<ModelProducts> productsList) {
        this.context = context;
        this.productsList=productsList;

    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_suggestions, parent, false);
        return new AdapterSuggestions.HolderProductUser (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderProductUser holder, int position) {
        //getData
        final ModelProducts modelProducts = productsList.get (position);
        String discountAvailable = modelProducts.getDiscountAvailable ();
        String discountNote = modelProducts.getDiscountNote ();
        String discountPrice = modelProducts.getDiscountPrice ();
        String originalPrice = modelProducts.getOriginalPrice ();
        String productDescription = modelProducts.getProductDescription ();
        String productCategory = modelProducts.getProductCategory ();
        String productTitle = modelProducts.getProductTitle ();
        String productQuantity = modelProducts.getProductQuantity ();
        String productId = modelProducts.getProductId ();
        String timestamp = modelProducts.getTimestamp ();
        String productIcon = modelProducts.getProductIcon ();
        final String seller_Id = modelProducts.getPerson_Id ();

        //Set Data
        holder.titleTv.setText(productTitle);
        holder.discountedNote.setText(discountNote);
        holder.discriptionTv.setText(productDescription);
        holder.discounntedPriceTv.setText("SYP" + discountPrice);
        holder.originalPriceTv.setText("SYP" + originalPrice);
        if (discountAvailable.equals ("true")){
            //product is on discount
            holder.discounntedPriceTv.setVisibility (View.VISIBLE);
            holder.discountedNote.setVisibility (View.VISIBLE);
            holder.originalPriceTv.setPaintFlags (holder.originalPriceTv.getPaintFlags ()| Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        }
        else {
            //product is not on discount
            holder.discounntedPriceTv.setVisibility (View.GONE);
            holder.discountedNote.setVisibility (View.GONE);
        }

        try{
            Picasso.get ().load (productIcon).placeholder (R.drawable.ic_add_shopping_red).into (holder.productIconIv);

        }catch (Exception e){
            holder.productIconIv.setImageResource (R.drawable.ic_add_shopping_red);
        }

        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent (context, ShopDetailsActivity.class);
                intent.putExtra ("shopUid", seller_Id);
                context.startActivity (intent);
            }
                });

    }




    @Override
    public int getItemCount () {
        return productsList.size ();
    }

       //HolderProductUser
    class HolderProductUser extends RecyclerView.ViewHolder {


        //View
        private ImageView productIconIv;
        private TextView discountedNote, titleTv, discriptionTv, addToCartTV, discounntedPriceTv,originalPriceTv;

        public HolderProductUser (@NonNull View itemView) {
            super (itemView);
            //init ui views
            productIconIv = itemView.findViewById (R.id.rowUserproductIconIv);
            discountedNote = itemView.findViewById (R.id.rowdiscountedNoteEt);
            titleTv = itemView.findViewById (R.id.rowtitleTv);
            discriptionTv = itemView.findViewById (R.id.descriptionTv);
            discounntedPriceTv = itemView.findViewById (R.id.rowDiscountedPriceTv);
            originalPriceTv = itemView.findViewById (R.id.rowOriginalPriceTv);


        }


}
}