package com.example.whatsappclone.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.whatsappclone.BuildConfig;
import com.example.whatsappclone.Models.User;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivitySetUpProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SetUpProfileActivity extends AppCompatActivity {

    ActivitySetUpProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;
    private final int IMAGE_GALLERY_REQUEST = 111;
    private static final int CAMERA_REQUEST_CODE = 212;
    private static final int WRITE_EXTERNAL_STORAGE = 123;
    private static final int IMAGE_INTENT_CODE = 443;
    private BottomSheetDialog bsPickPhoto;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Profile...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();

        binding.imageView.setOnClickListener(view -> {
            showBottomSheetPickPhoto();
        });

        binding.continueBT.setOnClickListener(view -> {
            String name = binding.nameET.getText().toString();

            if(name.isEmpty()){
                binding.nameET.setError("Please enter name");
                return;
            }

            dialog.show();
            if(selectedImage!=null){
                //create a folder inside "Profiles" folder
                //file name is "auth.getUid()"
                StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());

                //put image in user folder
                //image name is value of "auth.getUid()"
                reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        //if profile is uploaded successfully to storage
                        if(task.isSuccessful()){

                            //we want URL bcz we want to save into User profile
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // uri -> user profile link
                                    String imageUrl = uri.toString();

                                    //how
                                    String uid = auth.getUid();
                                    String phone = auth.getCurrentUser().getPhoneNumber();

                                    User user = new User(uid,name,phone,imageUrl);

                                    //add user data into firebase database inside "users" folder

                                    database.getReference()
                                            .child("users").child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {

                                                //if user is successfully added and go to next activity
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(SetUpProfileActivity.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }else{
                String uid = auth.getUid();
                String phone = auth.getCurrentUser().getPhoneNumber();

                User user = new User(uid,name,phone,"N/A");

                database.getReference()
                        .child("users").child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialog.dismiss();
                                Intent intent = new Intent(SetUpProfileActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
            }

        });
    }

    private void showBottomSheetPickPhoto() {
        View view = getLayoutInflater().inflate(R.layout.profile_pick_sheet, null);

        view.findViewById(R.id.ll_gallery).setOnClickListener(v -> {
            openGallery();
            bsPickPhoto.dismiss();
        });
        view.findViewById(R.id.ll_camera).setOnClickListener(v -> {
            checkCameraPermission();
            bsPickPhoto.dismiss();
        });

        bsPickPhoto = new BottomSheetDialog(this);
        bsPickPhoto.setContentView(view);

        bsPickPhoto.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        bsPickPhoto.setOnDismissListener(dialog -> bsPickPhoto = null);
        bsPickPhoto.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), IMAGE_GALLERY_REQUEST);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("listPhotoName", imageFileName);
            startActivityForResult(intent, IMAGE_INTENT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if(data!=null && data.getData()!=null){
//            binding.imageView.setImageURI(data.getData() );
//            selectedImage = data.getData();
//        }

        if (requestCode == IMAGE_GALLERY_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            imageUri = data.getData();
            binding.imageView.setImageURI(imageUri );
            selectedImage = imageUri;
        } else if (requestCode == IMAGE_INTENT_CODE
                && resultCode == RESULT_OK) {
            binding.imageView.setImageURI(imageUri );
            selectedImage = imageUri;
        }
    }
}