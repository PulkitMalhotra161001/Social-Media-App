package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.Models.Message;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ItemReceiveBinding;
import com.example.whatsappclone.databinding.ItemSendBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Message> messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE  = 2;

    String senderRoom, receiverRoom;


    public MessagesAdapter(Context context, ArrayList<Message> messages,String senderRoom, String receiverRoom){
        this.context = context;
        this.messages=messages;
        this.senderRoom=senderRoom;
        this.receiverRoom=receiverRoom;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
            return new sendViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }else{
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message  =messages.get(position);

        int reactions[]=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry};

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(holder.getClass()==sendViewHolder.class) {
                sendViewHolder viewHolder = (sendViewHolder) holder;
                viewHolder.binding.reaction.setImageResource(reactions[pos]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }else{
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.reaction.setImageResource(reactions[pos]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }

            message.setReaction(pos);

            FirebaseDatabase.getInstance()
                        .getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(message.getMessageId())
                        .setValue(message);

            FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom).child("messages").child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass()==sendViewHolder.class){
            sendViewHolder viewHolder = (sendViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());

            if(message.getReaction()>=0){
//                message.setReaction(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.reaction.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });
        }else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());

            if(message.getReaction()>=0){
//                message.setReaction(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.reaction.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class sendViewHolder extends RecyclerView.ViewHolder{
        ItemSendBinding binding;

        public sendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ItemReceiveBinding  binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }

}
