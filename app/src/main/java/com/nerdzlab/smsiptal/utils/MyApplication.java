package com.nerdzlab.smsiptal.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

/**
 * Created by orcunozyurt on 5/27/17.
 */

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class
            .getSimpleName();
    private static MyApplication mInstance;
    private RequestQueue mRequestQueue;
    private static ArrayList<String> canceled_subscriptions;
    TinyDB tinydb ;

    //private static Warehouse mWarehouse;



    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        tinydb = new TinyDB(getApplicationContext());
        canceled_subscriptions = getCanceledMessages();


    }
    public  ArrayList<String> getCanceledSubs(){

        return canceled_subscriptions;
    }

    private ArrayList<String> getCanceledMessages()
    {

        return tinydb.getListString("canceled");

    }

    public void addCanceled (String newcomer)
    {
        ArrayList<String> oldones = getCanceledMessages();
        if(oldones!= null) {
            oldones.add(newcomer);
        }else{
            oldones = new ArrayList<>();
            oldones.add(newcomer);
        }

        tinydb.putListString("canceled",oldones);

    }
    public Boolean isCanceledBefore(String input){

        if(getCanceledMessages()!= null && getCanceledMessages().contains(input))
            return true;
        else
            return false;
    }




    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


}
