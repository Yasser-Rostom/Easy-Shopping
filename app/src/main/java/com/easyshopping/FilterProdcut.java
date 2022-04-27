package com.easyshopping;

import android.widget.Filter;

import java.util.ArrayList;

import Adapters.AdapterProductSeller;
import Models.ModelProducts;

public class FilterProdcut extends Filter {

    private AdapterProductSeller adapter;
    private ArrayList<ModelProducts> filterList;

    public FilterProdcut (AdapterProductSeller adapter, ArrayList<ModelProducts> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering (CharSequence constraint) {
       FilterResults results = new FilterResults ();
       //validate data for search query
        if (constraint != null && constraint.length () >0){
            //search filed not empty, searching something perform search
            //change to upper case to make case insensitive
            constraint = constraint.toString ().toUpperCase ();
            //store our filtered list
            ArrayList<ModelProducts> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size (); i++){
                //check search by title and category
                if (filterList.get (i).getProductTitle ().toUpperCase ().contains (constraint)||
                filterList.get (i).getProductCategory ().toUpperCase ().contains (constraint)) {
                    //add filtered data to list
                    filteredModels.add (filterList.get (i));
                }
            }
            results.count=filteredModels.size ();
            results.values = filteredModels;
        }
        else{

            //search filed empty , not searching, return  original/all/comlete list
            results.count=filterList.size ();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults (CharSequence constraint, FilterResults results) {
        adapter.productsArrayList = (ArrayList<ModelProducts>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged ();

    }
}
