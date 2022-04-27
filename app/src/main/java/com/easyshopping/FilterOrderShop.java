package com.easyshopping;

import android.widget.Filter;

import java.util.ArrayList;

import Adapters.AdapterOrder;
import Models.ModelOrder;

public class FilterOrderShop extends Filter {

    private AdapterOrder adapter;
    private ArrayList<ModelOrder> filterList;

    public FilterOrderShop (AdapterOrder adapter, ArrayList<ModelOrder> filterList) {
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
            ArrayList<ModelOrder> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size (); i++){
                //check search by title and category
                if (filterList.get (i).getDelivery_state ().toUpperCase ().contains (constraint)){
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
        adapter.orderArrayList = (ArrayList<ModelOrder>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged ();

    }
}
