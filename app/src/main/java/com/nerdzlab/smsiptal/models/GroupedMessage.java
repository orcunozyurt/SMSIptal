package com.nerdzlab.smsiptal.models;

import java.util.ArrayList;

/**
 * Created by orcunozyurt on 5/24/17.
 */

public class GroupedMessage {
    private ArrayList<Message> messages;
    private int count;
    private String adress;

    public GroupedMessage(ArrayList<Message> messages, int count, String adress) {
        this.messages = messages;
        this.count = count;
        this.adress = adress;
    }

    public GroupedMessage() {
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public int getCount() {
        if(messages != null)
            return messages.size();
        else
            return 0;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }
}
