package com.nerdzlab.smsiptal;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.nerdzlab.smsiptal.models.AnalysedMessage;
import com.nerdzlab.smsiptal.models.GroupedMessage;
import com.nerdzlab.smsiptal.models.Message;
import com.nerdzlab.smsiptal.utils.MyApplication;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.nerdzlab.smsiptal.models.Message.ITEMMAP;

/**
 * An activity representing a list of Messages. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MessageDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MessageListActivity extends AppCompatActivity implements  LoaderCallbacks<Cursor> {

    private static final String TAG = "MessageListActivity";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_THREAD_ID = "thread_id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PERSON = "person";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DATE_SENT = "date_sent";
    public static final String COLUMN_PROTOCOL = "protocol";
    public static final String COLUMN_READ = "read";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_REPLY_PATH_PRESENT = "reply_path_present";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_SERVICE_CENTER = "service_center";
    public static final String COLUMN_LOCKED = "locked";
    public static final String COLUMN_ERROR_CODE = "error_code";
    public static final String COLUMN_SEEN = "seen";
    public static final String COLUMN_TIMED = "timed";
    public static final String COLUMN_DELETED = "deleted";
    public static final String COLUMN_SYNC_STATE = "sync_state";
    public static final String COLUMN_MARKER = "marker";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_BIND_ID = "bind_id";
    public static final String COLUMN_MX_STATUS = "mx_status";
    public static final String COLUMN_MX_ID = "mx_id";
    public static final String COLUMN_OUT_TIME = "out_time";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_SIM_ID = "sim_id";
    public static final String COLUMN_BLOCK_TYPE = "block_type";
    public static final String COLUMN_ADVANCED_SEEN = "advanced_seen";
    public static final String COLUMN_B2C_TTL = "b2c_ttl";
    public static final String COLUMN_B2C_NUMBERS = "b2c_numbers";
    public static final String COLUMN_FAKE_CELL_TYPE = "fake_cell_type";
    public static final String COLUMN_URL_RISKY_TYPE = "url_risky_type";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private  int mStatusCode;
    private ArrayList<Message> mData = new ArrayList<>();
    private SimpleItemRecyclerViewAdapter mAdapter;
    private LinkedHashMap<String,GroupedMessage> GROUPEDITEMMAP = new LinkedHashMap<>();
    private static final int PERMISSION_REQUEST_CODE = 1;
    View recyclerView;
    private DatabaseReference mDatabase;


    public Boolean isSpam(String body){


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
            return true;
        }
        else {
            matcher_mersis.reset();
            matcher_provider.reset();
            return false;
        }

    }

    // [START write_fan_out]
    private void writeNewMessage(String adress, String body, String sd, String rd) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("messages").push().getKey();
        Message msg = new Message(adress, body, sd, rd,false);
        Map<String, Object> postValues = msg.toMap();

        adress = adress.replaceAll("[^A-Za-z0-9]", "");

        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/messages/" + key, postValues);
        childUpdates.put("/messagesbyadress/" + adress + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
        movetoDetailFragment(adress);
    }
    // [END write_fan_out]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        Log.d(TAG, "onCreate: ");

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recyclerView = findViewById(R.id.message_list);
        assert recyclerView != null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {


            if (checkSelfPermission(
                    Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_SMS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    new MaterialDialog.Builder(this)
                            .title(R.string.request_perm)
                            .content(R.string.content_request_perm)
                            .positiveText(R.string.agree)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    requestPermissions(
                                            new String[]{Manifest.permission.READ_SMS},
                                            PERMISSION_REQUEST_CODE);
                                }
                            })
                            .show();

                } else {

                    // No explanation needed, we can request the permission.

                    requestPermissions(
                            new String[]{Manifest.permission.READ_SMS},
                            PERMISSION_REQUEST_CODE);


                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }else {
                setupRecyclerView((RecyclerView) recyclerView);
            }
        }else{


            setupRecyclerView((RecyclerView) recyclerView);

        }



        if (findViewById(R.id.message_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }





    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // sms-related task you need to do.
                    setupRecyclerView((RecyclerView) recyclerView);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    new MaterialDialog.Builder(this)
                            .title(R.string.reject)
                            .content(R.string.content_reject)
                            .positiveText(R.string.agree)
                            .show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        //mAdapter.swapItems(GROUPEDITEMMAP);
        //getSupportLoaderManager().restartLoader(0, null, this);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new SimpleItemRecyclerViewAdapter(GROUPEDITEMMAP);
        recyclerView.setAdapter(mAdapter);
        Log.d(TAG, "setupRecyclerView:: ");

        // Simple query to show the most recent SMS messages in the inbox
        //getSupportLoaderManager().destroyLoader();
        getSupportLoaderManager().initLoader(SmsQuery.TOKEN, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (i == SmsQuery.TOKEN) {
            // This will fetch all SMS messages in the inbox, ordered by date desc
            CursorLoader mcl = new CursorLoader(this, SmsQuery.CONTENT_URI, SmsQuery.PROJECTION,
                    null, null, SmsQuery.SORT_ORDER);

            return mcl;
        }
        return null;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == SmsQuery.TOKEN && cursor != null) {
            // Standard swap cursor in when load is done

            ITEMMAP.clear();
            GROUPEDITEMMAP.clear();

            while (cursor.moveToNext()) {

                String body = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY));

                if(isSpam(body) )
                {

                    Message msg = new Message();
                    msg.setAdress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                    msg.setBody(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY)));




                    mData.add(msg);
                    ITEMMAP.put(msg.getAdress(),msg);

                    if(GROUPEDITEMMAP.get(msg.getAdress()) != null)
                    {

                        GROUPEDITEMMAP.get(msg.getAdress()).getMessages().add(msg);
                    }else {
                        ArrayList<Message> msglist = new ArrayList<>();
                        msglist.add(msg);

                        GroupedMessage grmsg = new GroupedMessage();
                        grmsg.setAdress(msg.getAdress());
                        grmsg.setMessages(msglist);


                        GROUPEDITEMMAP.put(msg.getAdress(),grmsg);
                    }





                }

            }
            mAdapter.notifyDataSetChanged();

        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Standard swap cursor to null when loader is reset

    }

    public void movetoDetailFragment(String adress){
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(MessageDetailFragment.ARG_ITEM_ID, adress);
            MessageDetailFragment fragment = new MessageDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, fragment)
                    .commit();
        } else {
            Context context = MessageListActivity.this;
            Intent intent = new Intent(context, MessageDetailActivity.class);
            intent.putExtra(MessageDetailFragment.ARG_ITEM_ID, adress);

            context.startActivity(intent);
        }

    }


    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private  LinkedHashMap<String, GroupedMessage> mValues;

        public void swapItems(LinkedHashMap<String, GroupedMessage> data) {
            this.mValues = data;
            notifyDataSetChanged();
        }

        public SimpleItemRecyclerViewAdapter(LinkedHashMap<String, GroupedMessage> items) {
            mValues = items;
        }

        public Object getElementByIndex(LinkedHashMap map,int index){
            return map.get( (map.keySet().toArray())[ index ] );
        }





        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = (GroupedMessage)getElementByIndex(mValues, position);
            holder.mIdView.setText(holder.mItem.getAdress());
            holder.mContentView.setText(""+ holder.mItem.getCount()+" Adet");
            int total = 0;

            Log.d(TAG, "onBindViewHolder: " +
                    MyApplication.getInstance().isCanceledBefore(holder.mItem.getAdress()) + " " +
            MyApplication.getInstance().getCanceledSubs());

            if(MyApplication.getInstance().isCanceledBefore(holder.mItem.getAdress()))
            {
                holder.mTrash.setImageResource(R.drawable.buttonok);
            }else {
                holder.mTrash.setImageResource(R.drawable.trashcan);
            }

            for (int i = 0 ; i < mValues.size();i++){
                GroupedMessage  item = (GroupedMessage)getElementByIndex(mValues, i);
                total += item.getCount();

            }

            holder.mProgress.setDonut_progress(""+holder.mItem.getCount() * 100 / total);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movetoDetailFragment(holder.mItem.getAdress());
                }
            });


            holder.mTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: ");

                    if(MyApplication.getInstance().isCanceledBefore(holder.mItem.getAdress()))
                    {
                        movetoDetailFragment(holder.mItem.getAdress());

                    }else{

                        //askBackendDetails(holder.mItem.getAdress(),ITEMMAP.get(holder.mItem.getAdress()).getBody());

                        Query query = mDatabase.child("messagesbyadress")
                                .child(holder.mItem.getAdress())
                                .orderByChild("cancel_number");

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Log.d(TAG, "onDataChange: DATA SNAPSHOT EXITS");
                                    Boolean matchfound = false;
                                    Message matchedMessage = null;

                                    for (DataSnapshot savedmessage : dataSnapshot.getChildren()) {
                                        Message matchCandidate = savedmessage.getValue(Message.class);
                                        if( matchCandidate.getCancel_number() != null &&
                                                ITEMMAP.get(holder.mItem.getAdress()).getBody()
                                                        .contains(matchCandidate.getCancel_number())) {

                                            Log.d(TAG, "DATA MATCHED WITH : "+matchCandidate.getCancel_number());
                                            matchfound = true;
                                            matchedMessage = matchCandidate;
                                            break;
                                        }
                                    }

                                    if(matchfound && matchedMessage != null){

                                        if(matchedMessage.getSpam()) {
                                            sendSMS(matchedMessage.getCancel_number(),
                                                    matchedMessage.getCancel_phrase(),
                                                    matchedMessage.getAdress());
                                        }


                                    }
                                    else {

                                        Log.d(TAG, "NO MATCH CREATING NEW ONE ");

                                        writeNewMessage(holder.mItem.getAdress(),
                                                ITEMMAP.get(holder.mItem.getAdress()).getBody(),
                                                ITEMMAP.get(holder.mItem.getAdress()).getSentDate(),
                                                ITEMMAP.get(holder.mItem.getAdress()).getReceivedDate());

                                    }
                                }else {

                                    Log.d(TAG, "NO MATCH CREATING NEW ONE ");

                                    writeNewMessage(holder.mItem.getAdress(),
                                            ITEMMAP.get(holder.mItem.getAdress()).getBody(),
                                            ITEMMAP.get(holder.mItem.getAdress()).getSentDate(),
                                            ITEMMAP.get(holder.mItem.getAdress()).getReceivedDate());

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        //askFirebaseDetails();
                        /*writeNewMessage(holder.mItem.getAdress(),
                                ITEMMAP.get(holder.mItem.getAdress()).getBody(),
                                ITEMMAP.get(holder.mItem.getAdress()).getSentDate(),
                                ITEMMAP.get(holder.mItem.getAdress()).getReceivedDate());*/

                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public final ImageView mTrash;
            public GroupedMessage mItem;
            public final DonutProgress mProgress;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
                mProgress = (DonutProgress) view.findViewById(R.id.donut_progress);
                mTrash = (ImageView) view.findViewById(R.id.trashcan);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    /**
     * A basic SmsQuery on android.provider.Telephony.Sms.Inbox
     */
    private interface SmsQuery {
        int TOKEN = 1;
        static final Uri CONTENT_URI = Telephony.Sms.Inbox.CONTENT_URI;
        static final String[] PROJECTION = {
                Telephony.Sms.Inbox._ID,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.BODY,
        };
        static final String SORT_ORDER = Telephony.Sms.Inbox.DEFAULT_SORT_ORDER;
        int ID = 0;
        int ADDRESS = 1;
        int BODY = 2;
    }



    public void askBackendDetails(String sms_header, String sms_content){



        JSONObject obj = new JSONObject();

        try {
            obj.put("sms_header", sms_header);
            obj.put("sms_content", sms_content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.url) +"/contents/";
        Log.i("askBackendDetails","URL: "+url+" req: "+ obj);

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Yükleniyor...");
        pDialog.show();
        JsonObjectRequest loginrequest = new JsonObjectRequest(Request.Method.POST,url,obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        pDialog.hide();
                        pDialog.dismiss();

                        Gson gson = new Gson();
                        AnalysedMessage analysedMessage =
                                gson.fromJson(String.valueOf(response),AnalysedMessage.class);

                        if(analysedMessage.getSms_status() == 1 &&
                                analysedMessage.getSms_cancel_phrase() != null)
                        {
                            sendSMS(analysedMessage.getSms_cancel_number(), analysedMessage.getSms_cancel_phrase(), analysedMessage.getSms_header());

                        }else {
                            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "sms:" + analysedMessage.getSms_cancel_number()));
                            intent.putExtra( "sms_body", "" );
                            startActivity(intent);
                        }



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.hide();
                        pDialog.dismiss();
                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            Log.d(TAG, "onErrorResponse: " + new String(response.data));
                            //Additional cases
                        }
                        VolleyLog.d("RESPONSE", "Error: " + error);
                        View view = findViewById(android.R.id.content);
                        Snackbar.make(view, "Bir Hata Oluştu.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        //ShowErrorMessage(context);

                        /*MyApplication.getInstance().setAccessToken("");

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();*/
                    }
                }) {
            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                //headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NTU1NjQ1MzV9.uziTJmdCjxXKi6pLHh3OsSJXmlQyS7Izf7sT-kD3CHU");
                return headers;
            }
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Log.d("PARSEERROR", " PARSE ERROR" + response.statusCode);
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };
        loginrequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(loginrequest);

    }

    public void sendSMS(String phoneNo, String msg, String deletemessagenumber) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
            MyApplication.getInstance().addCanceled(deletemessagenumber);
            mAdapter.swapItems(GROUPEDITEMMAP);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    public void deleteSMS(Context context,  String number) {
        try {
            Log.d(TAG, "deleteSMS: Deleting SMS from inbox");
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[] { "_id", "thread_id", "address",
                            "person", "date", "body" }, null, null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);

                    Log.d(TAG, "deleteSMS: "+ address + " - "+ number);

                    if (address.equalsIgnoreCase(number)) {
                        Log.d(TAG,"Deleting SMS with id: " + threadId);
                        context.getContentResolver().delete(
                                Uri.parse("content://sms/" + id), null, null);

                        mAdapter.notifyDataSetChanged();
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG,"Could not delete SMS from inbox: " + e.getMessage());
        }
    }

}
