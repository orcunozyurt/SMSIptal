package com.nerdzlab.smsiptal;


import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;


import com.github.lzyzsd.circleprogress.DonutProgress;
import com.nerdzlab.smsiptal.models.GroupedMessage;
import com.nerdzlab.smsiptal.models.Message;


import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private ArrayList<Message> mData = new ArrayList<>();
    private SimpleItemRecyclerViewAdapter mAdapter;
    private LinkedHashMap<String,GroupedMessage> GROUPEDITEMMAP = new LinkedHashMap<>();

    /*public Data<Sms> getSMSData()
    {
        TelephonyProvider provider = new TelephonyProvider(getApplicationContext());
        return provider.getSms(TelephonyProvider.Filter.INBOX);
    }

    private void displaySmsLog() {
        Uri allMessages = Uri.parse("content://sms/inbox");
        //Cursor cursor = managedQuery(allMessages, null, null, null, null); Both are same
        Cursor cursor = this.getContentResolver().query(allMessages, null,
                null, null, null);

        while (cursor.moveToNext()) {
            *//*for (int i = 0; i < cursor.getColumnCount(); i++) {


            }*//*
            if(isSpam(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY))));
            {
                Log.d( " TEST ",  "--"+cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY)));
                Message msg = new Message();
                msg.setAdress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                msg.setBody(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY)));


                mData.add(msg);
                ITEMMAP.put(msg.getAdress(),msg);

                mAdapter.notifyDataSetChanged();


            }

        }
        cursor.close();

    }*/


    public Boolean isSpam(String body){


        Pattern pattern_mersis = Pattern.compile("(\\d{16})");
        Pattern pattern_provider = Pattern.compile("[A-Z][0-9]{3,3}");
        Matcher matcher_mersis = pattern_mersis.matcher(body);
        Matcher matcher_provider = pattern_provider.matcher(body);

        //Log.d(TAG, "isSpam: "+ matcher_mersis.find()+ " " + matcher_provider.find());
        if (matcher_mersis.find() && matcher_provider.find()) {
            System.out.println("MATCH FOUND: "+matcher_mersis.group(0) + " - "+ matcher_provider.group(0));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

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



        View recyclerView = findViewById(R.id.message_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.message_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new SimpleItemRecyclerViewAdapter(GROUPEDITEMMAP);
        recyclerView.setAdapter(mAdapter);
        //displaySmsLog();

        /*GetSMSInboxTask smstask = new GetSMSInboxTask();
        smstask.execute();*/

        // Simple query to show the most recent SMS messages in the inbox
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

            while (cursor.moveToNext()) {

                String body = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY));

                if(isSpam(body) )
                {

                    Message msg = new Message();
                    msg.setAdress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                    msg.setBody(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY)));
                    msg.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));



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




    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final LinkedHashMap<String, GroupedMessage> mValues;

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
            holder.mContentView.setText("--"+ holder.mItem.getCount());
            int total = 0;

            for (int i = 0 ; i < mValues.size();i++){
                GroupedMessage  item = (GroupedMessage)getElementByIndex(mValues, i);
                total += item.getCount();

            }

            holder.mProgress.setDonut_progress(""+holder.mItem.getCount() * 100 / total);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(MessageDetailFragment.ARG_ITEM_ID, holder.mItem.getAdress());
                        MessageDetailFragment fragment = new MessageDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.message_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MessageDetailActivity.class);
                        intent.putExtra(MessageDetailFragment.ARG_ITEM_ID, holder.mItem.getAdress());

                        context.startActivity(intent);
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
            public GroupedMessage mItem;
            public final DonutProgress mProgress;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
                mProgress = (DonutProgress) view.findViewById(R.id.donut_progress);
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

}
