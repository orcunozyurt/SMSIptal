package com.nerdzlab.smsiptal.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by orcunozyurt on 5/22/17.
 */

public class Message {
    public static HashMap<String,Message> ITEMMAP = new HashMap<>();

    private String adress;
    private String body;
    private String sentDate;
    private String receivedDate;
    private Boolean spam;
    private String cancel_number;
    private String cancel_phrase;


    public Message(String adress, String body, String sentDate, String receivedDate, Boolean spam) {
        this.body = body;

        this.adress = adress.replaceAll("[^A-Za-z0-9]", "");
        this.sentDate = sentDate;
        this.receivedDate = receivedDate;
        this.spam = spam;
    }

    public Message() {
        this.spam=false;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("adress", adress);
        result.put("body", body);
        result.put("sentDate", sentDate);
        result.put("receivedDate", receivedDate);
        result.put("spam", spam);


        return result;
    }
    // [END post_to_map]


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress.replaceAll("[^A-Za-z0-9]", "");;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Boolean getSpam() {
        return spam;
    }

    public void setSpam(Boolean spam) {
        this.spam = spam;
    }

    public String getCancel_number() {
        return cancel_number;
    }

    public void setCancel_number(String cancel_number) {
        this.cancel_number = cancel_number;
    }

    public String getCancel_phrase() {
        return cancel_phrase;
    }

    public void setCancel_phrase(String cancel_phrase) {
        this.cancel_phrase = cancel_phrase;
    }
}
