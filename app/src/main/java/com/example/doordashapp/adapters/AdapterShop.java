package com.example.doordashapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doordashapp.R;
import com.example.doordashapp.activities.ShopDetailsActivity;
import com.example.doordashapp.models.ModelShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {

    private Context context;
    private ArrayList<ModelShop> shopList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);//creating view of product seller
        return new AdapterShop.HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShop.HolderShop holder, int position) {
        //get data
        ModelShop modelShop = shopList.get(position);
        String uid = modelShop.getUid();
        String email = modelShop.getEmail();
        String name = modelShop.getName();
        String shopName = modelShop.getShopName();
        String phone = modelShop.getPhone();
        String country = modelShop.getCountry();
        String state = modelShop.getState();
        String city = modelShop.getCity();
        String address = modelShop.getAddress();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String timestamp = modelShop.getTimestamp();
        String accountType = modelShop.getAccountType();
        String online = modelShop.getOnline();
        String shopOpen = modelShop.getShopOpen();
        String profileImage = modelShop.getProfileImage();

        loadReviews(modelShop, holder);  // load avg rating, set to rating bar

        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        //check if online
        if (online.equals("true")) {
            //show owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        } else {
            //owner is offline
            holder.onlineIv.setVisibility(View.GONE);
        }
        //check if shop is open
        if (shopOpen.equals("true")) {
            //show is open
            holder.shopClosedTv.setVisibility(View.GONE);
        } else {
            //show is closed
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.shopIv);
        } catch (Exception e) {
            holder.shopIv.setImageResource(R.drawable.ic_store_gray);
        }

        //handle click listener,show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });

    }

    private float ratingSum = 0;

    private void loadReviews(ModelShop modelShop, HolderShop holder) {

        String shopUid = modelShop.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // clear list before adding data into it
                        ratingSum = 0;

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating; // for avg rating, add(addition of) all ratings, later will divide it by number of reviews
                        }

                        long numberOfReviews = dataSnapshot.getChildrenCount();
                        float avgRating = ratingSum / numberOfReviews;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return shopList.size();
    }


    //view holder
    class HolderShop extends RecyclerView.ViewHolder {
        //ui views of row_shop.xml

        private ImageView shopIv, onlineIv;
        private TextView shopClosedTv, shopNameTv, phoneTv, addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            // init UI views
            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);


        }
    }
}
