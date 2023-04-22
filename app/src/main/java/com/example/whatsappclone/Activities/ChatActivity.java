package com.example.whatsappclone.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.whatsappclone.Adapters.MessagesAdapter;
import com.example.whatsappclone.Models.Message;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityChatBinding;
import com.example.whatsappclone.dialog.DialogReviewSendImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom,receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;
    String receiverUid, senderUid;
    private Uri imageUri;
    private boolean actionsShown = false;
    private final int IMAGE_GALLERY_REQUEST = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        //custom toolBar
        setSupportActionBar(binding.toolbar);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");
        String token = getIntent().getStringExtra("token");

        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.profile);

        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()) {
                        if(status.equals("Offline")) {
                            binding.status.setVisibility(View.GONE);
                        } else {
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(adapter);



        //show messages in recyclerView adapter
        database.getReference().child("chats")
                        .child(senderRoom).child("messages")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                messages.clear();
                                for(DataSnapshot snapshot1:snapshot.getChildren()){
                                    Message message = snapshot1.getValue(Message.class);
                                    message.setMessageId(snapshot1.getKey());
                                    messages.add(message);
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        binding.sendB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messagetxt = binding.messagebox.getText().toString();
                Date date = new Date();
                Message message = new Message(messagetxt,senderUid,date.getTime());
                binding.messagebox.setText("");

                String randomKey = database.getReference().push().getKey();

                //added message to sender database
                database.getReference().child("chats").child(senderRoom).child("messages").child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                //added message to receiver database
                                database.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                sendNotification(name,message.getMessage(),token);
                                            }
                                        });

                                //for showing last message in Home Activity
                                HashMap<String,Object> lastMsgObj = new HashMap<>();
                                lastMsgObj.put("lastMsg",message.getMessage());
                                lastMsgObj.put("lastMsgTime",date.getTime());

                                Log.d("PulkitChatAdapter",lastMsgObj.toString());

                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                            }
                        });
            }
        });

        binding.attachment.setOnClickListener(v -> {
            if (actionsShown) {
                actionsShown = false;
                binding.layoutActions.setVisibility(View.GONE);
            } else {
                actionsShown = true;
                binding.layoutActions.setVisibility(View.VISIBLE);
            }
        });

        binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        final Handler handler = new Handler();
        binding.messagebox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });


        //remove Title Text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //back icon
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "select image"), IMAGE_GALLERY_REQUEST);
    }

    void sendNotification(String name,String message, String token){

        try {
            //for sending api call using volley we need request queue

            RequestQueue queue = Volley.newRequestQueue(this);

            //it send json data
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            //we can send segments also
            notificationData.put("to", token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatActivity.this, "sendNotification"+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }){
                //fo calling secured api, developers usually provide authorization key
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    String key = "Key=AAAAqe4IYmc:APA91bHouifbSb1CqV-mTJV0Y-HfnCTY0p44Aj1RhVTi8sKRE9I2LfMx-0jIuaZDjuFHCnMt_-ydTgJwOrrL73AQZJjrdU1hBTsW5cy3W4W78Lf9D_vFn3-LmcJltPlCEwtr7Au7wx29";
                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");
                    return map;
                }
            };

            queue.add(request);

        }catch(Exception e){

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            imageUri = data.getData();
//            uploadToFirebase();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                reviewImage(bitmap,data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        if(requestCode == IMAGE_GALLERY_REQUEST) {
//            if(data != null) {
//                if(data.getData() != null) {
//
//                }
//            }
//        }
    }

    private void reviewImage(Bitmap bitmap, @Nullable Intent data) {
        new DialogReviewSendImage(ChatActivity.this, bitmap).show(new DialogReviewSendImage.OnCallBack() {
            @Override
            public void OnButtonSendClick() {
                if (imageUri != null) {
                    final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
                    progressDialog.setMessage("Sending..");
                    progressDialog.show();
                    binding.layoutActions.setVisibility(View.GONE);
                    actionsShown = false;
                    UploadToDataBase(data,progressDialog);
                }
            }
        });
    }

    private void UploadToDataBase(@Nullable Intent data, ProgressDialog progressDialog) {
        Uri selectedImage = data.getData();
        Calendar calendar = Calendar.getInstance();
        StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");

        reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();

                            String messagetxt = binding.messagebox.getText().toString();
                            Date date = new Date();
                            Message message = new Message(messagetxt,senderUid,date.getTime());
                            message.setMessage("Xa%v5vac^v1v^vi*b&mOnqB61v(n}");
                            message.setImageUrl(filePath);
                            binding.messagebox.setText("");

                            String randomKey = database.getReference().push().getKey();

                            //for showing last message in Home Acticity
                            HashMap<String,Object> lastMsgObj = new HashMap<>();
                            lastMsgObj.put("lastMsg",message.getMessage());
                            lastMsgObj.put("lastMsgTime",date.getTime());

                            Log.d("PulkitChatAdapter",lastMsgObj.toString());

                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                            //added message to sender database
                            database.getReference().child("chats").child(senderRoom).child("messages").child(randomKey)
                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            //added message to receiver database
                                            database.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            progressDialog.dismiss();
                                                        }
                                                    });



                                        }
                                    });

//                                        Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.videocall:
                Toast.makeText(this,"Video call Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.call:
                Toast.makeText(this,"Voice call Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.viewContact:
                Toast.makeText(this,"View Contact Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.search:
                Toast.makeText(this,"Search Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.clearChat:
                Toast.makeText(this,"Clear chat Clicked",Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    //back functionality
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chattopmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}