package com.example.whatsappclone.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.example.whatsappclone.Adapters.TopStatusAdapter;
import com.example.whatsappclone.Adapters.UserAdapter;
import com.example.whatsappclone.Models.Status;
import com.example.whatsappclone.Models.User;
import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatusArrayList = new ArrayList<>();

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this,"Search Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.group:
                Toast.makeText(this,"Group Clicked",Toast.LENGTH_SHORT).show();
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