package com.example.WWMAPulse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class RemindersDBAdapter {
    // Database Related Constants

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "reminders";
    private static final int DATABASE_VERSION = 7;

    public static final String KEY_REMINDER = "reminder";
    public static final String KEY_NAME = "name";
    public static final String KEY_USERID = "userid";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_REMINDED = "reminded";


    private static final String TAG = "ReminderDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper2;
    private SQLiteDatabase mDb2;

    /**
     * Database creation SQL statement
     */
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + " ("
                    + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_REMINDER + " text not null, "
                    + KEY_NAME + " text not null, "
                    + KEY_USERID + " text not null, "
                    + KEY_REMINDED + " bool);";



    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public RemindersDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public RemindersDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new reminder using the title, description and reminder date time provided.
     * If the reminder is successfully created return the new rowId
     * for that reminder, otherwise return a -1 to indicate failure.
     *
     * @param title the title of the reminder
     * @param name the description of the reminder
     * @param reminderDateTime the date and time the reminder should remind the user
     * @return rowId or -1 if failed
     */
    public long createReminder(String title, String name, String userid) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_REMINDER, title);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_USERID, userid);
        initialValues.put(KEY_REMINDED, 0);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the reminder with the given rowId
     *
     * @param rowId id of reminder to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteReminder(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all reminders in the database
     *
     * @return Cursor over all reminders
     */
    public Cursor fetchAllReminders() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_REMINDER,
                KEY_NAME, KEY_USERID, KEY_REMINDED}, null, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the reminder that matches the given rowId
     *
     * @param rowId id of reminder to retrieve
     * @return Cursor positioned to matching reminder, if found
     * @throws SQLException if reminder could not be found/retrieved
     */
    public Cursor fetchReminder(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_REMINDER, KEY_NAME, KEY_USERID, KEY_REMINDED}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the reminder using the details provided. The reminder to be updated is
     * specified using the rowId, and it is altered to use the title, body and reminder date time
     * values passed in
     *
     * @param rowId id of reminder to update
     * @param title value to set reminder title to
     * @param body value to set reminder body to
     * @param reminderDateTime value to set the reminder time.
     * @return true if the reminder was successfully updated, false otherwise
     */
    public boolean updateReminder(long rowId, String title, String name, String userid) {
        ContentValues args = new ContentValues();
        args.put(KEY_REMINDER, title);
        args.put(KEY_NAME, name);
        args.put(KEY_USERID,userid);
        args.put(KEY_REMINDED, 0);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public int[] SearchReminder(String userid)
    {
        int[] id = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        String query = "SELECT _id FROM " + DATABASE_TABLE + " WHERE " + KEY_USERID + " = '" + userid + "';";

        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getReadableDatabase();

        Cursor c = mDb.rawQuery(query, null);

        if(c != null) {
            if(c.moveToFirst()) {
                int d = 0;
                do {
                    id[d] = (c.getInt(c.getColumnIndex(KEY_ROWID)));
                    d++;
                } while (c.moveToNext());
            }
        }

        c.close();
        return id;
    }

    public String getNotif(String rowid) throws SQLException {
        String query = "Select reminder FROM " + DATABASE_TABLE + " WHERE " + KEY_ROWID + " =  \"" + rowid + "\";";
        String remind = "";

        mDbHelper2 = new DatabaseHelper(mCtx);
        mDb2 = mDbHelper2.getWritableDatabase();

        Cursor cursor = mDb2.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            remind = cursor.getString(0);
            cursor.close();
        }
        return remind;
    }

    public boolean setasReminded(int rowid) throws SQLException{
        ContentValues args = new ContentValues();
        args.put(KEY_REMINDED, 1);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowid, null) > 0;
    }

    public int getReminded(int rowid) throws SQLException{
        int reminded = 0;

        mDbHelper2 = new DatabaseHelper(mCtx);
        mDb2 = mDbHelper2.getWritableDatabase();

        Cursor cursor =

                mDb2.query(true, DATABASE_TABLE, new String[] {KEY_REMINDED}, KEY_ROWID + "=" + rowid, null,
                        null, null, null, null);


        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            reminded = cursor.getInt(0);
            cursor.close();
        }

        Log.d("Reminded Val", Integer.toString(reminded));

        return reminded;
    }


}
