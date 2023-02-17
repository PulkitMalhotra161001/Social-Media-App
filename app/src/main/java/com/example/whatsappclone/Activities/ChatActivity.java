package com.example.whatsappclone.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.whatsappclone.Adapters.MessagesAdapter;
import com.example.whatsappclone.Models.Message;
import com.example.whatsappclone.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom,receiverRoom;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();

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

                database.getReference().child("chats").child(senderRoom).child("messages").child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });

                                HashMap<String,Object> lastMsgObj = new HashMap<>();
                                lastMsgObj.put("lastMsg",message.getMessage());
                                lastMsgObj.put("lastMsgTime",date.getTime());

                                Log.d("Pulkit ChatAdapter",lastMsgObj.toString());

                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                            }
                        });
            }
        });

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}