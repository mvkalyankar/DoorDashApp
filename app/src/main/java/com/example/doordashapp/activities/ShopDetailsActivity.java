package com.example.doordashapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doordashapp.R;
import com.example.doordashapp.adapters.AdapterCartItem;
import com.example.doordashapp.adapters.AdapterFoodUser;
import com.example.doordashapp.adapters.AdapterReview;
import com.example.doordashapp.models.ModelCartItem;
import com.example.doordashapp.models.ModelFood;
import com.example.doordashapp.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    private ImageView shopIv;
    private ImageButton backBtn, cartBtn, callBtn, mapBtn, reviewsBtn;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, cartCountTv;
    private EditText searchFoodEt;
    private RecyclerView foodsRv;

    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopLatitude, shopLongitude, shopName, shopEmail, shopAddress, shopPhone;
    public String deliveryFee;
    private RatingBar ratingBar;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelFood> foodsList;
    private AdapterFoodUser adapterFoodUser;

    //cart
    private ArrayList<ModelCartItem> cartItemsList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        // init UI views
        backBtn = findViewById(R.id.backBtn);
        cartBtn = findViewById(R.id.cartBtn);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        searchFoodEt = findViewById(R.id.searchFoodEt);
        mapBtn = findViewById(R.id.mapBtn);
        callBtn = findViewById(R.id.callBtn);
        shopIv = findViewById(R.id.shopIv);
        foodsRv = findViewById(R.id.foodsRv);
        cartCountTv = findViewById(R.id.cartCountTv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        ratingBar = findViewById(R.id.ratingBar);

        //get Uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait.....");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();
        loadShopDetails();
        loadShopFoods();
        loadReviews();  //avg rating, set on rating bar

        //declare it to class level and init it onCreate
        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_FID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //each shop has its own products and orders so if user add items to cart and go back and open cart in different shop then cart should be diff
        //delete cart data when evr user open this activity
        deleteCartData();   // before it
        cartCount();

        searchFoodEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterFoodUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCartDialog();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openMap();
            }
        });

        // handle reviewsBtn click, open reviews activity
        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pass shop uid to show its reviews
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;

    private void loadReviews() {

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

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {
        // declare it to class level and init in onCreate
        easyDB.deleteAllDataFromTable();//delete all records from cart
    }

    public void cartCount() {

        //keep it public so we can access it in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no item in cart,hide count TV
            cartCountTv.setVisibility(View.GONE);
        } else {
            //have items in cart,show cart count TV and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count);//concatenate with string,bcz we cant set int in TV

        }

    }

    private void openMap() {
//        saddr : source address
//        daddr : destination address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    public double allTotalPrice = 0.00;
    //need to view these views in adapter so make public
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;

    private void showCartDialog() {

        //init list
        cartItemsList = new ArrayList<>();

        //inflate layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);//creating view of dialog_cart
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemRv = view.findViewById(R.id.cartItemRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);
        Button paymentBtn = view.findViewById(R.id.paymentBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_FID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String fId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem("" + id,
                    "" + fId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity);

            cartItemsList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemsList);
        //set adapter
        cartItemRv.setAdapter(adapterCartItem);

        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                allTotalPrice = 0.00;
            }
        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate address
                if (myLatitude.equals("") || myLongitude.equals("") || myLongitude.equals("null") || myLatitude.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing order......", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (myPhone.equals("") || myPhone.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone number in your profile before placing order......", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cartItemsList.size() == 0) {
                    Toast.makeText(ShopDetailsActivity.this, "No items in cart", Toast.LENGTH_SHORT).show();
                    return;
                }



                submitOrder();
            }
        });

        //payment process
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cost = allTotalPriceTv.getText().toString().trim().replace("$", "");
                Intent intent = new Intent(ShopDetailsActivity.this,PaymentActivity.class);
                intent.putExtra("cost", cost);
                startActivity(intent);
                finish();
            }
        });

    }

    private void submitOrder() {
        progressDialog.setMessage("Placing order.....");
        progressDialog.show();

        //for order id and order time..
        String timestamp = "" + System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("$", ""); // remove $ if contains

        // add latitude, longitude of user to each order | delete previous orders from firebase or add manually to them

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("longitude", "" + myLongitude);

        //add to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for (int i = 0; i < cartItemsList.size(); i++) {
                            String fId = cartItemsList.get(i).getfId();
                            String id = cartItemsList.get(i).getId();
                            String name = cartItemsList.get(i).getName();
                            String cost = cartItemsList.get(i).getCost();
                            String price = cartItemsList.get(i).getPrice();
                            String quantity = cartItemsList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("fId", "" + fId);
                            hashMap1.put("name", "" + name);
                            hashMap1.put("cost", "" + cost);
                            hashMap1.put("price", "" + price);
                            hashMap1.put("quantity", "" + quantity);

                            ref.child(timestamp).child("Items").child(fId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Review Order", Toast.LENGTH_SHORT).show();

                        //after placing order ,open order details pg
//                        Intent intent = new Intent(ShopDetailsActivity.this, PaymentActivity.class);
                        Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                        intent.putExtra("orderTo", shopUid);
                        intent.putExtra("orderId", timestamp);
                        intent.putExtra("cost", cost);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });



    }


    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get user data
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String city = "" + ds.child("city").getValue();
                            String accountType = "" + ds.child("accountType").getValue();

                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get Shop data
                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();
                //set shop data
                shopNameTv.setText(shopName);
                phoneTv.setText(shopPhone);
                emailTv.setText(shopEmail);
                addressTv.setText(shopAddress);
                deliveryFeeTv.setText("Delivery Fee: $" + deliveryFee);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")) {
                    openCloseTv.setText("Open");
                } else {
                    openCloseTv.setText("Close");
                }

                try {
                    Picasso.get().load(profileImage).into(shopIv);

                } catch (Exception e) {
                    shopIv.setImageResource(R.drawable.ic_person_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopFoods() {
        //init list
        foodsList = new ArrayList<>();
        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Food")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        foodsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelFood modelFood = ds.getValue(ModelFood.class);
                            foodsList.add(modelFood);
                        }
                        //setup adapter
                        adapterFoodUser = new AdapterFoodUser(ShopDetailsActivity.this, foodsList);
                        //set adapter
                        foodsRv.setAdapter(adapterFoodUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}