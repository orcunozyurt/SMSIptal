package com.nerdzlab.smsiptal.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.nerdzlab.smsiptal.MessageListActivity;
import com.nerdzlab.smsiptal.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by btdays on 5/29/17.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "Message recieved";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: SMS RECEIVED");
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String msgbody = "";
        String msgadress= "";
        if (bundle != null)
        {
            Log.d(TAG, "onReceive: BUNDLE NOT NULL");
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                msgadress += "" + msgs[i].getOriginatingAddress();

                msgbody += msgs[i].getMessageBody().toString();
                msgbody += "\n";
            }
            Toast.makeText(context, msgbody, Toast.LENGTH_SHORT).show();
            if(!msgbody.isEmpty()){
                if(isSpam(msgbody)){

                    createNotification(context, msgadress,msgbody);
                }
            }
        }
    }

    private void createNotification(Context context, String msgadress, String msgbody){

        Log.d(TAG, "createNotification: NOTIFICATION MANAGER HANDLER");

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(context, MessageListActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);



        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo_no_background)
                        .setContentTitle("Kampanya Mesajı")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .addAction(R.drawable.logo_no_background, "Hemen Hallet", pIntent)
                        .setContentText("Az önce SMS gönderen "+msgadress+" sizi rahatsız mı ediyor? Hemen halledebilirim ;)");

        mBuilder.setSound(alarmSound);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] events = new String[6];
        events[0] = "Az önce size SMS gönderen "+msgadress+" ";
        events[1] = "sizi rahatsız mı ediyor? Hemen halledebilirim ;)";

        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Kampanya Mesajı");

        // Moves events into the expanded layout
        for (int i=0; i < events.length; i++) {

            inboxStyle.addLine(events[i]);
        }
        // Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);


        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 3000ms
                Log.d(TAG, "createNotification: DELAYED");

                mNotificationManager.notify(1, mBuilder.build());
            }
        }, 10000);

    }

    private Boolean isSpam(String body){


        Log.d(TAG, "isSpam: Method Call-");
        Pattern pattern_mersis = Pattern.compile("(\\d{16})");
        Pattern pattern_provider = Pattern.compile("[A-Z][0-9]{3,3}$");
        Matcher matcher_mersis = pattern_mersis.matcher(body);
        Matcher matcher_provider = pattern_provider.matcher(body);

        //Log.d(TAG, "isSpam: "+ matcher_mersis.find()+ " " + matcher_provider.find());
        if ((matcher_mersis.find() && matcher_provider.find())|| body.contains("Mersis") ||
                body.contains("mersis")) {
            matcher_mersis.reset();
            matcher_provider.reset();
            Log.d(TAG, "isSpam: true");
            return true;
        }
        else {
            Log.d(TAG, "isSpam: false");
            matcher_mersis.reset();
            matcher_provider.reset();
            return false;
        }

    }
}