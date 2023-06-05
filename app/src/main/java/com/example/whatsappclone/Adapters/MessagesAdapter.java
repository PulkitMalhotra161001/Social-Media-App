package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
            if(holder.getClass()==sendViewHolder.class && pos>=0) {
                sendViewHolder viewHolder = (sendViewHolder) holder;
                Log.d("MessageAdapter","sendViewHolder "+"local: "+pos.toString()+" db: "+message.getReaction());
                viewHolder.binding.reaction.setImageResource(reactions[pos]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }else if(pos>=0){
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                Log.d("MessageAdapter","ReceiverViewHolder "+"local: "+pos.toString()+" db: "+message.getReaction());
                viewHolder.binding.reaction.setImageResource(reactions[pos]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }


            //if same reaction choose then disable it
            if(message.getReaction()>=0 && message.getReaction()==pos)
                message.setReaction(-1);
            else if(pos>=0)
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


        //send view holder
        if(holder.getClass()==sendViewHolder.class){
            sendViewHolder viewHolder = (sendViewHolder) holder;

            //show image if image is send
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.avatar).into(viewHolder.binding.image);
            }

            viewHolder.binding.message.setText(message.getMessage());

            if(message.getReaction()>=0){
//                message.setReaction(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.reaction.setVisibility(View.GONE);
            }

//            viewHolder.itemView.setOnTouchListener((view, motionEvent) -> {
//                popup.onTouch(view,motionEvent);
//                return false;
//            });

//            viewHolder.itemView.setOnLongClickListener(v -> {
//                Log.d("MessageAdapterOnLongClick", String.valueOf(v));
//                View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
//                DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
//                AlertDialog dialog = new AlertDialog.Builder(context)
//                        .setTitle("Delete Message")
//                        .setView(binding.getRoot())
//                        .create();
//
//                binding.everyone.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        message.setMessage("This message is removed.");
//                        message.setReaction(-1);
//                        FirebaseDatabase.getInstance().getReference()
//                                .child("chats")
//                                .child(senderRoom)
//                                .child("messages")
//                                .child(message.getMessageId()).setValue(message);
//
//                        FirebaseDatabase.getInstance().getReference()
//                                .child("chats")
//                                .child(receiverRoom)
//                                .child("messages")
//                                .child(message.getMessageId()).setValue(message);
//                        dialog.dismiss();
//                    }
//                });
//
//                binding.delete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        FirebaseDatabase.getInstance().getReference()
//                                .child("chats")
//                                .child(senderRoom)
//                                .child("messages")
//                                .child(message.getMessageId()).setValue(null);
//                        dialog.dismiss();
//                    }
//                });
//
//                binding.cancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//
//                dialog.show();
//
//                return false;
//            });


        }else{
            //receiver view holder

            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            //show image if image is send
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.avatar).into(viewHolder.binding.image);
            }

            viewHolder.binding.message.setText(message.getMessage());

            if(message.getReaction()>=0){
//                message.setReaction(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setImageResource(reactions[message.getReaction()]);
                viewHolder.binding.reaction.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.reaction.setVisibility(View.GONE);
            }

//            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    popup.onTouch(view,motionEvent);
//                    return false;
//                }
//            });
//
//            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    popup.onTouch(view,motionEvent);
//                    return false;
//                }
//            });
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
