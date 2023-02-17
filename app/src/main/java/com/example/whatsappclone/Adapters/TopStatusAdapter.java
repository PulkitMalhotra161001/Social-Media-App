package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ItemStatusBinding;

import java.util.ArrayList;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder>{

    Context context;
    ArrayList<UserStatus> userStatusArrayList;

    public TopStatusAdapter(Context context, ArrayList<UserStatus> userStatusArrayList){
        this.context = context;
        this.userStatusArrayList = userStatusArrayList;
    }


    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status,parent,false);
        return new TopStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return userStatusArrayList.size();
    }

    public class TopStatusViewHolder extends RecyclerView.ViewHolder{
        ItemStatusBinding binding;

        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = binding;
        }
    }
}
