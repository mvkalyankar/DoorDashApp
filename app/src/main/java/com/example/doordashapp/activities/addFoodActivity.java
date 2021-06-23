package com.example.doordashapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.doordashapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class addFoodActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView foodIconIv;
    private EditText titleEt, descriptionEt, priceEt, quantityEt, discountedPriceEt, discountNoteEt;
    private SwitchCompat discountSwitch;
    private Button addFoodBtn;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission array
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri image_uri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);


        backBtn = findViewById(R.id.backBtn);
        foodIconIv = findViewById(R.id.foodIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedPriceEt = findViewById(R.id.discountPriceEt);
        discountNoteEt = findViewById(R.id.discountNoteEt);
        discountSwitch = findViewById(R.id.discountSwitch);
        addFoodBtn = findViewById(R.id.addFoodBtn);

        //on start is unchecked,so hide discountPrice,discountNote
        discountedPriceEt.setVisibility(View.GONE);
        discountNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        //setup progress dialogue
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait.....");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        foodIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialogue to pick image
                showImagePickDialogue();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input the data
                //validate data
                //add data to db
                inputData();
            }
        });

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //checked,show discount price,discount note
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountNoteEt.setVisibility(View.VISIBLE);
                } else {
                    //Not checked,hide discount price,discount note
                    discountedPriceEt.setVisibility(View.GONE);
                    discountNoteEt.setVisibility(View.GONE);
                }
            }
        });
    }

    private String foodTitle, foodDescription, foodQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        //input data
        foodTitle = titleEt.getText().toString().trim();
        foodDescription = descriptionEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();
        foodQuantity = quantityEt.getText().toString().trim();

        //validate value
        if (TextUtils.isEmpty(foodTitle)) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (discountAvailable) {
            //product is with discount
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount Price is required", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            //product is without discount
            discountPrice = "0";
            discountNote = "";
        }
        addProduct();
    }

    private void addProduct() {
        //add data to db

        progressDialog.setMessage("Adding Food...");
        progressDialog.show();
        final String timestamp = "" + System.currentTimeMillis();

        if (image_uri == null) {
            //save info without img
            //setup data to upload
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("foodId", "" + timestamp);
            hashMap.put("foodTitle", "" + foodTitle);
            hashMap.put("foodDescription", "" + foodDescription);
            hashMap.put("foodQuantity", "" + foodQuantity);
            hashMap.put("foodIcon", "");//no img,set empty
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + firebaseAuth.getUid());

            //add to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Food").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(addFoodActivity.this, "Food Added", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(addFoodActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            //upload with img
            //first upload img to storage
            //name and path of img to be uploaded
            String filePathAndName = "food_image/*" + "" + timestamp;
            //upload Image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //img is uploaded
                            //get url of the uploaded img
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("foodId", "" + timestamp);
                                hashMap.put("foodTitle", "" + foodTitle);
                                hashMap.put("foodDescription", "" + foodDescription);
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("foodQuantity", "" + foodQuantity);
                                hashMap.put("foodIcon", "" + downloadImageUri);//img uploaded
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", "" + firebaseAuth.getUid());

                                //add to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Food").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(addFoodActivity.this, "Food Added", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(addFoodActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(addFoodActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void clearData() {
        titleEt.setText("");
        descriptionEt.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedPriceEt.setText("");
        discountNoteEt.setText("");
        foodIconIv.setImageResource(R.drawable.ic_add_shopping_red);
        image_uri = null;

    }

    private void showImagePickDialogue() {
        // Options to display in dialogue
        String[] options = {"CAMERA", "GALLERY"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //camera clicked
                    if (checkCameraPermission()) {
                        //camera permission allowed
                        pickFromCamera();
                    } else {
                        //not allowed
                        requestCameraPermission();
                    }
                } else {
                    if (checkStoragePermission()) {
                        //storage permission allowed
                        pickFromGallery();
                    } else {
                        //not allowed
                        requestStoragePermission();
                    }
                }
            }
        }).show();
    }

    private void pickFromGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera

        //using media store to pick high quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Camera permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //picked image
                image_uri = data.getData();
                //set to imageView
                foodIconIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageView
                foodIconIv.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}