package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.easyshopping.EditProductActivity;
import com.easyshopping.FilterProdcut;
import com.easyshopping.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Models.ModelProducts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelProducts> productsArrayList,filterList;
    private FilterProdcut filter;


    public AdapterProductSeller (Context context, ArrayList<ModelProducts> productsArrayList) {
        this.context = context;
        this.productsArrayList = productsArrayList;
        this.filterList = productsArrayList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.row_product_seller, parent, false);
        return new HolderProductSeller (view);
    }

    @Override
    public void onBindViewHolder (@NonNull HolderProductSeller holder, int position) {
        //getData
        final ModelProducts modelProducts = productsArrayList.get (position);
      String id = modelProducts.getProductId ();
        String uid = modelProducts.getUid ();
        String discountAvailable = modelProducts.getDiscountAvailable ();
        String discountNote = modelProducts.getDiscountNote ();
        String discountPrice = modelProducts.getDiscountPrice ();
        String productCategory = modelProducts.getProductCategory ();
        String productDescription = modelProducts.getProductDescription ();
        String icon = modelProducts.getProductIcon ();
        String quantity = modelProducts.getProductQuantity ();
        String title = modelProducts.getProductTitle ();
        String timestamp = modelProducts.getTimestamp ();
        String originalPrice = modelProducts.getOriginalPrice ();

        //Set Data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("SYP" + discountPrice);
        holder.originalPriceTv.setText("SYP" + originalPrice);
        if (discountAvailable.equals ("true")){
            //product is on discount
            holder.discountedPriceTv.setVisibility (View.VISIBLE);
            holder.discountNoteTv.setVisibility (View.VISIBLE);
            holder.originalPriceTv.setPaintFlags (holder.originalPriceTv.getPaintFlags ()| Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        }
        else {
            //product is not on discount
            holder.discountedPriceTv.setVisibility (View.GONE);
            holder.discountNoteTv.setVisibility (View.GONE);
            holder.originalPriceTv.setPaintFlags (0);
        }

        try{
            Picasso.get ().load (icon).placeholder (R.drawable.ic_add_shopping_red).into (holder.productIconIv);

        }catch (Exception e){
            holder.productIconIv.setImageResource (R.drawable.ic_add_shopping_red);
        }

        holder.itemView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //handle item clicks, sho item details
                //handle item clicks show item details (in bottom  sheet)
                detailsBottomSheet(modelProducts);//Here modelProduct contains details of clicked product

            }
        });

    }

    private void detailsBottomSheet (ModelProducts modelProducts) {
        //bottom sheet
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog (context);
        //inflate view for bottomSheet
        View view = LayoutInflater.from (context).inflate (R.layout.bs_product_details_seller, null);
        //set View to bottomsheet
        bottomSheetDialog.setContentView (view);


        //init views bottomsheet
        ImageButton backBtn = view.findViewById (R.id.bsBackBtn);
        ImageButton deleteBtn = view.findViewById (R.id.bsDeleteBtn);
        ImageButton editBtn = view.findViewById (R.id.bsEditBtn);
        ImageView productIconIV= view.findViewById (R.id.bsProdcutIconIV);
        TextView discountNoteTv = view.findViewById (R.id.bsDiscountNoteTv);
        TextView discountedPriceTv = view.findViewById (R.id.bsDiscountedPriceTV);
        TextView titleTv = view.findViewById (R.id.bsTitle);
        TextView descriptionTv = view.findViewById (R.id.bsDescriptionTv);
        TextView categoryTv = view.findViewById (R.id.bsCategoryTv);
        TextView QuantityTV = view.findViewById (R.id.bsCategoryTv);
        TextView originalPriceTv = view.findViewById (R.id.bsOriginalPriceTv);

        final String product_Id = modelProducts.getProductId ();
        String seller_Id = modelProducts.getUid ();
        String discountAvailable = modelProducts.getDiscountAvailable ();
        String discountNote = modelProducts.getDiscountNote ();
        String discountPrice = modelProducts.getDiscountPrice ();
        String productCategory = modelProducts.getProductCategory ();
        String productDescription = modelProducts.getProductDescription ();
        String icon = modelProducts.getProductIcon ();
        String quantity = modelProducts.getProductQuantity ();
        final String title = modelProducts.getProductTitle ();
        String timestamp = modelProducts.getTimestamp ();
        String originalPrice = modelProducts.getOriginalPrice ();

        //set Data
        titleTv.setText (title);
        descriptionTv.setText (productDescription);
        categoryTv.setText (productCategory);
        QuantityTV.setText (quantity);
        discountNoteTv.setText (discountNote);
        discountedPriceTv.setText (discountPrice);
        originalPriceTv.setText (originalPrice);
        if(discountAvailable.equals ("true")){
            //product is on discount
            discountedPriceTv.setVisibility (View.VISIBLE);
            discountNoteTv.setVisibility (View.VISIBLE);
            originalPriceTv.setPaintFlags (originalPriceTv.getPaintFlags () | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else {
            //product is not on discount
            discountedPriceTv.setVisibility (View.GONE);
            discountNoteTv.setVisibility (View.GONE);
                    }

        try{
            Picasso.get ().load (icon).placeholder (R.drawable.ic_add_shopping_red).into (productIconIV);
        }catch (Exception e){
            productIconIV.setImageResource (R.drawable.ic_add_shopping_red);
        }
        //Show Bottom Dialog
        bottomSheetDialog.show ();

        //Edit Dialog
        editBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                bottomSheetDialog.dismiss ();
                //Open edit product activity
                Intent intent = new Intent (context, EditProductActivity.class);
                intent.putExtra("productId", product_Id);
                context.startActivity (intent);
            }
        });
        deleteBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                bottomSheetDialog.dismiss ();
               //Show delete Confirm
                AlertDialog.Builder builder = new AlertDialog.Builder (context);
                builder.setTitle ("Delete")
                        .setMessage ("Are you sure you want to delete product" + title + " ?")
                        .setPositiveButton ("DELETE", new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                //Delete
                                deleteProduct(product_Id);//id is the product id
                            }
                        })
                        .setNegativeButton ("NO", new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick (DialogInterface dialog, int which) {
                                //Cancel, dismiss dialog
                                dialog.dismiss ();
                            }
                        })
                        .show ();
            }
        });
        backBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                bottomSheetDialog.dismiss ();
            }
        });
    }

    private void deleteProduct (String id) {
        //Delete product using its id
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance ();
        DatabaseReference ref = FirebaseDatabase.getInstance ().getReference ("Persons");
        ref.child (firebaseAuth.getUid ()).child ("Products").child (id).removeValue ()
                .addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess (Void aVoid) {
                        //Product had been deleted
                        showToastSuccess ("Product has been deleted.....");

                    }
                })
                .addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure (@NonNull Exception e) {
                    //Failed to delete product
                    showToastError ("Failed to delete Prodcut : " + e.getMessage ());
                    }
                });

    }

    @Override
    public int getItemCount () {

        return productsArrayList.size ();
    }

    @Override
    public Filter getFilter () {
        if(filter == null){
            filter = new FilterProdcut ( this,filterList);
        }
        return filter;
    }


    class HolderProductSeller extends RecyclerView.ViewHolder{
        //holds views of recyclerView
        private ImageView productIconIv;
        private TextView discountNoteTv, titleTv, quantityTv, discountedPriceTv, originalPriceTv;

        public HolderProductSeller (@NonNull View itemView) {

            super (itemView);
            productIconIv = itemView.findViewById (R.id.rowproductIconIv);
            titleTv = itemView.findViewById (R.id.rowtitleTv);
            quantityTv = itemView.findViewById (R.id.rowQuantityTv);
            discountedPriceTv = itemView.findViewById (R.id.rowDiscountedPriceTv);
            originalPriceTv = itemView.findViewById (R.id.rowOriginalPriceTv);
            discountNoteTv= itemView.findViewById (R.id.rowdiscountedNoteEt);


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
