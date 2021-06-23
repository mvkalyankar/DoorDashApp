package com.example.doordashapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doordashapp.FoodDonationActivity;
import com.example.doordashapp.R;
import com.example.doordashapp.adapters.AdapterFoodOwner;
import com.example.doordashapp.adapters.AdapterOrderShop;
import com.example.doordashapp.models.ModelFood;
import com.example.doordashapp.models.ModelOrderShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainOwnerActivity extends AppCompatActivity {

    private TextView nameTv, shopNameTv, emailTv, tabFoodsTv, tabOrdersTv, filteredFoodsTv, filteredOrdersTv, donationTv;
    private ImageButton logoutBtn, editProfileBtn, addFoodBtn, filterOrderBtn, reviewsBtn, settingsBtn;
    private ImageView profileIv;
    private EditText searchFoodEt;
    private RelativeLayout foodRl, ordersRl;
    private RecyclerView foodsRv, ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelFood> foodList;
    private AdapterFoodOwner adapterFoodOwner;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_owner);

        nameTv = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addFoodBtn = findViewById(R.id.addFoodBtn);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        profileIv = findViewById(R.id.profileIv);
        tabFoodsTv = findViewById(R.id.tabFoodsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        foodRl = findViewById(R.id.foodRl);
        ordersRl = findViewById(R.id.ordersRl);
        foodsRv = findViewById(R.id.foodsRv);
        searchFoodEt = findViewById(R.id.searchFoodEt);
        filteredFoodsTv = findViewById(R.id.filteredFoodsTv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        ordersRv = findViewById(R.id.ordersRv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        donationTv = findViewById(R.id.donationTv);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait.....");
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();
        loadAllFoods();
        loadAllOrders();
        showFoodsUI();

        //search
        searchFoodEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterFoodOwner.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make offline
                //signout
                makeMeOffline();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                startActivity(new Intent(MainOwnerActivity.this, ProfileEditOwnerActivity.class));
            }
        });

        addFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainOwnerActivity.this, addFoodActivity.class));
            }
        });

        tabFoodsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showFoodsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // options to display in dialog
                String[] options = {"All", "In Progress", "Completed", "Cancelled"};

                // dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainOwnerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                // handle item clicks
                                if (which == 0) {
                                    // all clicked
                                    filteredOrdersTv.setText("Showing All");
                                    adapterOrderShop.getFilter().filter("");    // show all orders
                                } else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing " + optionClicked + "Orders");    // e.g. showing completed orders
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open same reviews activity as used in user main page
                Intent intent = new Intent(MainOwnerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", firebaseAuth.getUid());
                startActivity(intent);
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainOwnerActivity.this, SettingsActivity.class));
            }
        });

        donationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainOwnerActivity.this, FoodDonationActivity.class));

            }
        });
    }

    private void loadAllOrders() {
        // init array list
        orderShopArrayList = new ArrayList<>();

        // load orders of shop
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // clear list before adding data in it
                        orderShopArrayList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            // add to list
                            orderShopArrayList.add(modelOrderShop);
                        }
                        // setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainOwnerActivity.this, orderShopArrayList);
                        // set adapter to recyclerview
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllFoods() {
        foodList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Food")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        foodList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelFood modelProduct = ds.getValue(ModelFood.class);
                            foodList.add(modelProduct);
                        }
                        //setup adapter
                        adapterFoodOwner = new AdapterFoodOwner(MainOwnerActivity.this, foodList);
                        //set adapter
                        foodsRv.setAdapter(adapterFoodOwner);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showFoodsUI() {

        //show foods ui and hide orders ui
        foodRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabFoodsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabFoodsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui and hide foods ui
        foodRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabFoodsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabFoodsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void makeMeOffline() {
        progressDialog.setMessage("Logging out user.....");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainOwnerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainOwnerActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }

    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data from db
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String email = "" + ds.child("email").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            //set data
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.ic_store_gray);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
    }
}