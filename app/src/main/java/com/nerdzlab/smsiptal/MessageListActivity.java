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
import android.view.Menu;
import android.view.MenuItem;
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


import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

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
    BranchUniversalObject branchUniversalObject;


    // This would be your own method where you've loaded the content for this page
    void contentLoaded() {
        // Initialize a Branch Universal Object for the page the user is viewing
        branchUniversalObject = new BranchUniversalObject();

        // Trigger a view on the content for analytics tracking
        branchUniversalObject.registerView();

        // List on Google App Indexing
        branchUniversalObject.listOnGoogleSearch(this);
    }

    // This is the function to handle sharing when a user clicks the share button
    void initiateSharing() {

        LinkProperties linkProperties = new LinkProperties()
                .setFeature("sharing");

        // Customize the appearance of your share sheet
        ShareSheetStyle shareSheetStyle = new ShareSheetStyle(this, "SMS Iptal", "Kampanya SMS İzinlerini İptal Edin: ")
                .setCopyUrlStyle(getResources().getDrawable(android.R.drawable.ic_menu_send), "Link kopyala", "Link kopyalandı!")
                .setMoreOptionStyle(getResources().getDrawable(android.R.drawable.ic_menu_search), "Daha Fazla")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER);

        // Show the share sheet for the content you want the user to share. A link will be automatically created and put in the message.
        branchUniversalObject.showShareSheet(this, linkProperties, shareSheetStyle, new Branch.BranchLinkShareListener() {
            @Override
            public void onShareLinkDialogLaunched() { }
            @Override
            public void onShareLinkDialogDismissed() { }
            @Override
            public void onChannelSelected(String channelName) { }
            @Override
            public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
                // The link will be available in sharedLink
            }
        });
    }


    /*
     * isSpam method makes regex check for common unwanted SMS templates
     */

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

    // Updates FB with the message content if not in DB Before
    private void writeNewMessage(String adress, String body, String sd, String rd, Boolean sendnow) {

        if(sendnow)
        {
            String key = mDatabase.child("messages").push().getKey();
            Message msg = new Message(adress, body, sd, rd,false);
            Map<String, Object> postValues = msg.toMap();

            adress = adress.replaceAll("[^A-Za-z0-9]", "");

            Map<String, Object> childUpdates = new HashMap<>();
            //childUpdates.put("/messages/" + key, postValues);
            childUpdates.put("/messagesbyadress/" + adress + "/" + key, postValues);

            mDatabase.updateChildren(childUpdates);

        }

        final String finalAdress = adress;
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.write_new_title)
                .content(R.string.write_new_content)
                .positiveText(R.string.agree)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        movetoDetailFragment(finalAdress);
                    }
                })
                .show();

    }


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

        contentLoaded();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                new MaterialDialog.Builder(view.getContext())
                        .title(R.string.info_title)
                        .content(R.string.info_content)
                        .positiveText(R.string.agree)
                        .show();
            }
        });

        recyclerView = findViewById(R.id.message_list);
        assert recyclerView != null;



        //Permission Handling above SDK M

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_item_share:

                initiateSharing();

                break;

        }
        return super.onOptionsItemSelected(item);
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
    protected void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchUniversalReferralInitListener() {
            @Override
            public void onInitFinished(BranchUniversalObject branchUniversalObject, LinkProperties linkProperties, BranchError error) {
                if (error == null && branchUniversalObject != null) {
                    // This code will execute when your app is opened from a Branch deep link, which
                    // means that you can route to a custom activity depending on what they clicked.
                    // In this example, we'll just print out the data from the link that was clicked.

                    Log.i("BranchTestBed", "title " + branchUniversalObject.getTitle());
                    Log.i("ContentMetaData", "metadata " + branchUniversalObject.getMetadata());
                }
            }
        }, this.getIntent().getData(), this);
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


                        final MaterialDialog md = new MaterialDialog.Builder(v.getContext())
                                .title(R.string.progress_dialog)
                                .content(R.string.please_wait)
                                .progress(true, 0)
                                .show();

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
                                    Message matchedButNotProcessedMessage = null;

                                    for (DataSnapshot savedmessage : dataSnapshot.getChildren()) {
                                        Message matchCandidate = savedmessage.getValue(Message.class);
                                        if( matchCandidate.getCancel_number() != null &&
                                                ITEMMAP.get(holder.mItem.getAdress()).getBody()
                                                        .contains(matchCandidate.getCancel_number())) {

                                            Log.d(TAG, "DATA MATCHED WITH : "+matchCandidate.getCancel_number());
                                            matchfound = true;
                                            matchedMessage = matchCandidate;
                                            break;
                                        }else if( matchCandidate.getBody().equalsIgnoreCase(ITEMMAP.get(holder.mItem.getAdress()).getBody()) &&
                                                matchCandidate.getSpam() == false){
                                            matchedButNotProcessedMessage = matchCandidate;

                                        }
                                    }
                                    md.dismiss();

                                    if(matchfound && matchedMessage != null){

                                        if(matchedMessage.getSpam()) {
                                            sendSMS(matchedMessage.getCancel_number(),
                                                    matchedMessage.getCancel_phrase(),
                                                    matchedMessage.getAdress());
                                        }


                                    }
                                    else {

                                        Log.d(TAG, "NO MATCH CREATING NEW ONE IF NOT THERE ALREADY ");

                                        if(matchedButNotProcessedMessage == null)
                                        {
                                            writeNewMessage(holder.mItem.getAdress(),
                                                    ITEMMAP.get(holder.mItem.getAdress()).getBody(),
                                                    ITEMMAP.get(holder.mItem.getAdress()).getSentDate(),
                                                    ITEMMAP.get(holder.mItem.getAdress()).getReceivedDate(),true);

                                        }else{
                                            writeNewMessage(holder.mItem.getAdress(),
                                                    ITEMMAP.get(holder.mItem.getAdress()).getBody(),
                                                    ITEMMAP.get(holder.mItem.getAdress()).getSentDate(),
                                                    ITEMMAP.get(holder.mItem.getAdress()).getReceivedDate(),false);

                                        }




                                    }

                                }else {
                                    md.dismiss();

                                    Log.d(TAG, "NO MATCH CREATING NEW ONE ");

                                    writeNewMessage(holder.mItem.getAdress(),
                                            ITEMMAP.get(holder.mItem.getAdress()).getBody(),
                                            ITEMMAP.get(holder.mItem.getAdress()).getSentDate(),
                                            ITEMMAP.get(holder.mItem.getAdress()).getReceivedDate(),
                                            true);

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


}
