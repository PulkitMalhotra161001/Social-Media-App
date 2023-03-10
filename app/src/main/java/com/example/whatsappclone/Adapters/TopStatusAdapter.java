package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Activities.MainActivity;
import com.example.whatsappclone.Models.Status;
import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ItemStatusBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;
import omari.hamza.storyview.utils.StoryViewHeaderInfo;

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

        UserStatus userStatus = userStatusArrayList.get(position);

        Log.d("TopStatusAdapter",userStatus.getStatusArrayList().size()+"");
        Status lastStatus = userStatus.getStatusArrayList().get(userStatus.getStatusArrayList().size()-1);
        Glide.with(context).load(lastStatus.getImageUrl()).into(holder.binding.image);
        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatusArrayList().size());


        holder.binding.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<StoryViewHeaderInfo> headerInfoArrayList = new ArrayList<>();

                for (Status story : userStatus.getStatusArrayList()) {
                    headerInfoArrayList.add(new StoryViewHeaderInfo(
                            userStatus.getName(),
                            timeStampFormat(story.getTimeStamp()),
                            userStatus.getProfileImage()
                    ));
                }

                ArrayList<MyStory> myStories = new ArrayList<>();
                for(Status status:userStatus.getStatusArrayList()){
                    myStories.add(new MyStory(status.getImageUrl()));
                }

                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // MyStory's ArrayList
                        .setStoryDuration(5000) // Optional, default is 2000 Millis
                        .setHeadingInfoList(headerInfoArrayList) // StoryViewHeaderInfo's ArrayList
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                // your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                // your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before show method
                        .show();
//
//
//                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
//                        .setStoriesList(myStories) // Required
//                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
//                        .setTitleText(userStatus.getName()) // Default is Hidden
//                        .setSubtitleText("") // Default is Hidden
//                        .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
//                        .setStoryClickListeners(new StoryClickListeners() {
//                            @Override
//                            public void onDescriptionClickListener(int position) {
//                                //your action
//
//                            }
//
//                            @Override
//                            public void onTitleIconClickListener(int position) {
//                                //your action
//                            }
//                        }) // Optional Listeners
//                        .build() // Must be called before calling show method
//                        .show();


            }
        });
        Log.d("TopStatusAdapter",userStatus.getStatusArrayList().size()+"");
    }

    private String timeStampFormat(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(new Date(timeStamp));
    }

    @Override
    public int getItemCount() {
        return userStatusArrayList.size();
    }

    public class TopStatusViewHolder extends RecyclerView.ViewHolder{
        ItemStatusBinding binding;

        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = ItemStatusBinding.bind(itemView);
//            this.binding = binding;
        }
    }
}
