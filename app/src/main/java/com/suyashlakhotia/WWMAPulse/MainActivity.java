package com.suyashlakhotia.WWMAPulse;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.razer.android.nabuopensdk.AuthCheckCallback;
import com.razer.android.nabuopensdk.NabuOpenSDK;
import com.razer.android.nabuopensdk.interfaces.NabuAuthListener;
import com.razer.android.nabuopensdk.interfaces.PulseListener;
import com.razer.android.nabuopensdk.interfaces.UserIDListener;
import com.razer.android.nabuopensdk.models.NabuBand;
import com.razer.android.nabuopensdk.models.PulseData;
import com.razer.android.nabuopensdk.models.Scope;

import java.util.Calendar;


public class MainActivity extends ListActivity {

    static NabuOpenSDK nabuSDK = null;

    private Button btnLogin;
    private Button btnPulse;
    private Button btnCheckApp;
    private Button btnGetUserId;
    private Button btnClear;
    private Button btnAddRem;

    private TextView tvResult;

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private RemindersDBAdapter mDbHelper;
    private FriendsDBAdapter mDbHelper2;

    StringBuilder builder;

    private NabuBand selectedBand = null;

    public void PlaceholderFragment() {
        builder = new StringBuilder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new RemindersDBAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());

        mDbHelper2 = new FriendsDBAdapter(this);
        mDbHelper2.open();

        btnLogin = (Button) findViewById(R.id.button1);
        btnPulse = (Button) findViewById(R.id.button5_1);
        btnCheckApp = (Button) findViewById(R.id.buttonCheckApp);
        btnGetUserId = (Button) findViewById(R.id.btnUserID);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnAddRem = (Button) findViewById(R.id.btnAddRem);

        tvResult = (TextView) findViewById(R.id.textView2);

        nabuSDK = NabuOpenSDK.getInstance(this);

        onActivityCreated(savedInstanceState);

        startService(new Intent(this, PulseReceiver.class));
    }

    private void fillData() {
        Cursor remindersCursor = mDbHelper.fetchAllReminders();
        startManagingCursor(remindersCursor);

        String[] from = new String[]{RemindersDBAdapter.KEY_REMINDER};

        int[] to = new int[]{R.id.text1};

        SimpleCursorAdapter reminders =
                new SimpleCursorAdapter(this, R.layout.reminder_row, remindersCursor, from, to);
        setListAdapter(reminders);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.long_press, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_delete:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteReminder(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createReminder() {
        Intent i = new Intent(this, AddReminderActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, AddReminderActivity.class);
        i.putExtra(RemindersDBAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

    public void setResult(String s) {
        tvResult.setText(s);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        getMyId();

        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nabuSDK.initiate(MainActivity.this, "37d2c19f88f8068b8357d306614b565c87229cce", new String[]{Scope.COMPLETE}, new NabuAuthListener() {

                    @Override
                    public void onAuthSuccess(String arg0) {

                        Log.e("Authentication Success", arg0);
                        builder = new StringBuilder();
                        builder.append(arg0);
                        setResult(builder.toString());

                    }

                    @Override
                    public void onAuthFailed(String arg0) {

                        Log.e("Authentication Failed", arg0);
                        builder = new StringBuilder();
                        builder.append(arg0);
                        setResult(builder.toString());
                    }
                });

            }
        });

        btnPulse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                queryPulseData();

            }
        });

        btnCheckApp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkApp();
            }
        });

        btnGetUserId.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyId();
            }
        });

        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearScrn();
            }
        });

        btnAddRem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createReminder();
            }
        });
    }

    /**
     * Query Pulse data.
     */
    private void queryPulseData() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, -1);
        nabuSDK.getPulseData(this, c.getTimeInMillis(), System.currentTimeMillis(), new PulseListener() {

            @Override
            public void onReceiveFailed(String arg0) {
                builder = new StringBuilder();

                builder.append(arg0);

                if (builder.length() == 0)
                    builder.append("Receive Failed");

                setResult(builder.toString());

            }

            @Override
            public void onReceiveData(PulseData[] arg0) {
                builder = new StringBuilder();

                for (PulseData pulse : arg0) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(pulse.timeStamp * 1000);
                    builder.append(Integer.toString(c.get(Calendar.DATE)) + Integer.toString(c.get(Calendar.HOUR_OF_DAY)) + Integer.toString(c.get(Calendar.MINUTE)));
                    builder.append("\n");
                    builder.append("-------------------------");
                    builder.append("\n");
                    for (String userID : pulse.userIDs) {
                        builder.append(userID);
                        builder.append("\n");
                    }
                    builder.append("-------------------------");
                    builder.append("\n");
                }

                if (builder.length() == 0)
                    builder.append("No Nabus Nearby");

                setResult(builder.toString());

            }
        });

        Log.e("Manual Pulse", "Done");
    }

    /**
     * Check App Authorization.
     */
    private void checkApp() {
        nabuSDK.checkAppAuthorized(this.getApplicationContext(), new AuthCheckCallback() {

            @Override
            public void onSuccess(boolean isAuthorized) {
                // LOGIN SUCCESSFUL

                builder = new StringBuilder();
                builder.append("isAuthorized:");
                builder.append(Boolean.toString(isAuthorized));

                if (builder.length() == 0)
                    builder.append("No Result");

                setResult(builder.toString());
            }

            @Override
            public void onFailed(String errorMessage) {
                // LOGIN FAILED
                builder = new StringBuilder();

                builder.append(errorMessage);

                if (builder.length() == 0)
                    builder.append("No Result");

                setResult(builder.toString());

            }
        });
    }

    /**
     * Get User ID.
     */
    protected void getMyId() {
        nabuSDK.getCurrentUserID(this, new UserIDListener() {

            @Override
            public void onReceiveFailed(String errorMessage) {
                tvResult.setText("Error: " + errorMessage);

            }

            @Override
            public void onReceiveData(String userID) {
                tvResult.setText(userID);
            }
        });
    }

    /**
     * Clear Result TV.
     */
    protected void clearScrn() {
        tvResult.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        nabuSDK.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nabuSDK.onPause(this);
    }

    @Override
    protected void onDestroy() {
        Log.e("Destroyed", "Destroyed");
        nabuSDK.onDestroy(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

