package com.example.whatsappclone;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

//fetching the data from the API
public interface RetrofitAPI {

    @GET
    Call<MsgModel> getMessage(@Url String url);
}
