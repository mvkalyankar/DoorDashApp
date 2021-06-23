package com.example.doordashapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doordashapp.FilterFoodUser;
import com.example.doordashapp.R;
import com.example.doordashapp.activities.ShopDetailsActivity;
import com.example.doordashapp.models.ModelFood;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterFoodUser extends RecyclerView.Adapter<AdapterFoodUser.HolderFoodUser> implements Filterable {

    private Context context;
    public ArrayList<ModelFood> foodsList, filterList;
    private FilterFoodUser filter;

    public AdapterFoodUser(Context context, ArrayList<ModelFood> foodsList) {
        this.context = context;
        this.foodsList = foodsList;
        this.filterList = foodsList;
    }

    @NonNull
    @Override
    public HolderFoodUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_food_user, parent, false);//creating view of product seller
        return new HolderFoodUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFoodUser.HolderFoodUser holder, int position) {

        ModelFood modelFood = foodsList.get(position);

        String discountAvailable = modelFood.getDiscountAvailable();
        String discountNote = modelFood.getDiscountNote();
        String discountPrice = modelFood.getDiscountPrice();
        String originalPrice = modelFood.getOriginalPrice();
        String foodDescription = modelFood.getFoodDescription();
        String foodTitle = modelFood.getFoodTitle();
        String foodId = modelFood.getFoodId();
        String foodIcon = modelFood.getFoodIcon();
        String timestamp = modelFood.getTimestamp();

        //set data
        holder.titleTv.setText(foodTitle);
        holder.descriptionTv.setText(foodDescription);
        holder.discountNoteTv.setText(discountNote);
        holder.discountPriceTv.setText("$" + discountPrice);
        holder.originalPriceTv.setText("$" + originalPrice);

        if (discountAvailable.equals("true")) {
            //food is on discount
            holder.discountPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price

        } else {
            //food is not on discount
            holder.discountPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }

        try {
            Picasso.get().load(foodIcon).placeholder(R.drawable.ic_add_shopping_red).into(holder.foodIconIv);
        } catch (Exception e) {
            holder.foodIconIv.setImageResource(R.drawable.ic_add_shopping_red);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product to cart
                showQuantityDialog(modelFood);

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  show item details
                //detailsBottomSheet(modelProduct);//here modelproduct contains clicked product details
            }
        });

    }

    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;

    private void showQuantityDialog(ModelFood modelFood) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);

        //init layput views from dialog_quantity.xml
        ImageView foodIv = view.findViewById(R.id.foodIv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView fQuantityTv = view.findViewById(R.id.fQuantityTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalPriceTv = view.findViewById(R.id.finalTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model
        String foodId = modelFood.getFoodId();
        String discountNote = modelFood.getDiscountNote();
        String foodDescription = modelFood.getFoodDescription();
        String foodQuantity = modelFood.getFoodQuantity();
        String foodTitle = modelFood.getFoodTitle();
        String foodIcon = modelFood.getFoodIcon();

        final String price;
        if (modelFood.getDiscountAvailable().equals("true")) {
            //food has discount
            price = modelFood.getDiscountPrice();
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price

        } else {
            price = modelFood.getOriginalPrice();
            priceDiscountedTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
            originalPriceTv.setPaintFlags(0);

        }

        cost = Double.parseDouble(price.replaceAll("$", ""));
        finalCost = Double.parseDouble(price.replaceAll("$", ""));
        ;
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //set view to dialog
        builder.setView(view);
        //set data
        try {
            Picasso.get().load(foodIcon).placeholder(R.drawable.ic_cart_gray).into(foodIv);
        } catch (Exception e) {
            foodIv.setImageResource(R.drawable.ic_cart_gray);
        }

        titleTv.setText("" + foodTitle);
        descriptionTv.setText("" + foodDescription);
        fQuantityTv.setText("" + foodQuantity);
        discountNoteTv.setText("" + discountNote);
        quantityTv.setText("" + quantity);
        originalPriceTv.setText("$" + modelFood.getOriginalPrice());
        priceDiscountedTv.setText("$" + modelFood.getDiscountPrice());
        finalPriceTv.setText("$" + finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalCost = finalCost + cost;
                quantity++;

                finalPriceTv.setText("$" + finalCost);
                quantityTv.setText("" + quantity);
            }
        });

        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 1) {
                    finalCost = finalCost - cost;
                    quantity--;

                    finalPriceTv.setText("$" + finalCost);
                    quantityTv.setText("" + quantity);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPriceTv.getText().toString().trim().replace("$", "");
                String quantity = quantityTv.getText().toString().trim();

                //add  to db(SQLite)
                addToCart(foodId, title, priceEach, totalPrice, quantity);

                dialog.dismiss();
            }
        });


    }

    private int itemId = 1;

    private void addToCart(String foodId, String title, String priceEach, String price, String quantity) {

        itemId++;
        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_FID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id", itemId)
                .addData("Item_FID", foodId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to Cart......", Toast.LENGTH_SHORT).show();

        //update cart count
        ((ShopDetailsActivity) context).cartCount();

    }

    @Override
    public int getItemCount() {
        return foodsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterFoodUser(this, filterList);
        }
        return filter;
    }

    class HolderFoodUser extends RecyclerView.ViewHolder {

        //holds views of recyclerview from row_food_user.cml
        private ImageView foodIconIv;
        private TextView discountNoteTv, titleTv, descriptionTv, addToCartTv, discountPriceTv, originalPriceTv;

        public HolderFoodUser(@NonNull View itemView) {
            super(itemView);

            // init UI views
            foodIconIv = itemView.findViewById(R.id.foodIconIv);
            discountNoteTv = itemView.findViewById(R.id.discountNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            discountPriceTv = itemView.findViewById(R.id.discountPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }
}
