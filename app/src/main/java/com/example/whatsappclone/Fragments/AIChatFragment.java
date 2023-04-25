package com.example.whatsappclone.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.ChatRvAdapter;
import com.example.whatsappclone.ChatsModel;
import com.example.whatsappclone.MsgModel;
import com.example.whatsappclone.R;
import com.example.whatsappclone.RetrofitAPI;
import com.example.whatsappclone.databinding.ActivityMainBinding;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AIChatFragment extends Fragment {

    ActivityMainBinding binding;
    private final String BOT_KEY = "bot", USER_KEY = "user";
    ArrayList<ChatsModel> chatsModelArrayList;
    private ChatRvAdapter chatRvAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a_i_chat, container, false);

        view.findViewById(R.id.idEdtMessage).requestFocus();
        chatsModelArrayList = new ArrayList<>();
        chatRvAdapter = new ChatRvAdapter(chatsModelArrayList, getContext());

        //vertical orientation
        LinearLayoutManager manager = new LinearLayoutManager(getContext());

        RecyclerView recyclerView = view.findViewById(R.id.idRVChats);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(chatRvAdapter);

        view.findViewById(R.id.idFABSend).setOnClickListener(view1 -> {
            String messageTxt = ((EditText) getView().findViewById(R.id.idEdtMessage)).getText().toString();

            if (messageTxt.isEmpty()) {
                Toast.makeText(getContext(), "Please enter some message", Toast.LENGTH_SHORT).show();
            } else {
                getResponse(messageTxt);
                EditText messageBox = getView().findViewById(R.id.idEdtMessage);
                messageBox.setText("");
            }
        });

        view.findViewById(R.id.refresh).setOnClickListener(view1 -> {
            Toast.makeText(getContext(), "Refresh Clicked", Toast.LENGTH_SHORT).show();
        });

        return view;

    }

    private void getResponse(String message) {
        chatsModelArrayList.add(new ChatsModel(message, USER_KEY));
        chatRvAdapter.notifyDataSetChanged();

        String url = "http://api.brainshop.ai/get?bid=172753&key=KeSoP2pQcZjdBgZb&uid=[uid]&msg=" + message;
        String BASE_URL = "http://api.brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if (response.isSuccessful()) {
                    MsgModel model = response.body();
                    chatsModelArrayList.add(new ChatsModel(model.getCnt(), BOT_KEY));
                    chatRvAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {
                chatsModelArrayList.add(new ChatsModel("Please revert your questions", BOT_KEY));
                chatRvAdapter.notifyDataSetChanged();

            }
        });
    }
}