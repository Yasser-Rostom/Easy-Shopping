package Adapters;

import android.app.AlertDialog;
import android.content.Context;
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

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    Context context;
    public ArrayList<ModelProducts> productsList, filterList;
    private FilterProdcutUser filter;

    public AdapterProductUser (Context context, ArrayList<ModelProducts> productsList) {
        this.context = context;
        this.productsList=productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_product_user, parent, false);
        return new AdapterProductUser.HolderProductUser (view);
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

        holder.addToCartTV.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //add product  to cart
                showQuantityDialog(modelProducts);
            }
        });
        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //show product Details

            }
        });

    }

    private double cost = 0;
    private double finalCost =0;
    private int quantity = 0;

    private void showQuantityDialog (ModelProducts modelProducts) {
        //Inflate layout for dialog
        View view = LayoutInflater.from (context).inflate (R.layout.dialog_quantity, null);
        // init layout views
        ImageView productIv = view.findViewById (R.id.QproductIV);
        final TextView titleTv = view.findViewById (R.id.QtitleTv);
        TextView pQuantityTv = view.findViewById (R.id.QquantityTv);
        TextView descriptionTv = view.findViewById (R.id.QdescriptionTv);
        final TextView finalPriceTv = view.findViewById (R.id.finalPriceTv);
        TextView discountedNoteTv = view.findViewById (R.id.QdiscountedNoteTv);
        TextView priceDiscountedTv = view.findViewById (R.id.QpriceDiscountedTv);
        final TextView originalPriceTv = view.findViewById (R.id.QoriginalPriceTv);
        ImageButton decrementBtn = view.findViewById (R.id.minusBtn);
        ImageButton incrementBtn = view.findViewById (R.id.incrementBtn);
        final TextView quantityTv = view.findViewById (R.id.wantedQuantityTv);
        TextView continueBtn = view.findViewById (R.id.addToCartBtn);

        //Get data from model products
        final String productId = modelProducts.getProductId ();
        String title = modelProducts.getProductTitle ();
        final String productQuantity = modelProducts.getProductQuantity ();
        String description = modelProducts.getProductDescription ();
        String discountedNote = modelProducts.getDiscountNote ();
        String Image = modelProducts.getProductIcon ();
        final String price;
        if (modelProducts.getDiscountAvailable ().equals ("true")){
            //Product have discount
            price = modelProducts.getDiscountPrice ();
            discountedNoteTv.setVisibility (View.VISIBLE);
            originalPriceTv.setPaintFlags (originalPriceTv.getPaintFlags ()| Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
                  }
        else{
            //Product don't have discount
            price = modelProducts.getOriginalPrice ();
            discountedNoteTv.setVisibility (View.GONE);
            priceDiscountedTv.setVisibility (View.GONE);
            originalPriceTv.setPaintFlags (0);
        }
        cost = Double.parseDouble (price.replaceAll ("S.P", ""));
        finalCost = Double.parseDouble (price.replaceAll ("S.P",""));
        quantity = 1;
        //Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder (context);
        builder.setView (view);

        //setData
        try {
            Picasso.get ().load (Image).placeholder (R.drawable.ic_shopping_cart).into (productIv);
        }catch (Exception e){
            productIv.setImageResource (R.drawable.ic_shopping_cart);
        }
        titleTv.setText (title);
        pQuantityTv.setText (""+ productQuantity);
        descriptionTv.setText (description);
        discountedNoteTv.setText (discountedNote);
        quantityTv.setText (""+ quantity);
        originalPriceTv.setText (""+ modelProducts.getOriginalPrice ());
        priceDiscountedTv.setText (""+ modelProducts.getDiscountPrice ());
        finalPriceTv.setText (""+ finalCost);

        final AlertDialog dialog = builder.create ();
        dialog.show ();

        //increae quantity
        incrementBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                if (quantity > (Integer.parseInt (productQuantity))-1){
                    Toasty.warning (context, "You can't Choose more than existing quantity",Toasty.LENGTH_LONG).show ();
                }
                else {
               finalCost = finalCost + cost;
               quantity ++;
                quantityTv.setText (quantity+"");
                finalPriceTv.setText (finalCost+"");
            }
            }
        });
        decrementBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                if (quantity < 2){
                    Toasty.warning (context, "You can't decrement less than 1",Toasty.LENGTH_LONG).show ();
                }else{

                finalCost = finalCost - cost;
                quantity --;
                quantityTv.setText (quantity+"");
                finalPriceTv.setText (finalCost+"");
            }}
        });

        continueBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                String title = titleTv.getText ().toString ().trim ();
                String priceEach = price;
                String totalPrice = finalPriceTv.getText ().toString ().trim ();
                String quantity = quantityTv.getText ().toString ().trim ();

                //add to db (SQlite)
                addToCart(productId, title, priceEach,totalPrice,quantity);
                dialog.dismiss ();
            }
        });


    }

    private int itemId = 1;
    private void addToCart (String productId, String title, String priceEach, String price, String quantity) {
    itemId++;
        EasyDB easyDB = EasyDB.init (context, "ITEMS_DB")
                .setTableName ("ITEMS_TABLE")
                .addColumn (new Column ("Item_Id", new String[] {"text", "unique"}))
                .addColumn (new Column ("Item_PID", new String[] {"text", "not null"}))
                .addColumn (new Column ("Item_Name", new String[] {"text", "not null"}))
                .addColumn (new Column ("Item_Price_Each", new String[] {"text", "not null"}))
                .addColumn (new Column ("Item_Price", new String[] {"text", "not null"}))
                .addColumn (new Column ("Item_Quantity", new String[] {"text", "not null"}))
                .doneTableColumn ();
        Boolean b = easyDB.addData ("Item_Id" , itemId)
                .addData ("Item_PID",productId)
                .addData ("Item_Name",title)
                .addData ("Item_Price_Each",priceEach)
                .addData ("Item_Price",price)
                .addData ("Item_Quantity",quantity)
                .doneDataAdding ();
        //To manifest add this attribute <activity android:name=".ShopDetailsActivity"
        //            android:windowSoftInputMode="stateHidden"/>
        Toasty.success (context,"Added.....", Toasty.LENGTH_LONG).show ();

        //update cart cunt;
        ((ShopDetailsActivity)context).cartCount ();//also we must add this line in adapterCartItem.java
    }

    @Override
    public int getItemCount () {
        return productsList.size ();
    }

    @Override
    public Filter getFilter () {
        if (filter == null){
            filter = new FilterProdcutUser(this, filterList);
        }
        return filter;
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
            addToCartTV = itemView.findViewById (R.id.addToCartTv);
            discounntedPriceTv = itemView.findViewById (R.id.rowDiscountedPriceTv);
            originalPriceTv = itemView.findViewById (R.id.rowOriginalPriceTv);


        }


}
}