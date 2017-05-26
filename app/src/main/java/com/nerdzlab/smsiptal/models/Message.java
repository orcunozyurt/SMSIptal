package com.nerdzlab.smsiptal.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by orcunozyurt on 5/22/17.
 */

public class Message {
    public static HashMap<String,Message> ITEMMAP = new HashMap<>();

    private int id;
    private String body;
    private String adress;
    private String type;
    private String serviceCenter;
    private String subject;
    private Long sentDate;
    private Long receivedDate;
    private Boolean seen;
    private Boolean read;
    private Boolean spam;

    public Message(int id,String body, String adress, String type, String serviceCenter, String subject,
                   Long sentDate, Long receivedDate, Boolean seen, Boolean read) {
        this.body = body;
        this.adress = adress;
        this.type = type;
        this.serviceCenter = serviceCenter;
        this.subject = subject;
        this.sentDate = sentDate;
        this.receivedDate = receivedDate;
        this.seen = seen;
        this.read = read;
        this.id = id;
    }

    public Message() {
    }

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
        this.adress = adress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getSentDate() {
        return sentDate;
    }

    public void setSentDate(Long sentDate) {
        this.sentDate = sentDate;
    }

    public Long getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Long receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getSpam() {
        return spam;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
