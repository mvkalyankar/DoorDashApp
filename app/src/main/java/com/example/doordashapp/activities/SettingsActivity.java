package com.example.doordashapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.doordashapp.Constants;
import com.example.doordashapp.FoodDonationActivity;
import com.example.doordashapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private TextView notificationStatusTv;
    private SwitchCompat fcmSwitch;
    private FirebaseAuth firebaseAuth;

    private static final String enabledMessage = "Notifications are enabled";
    private static final String disabledMessage = "Notifications are disabled";

    //now we need to save the state of what user choosen,for this use shared preferences
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    private boolean isChecked=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_settings);

         backBtn = findViewById(R.id.backBtn);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);
        fcmSwitch = findViewById(R.id.fcmSwitch);
        firebaseAuth = FirebaseAuth.getInstance();

        sp = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);
        isChecked = sp.getBoolean("FCM_ENABLED",false);
        fcmSwitch.setChecked(isChecked);

        if(isChecked){
            //was enabled
            notificationStatusTv.setText(enabledMessage);
        }else{
            //was diabled
            notificationStatusTv.setText(disabledMessage);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fcmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChecked){
                    //checked,enable
                    subscribeToTopic();
                }else{
                    //Unchecked,diable
                    unSubscribeToTopic();
                }
            }
        });


     }

//we will work with topic based firebase messaging,for a user to receive topic based msg/notification hav to subscribe to that topic
//req : FCM service key
//Go to firebase->project setting->cloud messaging->copy the service key...if there is no key then u can click on "add server key"

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //subscribed successfully
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",false);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disabledMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                //failed
                Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //unsubscribed successfully
                        //save setting ins shared preference
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",true);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enabledMessage);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                //failed
                Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}
