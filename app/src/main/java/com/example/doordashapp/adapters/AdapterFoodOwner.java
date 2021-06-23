package com.example.doordashapp.adapters;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doordashapp.activities.EditFoodActivity;
import com.example.doordashapp.FilterFood;
import com.example.doordashapp.models.ModelFood;
import com.example.doordashapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterFoodOwner extends RecyclerView.Adapter<AdapterFoodOwner.HolderFoodOwner> implements Filterable {

    private Context context;
    public ArrayList<ModelFood> foodsList, filterList;
    private FilterFood filter;

    public AdapterFoodOwner(Context context, ArrayList<ModelFood> foodsList) {
        this.context = context;
        this.foodsList = foodsList;
        this.filterList = foodsList;
    }

    @NonNull
    @Override
    public HolderFoodOwner onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_food_owner, parent, false);//creating view of product seller
        return new HolderFoodOwner(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFoodOwner.HolderFoodOwner holder, int position) {
        //get data
        ModelFood modelFood = foodsList.get(position);
        String id = modelFood.getFoodId();
        String uid = modelFood.getUid();
        String title = modelFood.getFoodTitle();
        String foodDescription = modelFood.getFoodDescription();
        String quantity = modelFood.getFoodQuantity();
        String foodIcon = modelFood.getFoodIcon();
        String originalPrice = modelFood.getOriginalPrice();
        String discountPrice = modelFood.getDiscountPrice();
        String discountAvailable = modelFood.getDiscountAvailable();
        String discountNote = modelFood.getDiscountNote();
        String timestamp = modelFood.getTimestamp();

        //set data
        holder.titleTv.setText(title);
        holder.discountNoteTv.setText(discountNote);
        holder.quantityTv.setText(quantity);
        holder.discountPriceTv.setText("$" + discountPrice);
        holder.originalPriceTv.setText("$" + originalPrice);

        if (discountAvailable.equals("true")) {
            //product is on discount
            holder.discountPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price

        } else {
            //product is not on discount
            holder.discountPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }
        try {
            Picasso.get().load(foodIcon).placeholder(R.drawable.ic_add_shopping_red).into(holder.foodIconIv);
        } catch (Exception e) {
            holder.foodIconIv.setImageResource(R.drawable.ic_add_shopping_red);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks,show item details
                detailsBottomSheet(modelFood);//here modelFood contains clicked food details
            }
        });
    }

    private void detailsBottomSheet(ModelFood modelFood) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottomSheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_food_details_owner, null);
        //set view to bottom
        bottomSheetDialog.setContentView(view);

        //init view of the bottom sheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editProfileBtn);
        ImageView foodIconIv = view.findViewById(R.id.foodIconIv);

        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountPriceTv = view.findViewById(R.id.discountPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

        //get data
        String id = modelFood.getFoodId();
        String uid = modelFood.getUid();
        String title = modelFood.getFoodTitle();
        String foodDescription = modelFood.getFoodDescription();
        String foodIcon = modelFood.getFoodIcon();
        String originalPrice = modelFood.getOriginalPrice();
        String discountPrice = modelFood.getDiscountPrice();
        String discountAvailable = modelFood.getDiscountAvailable();
        String discountNote = modelFood.getDiscountNote();
        String timestamp = modelFood.getTimestamp();
        String quantity = modelFood.getFoodQuantity();

        //set data
        titleTv.setText(title);
        discountNoteTv.setText(discountNote);
        descriptionTv.setText(foodDescription);
        quantityTv.setText(quantity);
        discountPriceTv.setText("$" + discountPrice);
        originalPriceTv.setText("$" + originalPrice);

        if (discountAvailable.equals("true")) {
            //food is on discount
            discountPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price

        } else {
            //food is not on discount
            discountPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
            originalPriceTv.setPaintFlags(0);
        }

        try {
            Picasso.get().load(foodIcon).placeholder(R.drawable.ic_add_shopping_red).into(foodIconIv);
        } catch (Exception e) {
            foodIconIv.setImageResource(R.drawable.ic_add_shopping_red);
        }

        //show dialog
        bottomSheetDialog.show();
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                bottomSheetDialog.show();
                //open edit product activity,pass id to product
                Intent intent = new Intent(context, EditFoodActivity.class);
                intent.putExtra("foodId", id);
                context.startActivity(intent);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                bottomSheetDialog.show();
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete" + title + "?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //  delete
                                deleteProduct(id);//id is the product id
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel,dismiss dialog
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deleteProduct(String id) {
        //delete product using its id
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Food").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //product deleted
                        Toast.makeText(context, "Food Item deleted...", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodsList.size();
    }


    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterFood(this, filterList);
        }
        return filter;
    }


    //holder class
    class HolderFoodOwner extends RecyclerView.ViewHolder {
        //holds views of recyclerview
        //ui views of row_food_owner.xml
        private ImageView foodIconIv, nextIv;
        private TextView discountNoteTv, titleTv, quantityTv, discountPriceTv, originalPriceTv;

        public HolderFoodOwner(@NonNull View itemView) {
            super(itemView);

            foodIconIv = itemView.findViewById(R.id.foodIconIv);
            nextIv = itemView.findViewById(R.id.nextIv);
            discountNoteTv = itemView.findViewById(R.id.discountNoteTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            discountPriceTv = itemView.findViewById(R.id.discountPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);

        }
    }

}
