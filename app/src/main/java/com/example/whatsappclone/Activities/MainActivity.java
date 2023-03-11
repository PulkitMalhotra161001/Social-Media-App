package com.example.whatsappclone.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.whatsappclone.Adapters.TopStatusAdapter;
import com.example.whatsappclone.Adapters.UserAdapter;
import com.example.whatsappclone.Models.Status;
import com.example.whatsappclone.Models.User;
import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //RecyclerView -> Context, Sample Layout, Sample Data
    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    UserAdapter usersAdapter;

    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatusArrayList;
    ProgressDialog dialog;

    User user;

    //outside app notification
    //in-app chatting with other person notification
    //remoteConfig look change

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //remoteConfig -> control app from backEnd and change look and feel like fetival season
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                //tim interval fetch server for updates
                //0 for debugging and 3600 standard
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        //using this function we change color and activate it
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

                String backgroundImage = mFirebaseRemoteConfig.getString("backgroundImage");

                //load image drawable from storage
//                Glide.with(MainActivity.this)
//                        .load(backgroundImage)
//                        .into(binding.backgroundImage);

                /* Toolbar Color */
                String toolbarColor = mFirebaseRemoteConfig.getString("toolbarColor");
                String toolBarImage = mFirebaseRemoteConfig.getString("toolbarImage");
                boolean isToolBarImageEnabled = mFirebaseRemoteConfig.getBoolean("toolBarImageEnabled");
                //i set it as false in firebase remote Config


                if(isToolBarImageEnabled) {
                    Glide.with(MainActivity.this)
                            .load(toolBarImage)
                            .into(new CustomTarget<Drawable>() {


                                @Override
                                public void onResourceReady(@NonNull @NotNull Drawable resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Drawable> transition) {
                                    getSupportActionBar()
                                            .setBackgroundDrawable(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                                }
                            });
                } else {
//                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));
                }

            }
        });


        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatusArrayList = new ArrayList<>();

        //firebase notification
        //there are 2 ways to send notification
        //option 1 -> generate firebase function( it will detect when chat folder is updated then take token from user folder and send notification)
        //option 2 -> code it with the help of google api (volley(google)/retrofit library for api call)
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                HashMap<String,Object> map=new HashMap<>();
                map.put("token",token);

                //update user database and add unique token so we can use in the future for notification
                //make sure app is not open
                database.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).updateChildren(map);
            }
        });

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user=snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersAdapter = new UserAdapter(this,users);
        statusAdapter = new TopStatusAdapter(this,userStatusArrayList);

        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);
        binding.statusList.setAdapter(statusAdapter);

        binding.RV.setAdapter(usersAdapter);

        binding.RV.showShimmerAdapter();
        binding.statusList.showShimmerAdapter();

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                        users.add(user);
                }
                binding.RV.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set stories to activity
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatusArrayList.clear();
                    for(DataSnapshot storySnapshot:snapshot.getChildren()) {
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> status_list = new ArrayList<>();
                        for(DataSnapshot statusSnapshot: storySnapshot.child("statusArrayList").getChildren()){
                            Status tempStatus = statusSnapshot.getValue(Status.class);
                            status_list.add(tempStatus);
                        }

                        status.setStatusArrayList(status_list);
                        userStatusArrayList.add(status);
                    }
                    statusAdapter.notifyDataSetChanged();
                }
                binding.statusList.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this,"Search Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.group:
                startActivity(new Intent(MainActivity.this,GroupChatActivity.class));
                break;
            case R.id.invite:
                Toast.makeText(this,"Invite Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.sing_out:
                Toast.makeText(this,"Sign Out Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(this,"Settings Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.addStatus:
                addStaus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addStaus() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,75);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null && data.getClipData()!=null){
            Log.d("MainActivityImages","ClipData: "+data.getClipData().toString());
        }

        if(data!=null && data.getData()!=null)
            Log.d("MainActivityImages","Data: "+data.getData().toString());

        if(data!=null){
            dialog.show();
            if(data.getData()!=null){
                addToFirebaseAndStorage(data.getData(),true);
            }else if(data.getClipData()!=null){
                boolean dismiss = false;
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    if(i==data.getClipData().getItemCount()-1)  dismiss=true;
                    addToFirebaseAndStorage(data.getClipData().getItemAt(i).getUri(),dismiss);
                }
            }
        }
    }

    private void addToFirebaseAndStorage(Uri data,boolean dismiss) {
        Log.d("addToFirebaseAndStorage",data.toString());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Date date = new Date();
        StorageReference reference = storage.getReference().child("status").child(date.getTime()+"");
        reference.putFile(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                //is image is successfully uploaded to firebase storage
                //we download the url and then save it to our firebase database
                reference.getDownloadUrl().addOnSuccessListener(uri -> {

                    UserStatus userStatus = new UserStatus();
                    userStatus.setName(user.getName());
                    userStatus.setProfileImage(user.getProfileImage());
                    userStatus.setLastUpdated(date.getTime());

                    HashMap<String,Object> obj = new HashMap<>();
                    obj.put("name",userStatus.getName());
                    obj.put("lastUpdated",userStatus.getLastUpdated());
                    obj.put("profileImage",userStatus.getProfileImage());

                    //update database
                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);

                    //update story list
                    String imageUrl = uri.toString();
                    Status status = new Status(imageUrl,userStatus.getLastUpdated());
                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid()).child("statusArrayList").push().setValue(status);

                    if(dismiss) dialog.dismiss();
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}