package com.example.whatsappclone;

//hold display data
public class ChatsModel {

    private String message, sender;

    //multiple recycler view
    //message will display the data
    //sender tell who is the sender (human or bot)

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ChatsModel(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }
}
