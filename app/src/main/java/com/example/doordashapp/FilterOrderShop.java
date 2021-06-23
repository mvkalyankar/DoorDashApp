package com.example.doordashapp;

import android.widget.Filter;

import com.example.doordashapp.adapters.AdapterFoodOwner;
import com.example.doordashapp.adapters.AdapterOrderShop;
import com.example.doordashapp.models.ModelFood;
import com.example.doordashapp.models.ModelOrderShop;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();//holds the result of filtering operation
        //validate data for search query
        if (constraint != null && constraint.length() > 0) {
            //search field is not empty,searching something,perform search operation
            //change to upper case and make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderShop> filteredModel = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //search by title and category
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)) {
                    //add filtered data to list
                    filteredModel.add(filterList.get(i));
                }
            }
            results.count = filteredModel.size();
            results.values = filteredModel;
        } else {
            //search field is empty,not searching,return original list/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;

    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
