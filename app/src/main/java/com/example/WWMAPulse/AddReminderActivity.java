package com.example.WWMAPulse;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class AddReminderActivity extends Activity {

    //
    // Dialog Constants
    //
    private static final int DATE_PICKER_DIALOG = 0;
    private static final int TIME_PICKER_DIALOG = 1;

    //
    // Date Format
    //
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";

    private EditText mReminderText;
    private Button mConfirmButton;
    private Button mAddPersonButton;
    private Long mRowId;
    private RemindersDBAdapter mDbHelper;
    private FriendsDBAdapter mDbHelper2;
    private FriendsDBAdapter mDb2;
    private Calendar mCalendar;
    private Spinner mPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new RemindersDBAdapter(this);
        mDbHelper2 = new FriendsDBAdapter(this);

        setContentView(R.layout.activity_add_reminder);

        mCalendar = Calendar.getInstance();
        mReminderText = (EditText) findViewById(R.id.editText);
        mPerson = (Spinner) findViewById(R.id.spinner);
        mConfirmButton = (Button) findViewById(R.id.button);
        mAddPersonButton = (Button) findViewById(R.id.button2);

        mRowId = savedInstanceState != null ? savedInstanceState.getLong(RemindersDBAdapter.KEY_ROWID)
                : null;

        mAddPersonButton.setOnClickListener(button2Listener);

        registerButtonListenersAndSetDefaultText();

        loadspinnerdata();
    }

    private void loadspinnerdata() {
        // Spinner Drop down elements
        mDbHelper2.open();
        List<String> labels = mDbHelper2.getAllLabels();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mPerson.setAdapter(dataAdapter);
    }

    private Button.OnClickListener button2Listener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            addnewFriend();
            finish();
        }};

    private void addnewFriend() {
        Intent i = new Intent(this, AddFriendActivity.class);
        startActivityForResult(i, 0);
    }

    private void setRowIdFromIntent() {
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(RemindersDBAdapter.KEY_ROWID)
                    : null;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper.open();
        setRowIdFromIntent();
        populateFields();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DATE_PICKER_DIALOG:
                return showDatePicker();
            case TIME_PICKER_DIALOG:
                return showTimePicker();
        }
        return super.onCreateDialog(id);
    }

    private DatePickerDialog showDatePicker() {


        DatePickerDialog datePicker = new DatePickerDialog(AddReminderActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        return datePicker;
    }

    private TimePickerDialog showTimePicker() {

        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
            }
        }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);

        return timePicker;
    }

    private void registerButtonListenersAndSetDefaultText() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveState();
                setResult(RESULT_OK);
                Toast.makeText(AddReminderActivity.this, "Reminder Saved.", Toast.LENGTH_LONG).show();
                finish();
            }

        });
    }

    private void populateFields()  {
        // Only populate the text boxes and change the calendar date
        // if the row is not null from the database.
        if (mRowId != null) {
            Cursor reminder = mDbHelper.fetchReminder(mRowId);
            startManagingCursor(reminder);
            mReminderText.setText(reminder.getString(
                    reminder.getColumnIndexOrThrow(RemindersDBAdapter.KEY_REMINDER)));
            mPerson.setSelection(((ArrayAdapter<String>)mPerson.getAdapter()).getPosition((reminder.getString(
                    reminder.getColumnIndexOrThrow(RemindersDBAdapter.KEY_NAME)))));
        } else {
            // This is a new task - add defaults from preferences if set.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultTitleKey = "Default Reminder.";
            String defaultTimeKey = "Default Person.";

            String defaultTitle = prefs.getString(defaultTitleKey, null);
            String defaultTime = prefs.getString(defaultTimeKey, null);

            if(defaultTitle != null)
                mReminderText.setText(defaultTitle);

            if(defaultTime != null)
                mCalendar.add(Calendar.MINUTE, Integer.parseInt(defaultTime));

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(RemindersDBAdapter.KEY_ROWID, mRowId);
    }



    private void saveState() {
        mDb2 = new FriendsDBAdapter(this);

        String title = mReminderText.getText().toString();
        String person_name = mPerson.getSelectedItem().toString();
        String bluetooth = mDb2.SearchTable(person_name);

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        String reminderDateTime = dateTimeFormat.format(mCalendar.getTime());

        if (mRowId == null) {

            long id = mDbHelper.createReminder(title, person_name, bluetooth);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateReminder(mRowId, title, person_name, bluetooth);
        }

    }


}
