package com.example.whatsappclone;

//this is hold the data from the api
public class MsgModel {
    private String cnt;

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public MsgModel(String cnt) {
        this.cnt = cnt;
    }
}
