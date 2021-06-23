package com.example.doordashapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doordashapp.R;
import com.example.doordashapp.adapters.AdapterOrderUser;
import com.example.doordashapp.adapters.AdapterShop;
import com.example.doordashapp.models.ModelOrderUser;
import com.example.doordashapp.models.ModelShop;
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

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv;
    private ImageButton logoutBtn, editProfileBtn, settingsBtn;
    private ImageView profileIv;
    private RelativeLayout shopsRl, ordersRl;
    private RecyclerView shopsRv, ordersRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> shopList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        emailTv = findViewById(R.id.emailTv);
        profileIv = findViewById(R.id.profileIv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        shopsRv = findViewById(R.id.shopsRv);
        ordersRv = findViewById(R.id.ordersRv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        settingsBtn = findViewById(R.id.settingsBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait.....");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        showShopsUI();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //signout
                makeMeOffline();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));

            }
        });

        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showShopsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });
    }

    private void showShopsUI() {
        //show Shops ui and hide orders ui
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui and hide Shops ui
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

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
                Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
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
                            //get user data
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String city = "" + ds.child("city").getValue();
                            String accountType = "" + ds.child("accountType").getValue();

                            //set user data
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.ic_person_gray);
                            }

                            //load only those shops that are in the users city
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadOrders() {

        //init list
        ordersList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before adding
                ordersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = "" + ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);
                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);
                                        //set adapter to RV
                                        ordersRv.setAdapter(adapterOrderUser);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String city) {
        //init list
        shopList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Owner")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        shopList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelShop modelShop = ds.getValue(ModelShop.class);
                            String shopCity = "" + ds.child("city").getValue();

                            //show only user city shops
                            if (shopCity.equals(city)) {
                                shopList.add(modelShop);
                            }
                            //if you want to display all shops,skip if stmt n add this
                            //shopList.add(modelShop);
                        }
                        adapterShop = new AdapterShop(MainUserActivity.this, shopList);
                        //set adapter to RV
                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}