package com.example.doordashapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.doordashapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText emailEt;
    private Button recoverBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        backBtn = findViewById(R.id.backBtn);
        emailEt = findViewById(R.id.emailEt);
        recoverBtn = findViewById(R.id.recoverBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait.....");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // onBackPressed();
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                finish();
            }
        });

        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
                clearData();
            }
        });
    }

    private String email;

    private void recoverPassword() {
        email = emailEt.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email id..", Toast.LENGTH_SHORT).show();
            return;
        } else {
            progressDialog.setMessage("Sending instruction to reset password ");
            progressDialog.show();

            firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //instructions sent
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, "Reset password instruction is sent to your mail.....", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void clearData() {
        emailEt.setText("");
    }
}