package com.nerdzlab.smsiptal.models;

/**
 * Created by orcunozyurt on 5/27/17.
 */

public class AnalysedMessage {
    private String sms_header;
    private String sms_content;
    private String sms_cancel_phrase;
    private String sms_cancel_number;
    private Integer sms_status;

    public AnalysedMessage() {
    }

    @Override
    public String toString() {
        return "AnalysedMessage{" +
                "sms_header='" + sms_header + '\'' +
                ", sms_content='" + sms_content + '\'' +
                ", sms_cancel_phrase='" + sms_cancel_phrase + '\'' +
                ", sms_cancel_number='" + sms_cancel_number + '\'' +
                ", sms_status=" + sms_status +
                '}';
    }

    public String getSms_header() {
        return sms_header;
    }

    public void setSms_header(String sms_header) {
        this.sms_header = sms_header;
    }

    public String getSms_content() {
        return sms_content;
    }

    public void setSms_content(String sms_content) {
        this.sms_content = sms_content;
    }

    public String getSms_cancel_phrase() {
        return sms_cancel_phrase;
    }

    public void setSms_cancel_phrase(String sms_cancel_phrase) {
        this.sms_cancel_phrase = sms_cancel_phrase;
    }

    public String getSms_cancel_number() {
        return sms_cancel_number;
    }

    public void setSms_cancel_number(String sms_cancel_number) {
        this.sms_cancel_number = sms_cancel_number;
    }

    public Integer getSms_status() {
        return sms_status;
    }

    public void setSms_status(Integer sms_status) {
        this.sms_status = sms_status;
    }
}
