package com.example.WWMAPulse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 25-07-2015.
 */
public class FriendsDBAdapter {
    private static final String F_DATABASE_NAME = "data2";
    private static final String F_DATABASE_TABLE = "friends";
    private static final int F_DATABASE_VERSION = 7;


    public static final String KEYF_NAME = "name";
    public static final String KEYF_ROWID = "_id";
    public static final String KEYF_USERID = "userid";

    private static final String TAG = "FriendsAdapter";
    private DatabaseHelper mDbHelper2;
    private SQLiteDatabase mDb2;

    /**
     * Database creation SQL statement
     */
    private static final String F_DATABASE_CREATE =
            "create table " + F_DATABASE_TABLE + " ("
                    + KEYF_ROWID + " integer primary key autoincrement, "
                    + KEYF_NAME + " text not null, "
                    + KEYF_USERID + " text not null);";



    private final Context mCtx2;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, F_DATABASE_NAME, null, F_DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(F_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + F_DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public FriendsDBAdapter(Context ctx) {
        this.mCtx2 = ctx;
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
    public FriendsDBAdapter open() throws SQLException {
        mDbHelper2 = new DatabaseHelper(mCtx2);
        mDb2 = mDbHelper2.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper2.close();
    }


    /**
     * Create a new reminder using the title, description and reminder date time provided.
     * If the reminder is successfully created return the new rowId
     * for that reminder, otherwise return a -1 to indicate failure.
     *
     * @param name the description of the reminder
     * @return rowId or -1 if failed
     */
    public long createFriends(String name, String userid) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEYF_NAME, name);
        initialValues.put(KEYF_USERID, userid);
        return mDb2.insert(F_DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the reminder with the given rowId
     *
     * @param rowId id of reminder to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteFriends(long rowId) {

        return mDb2.delete(F_DATABASE_TABLE, KEYF_ROWID + "=" + rowId, null) > 0;
    }


    /**
     * Return a Cursor over the list of all reminders in the database
     *
     * @return Cursor over all reminders
     */
    public Cursor fetchAllFriends() {

        return mDb2.query(F_DATABASE_TABLE, new String[] {KEYF_ROWID,
                KEYF_NAME, KEYF_USERID}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the reminder that matches the given rowId
     *
     * @param rowId id of reminder to retrieve
     * @return Cursor positioned to matching reminder, if found
     * @throws SQLException if reminder could not be found/retrieved
     */
    public Cursor fetchFriends(long rowId) throws SQLException {

        Cursor mCursor =

                mDb2.query(true, F_DATABASE_TABLE, new String[] {KEYF_ROWID,
                                KEYF_NAME,KEYF_USERID}, KEYF_ROWID + "=" + rowId, null,
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
     * @param body value to set reminder body to
     * @return true if the reminder was successfully updated, false otherwise
     */
    public boolean updateFriends(long rowId, String body, String userid) {
        ContentValues args = new ContentValues();
        args.put(KEYF_NAME, body);
        args.put(KEYF_USERID,userid);
        return mDb2.update(F_DATABASE_TABLE, args, KEYF_ROWID + "=" + rowId, null) > 0;
    }

    public List<String> getAllLabels(){
        List<String> labels = new ArrayList<String>();

        // Select All Query
        String selectQuery = " SELECT " + KEYF_NAME + " FROM "+ F_DATABASE_TABLE;

        SQLiteDatabase mDb2ReadableDatabase = mDbHelper2.getReadableDatabase();
        Cursor cursor = mDb2ReadableDatabase.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        mDb2ReadableDatabase.close();

        // returning labels
        return labels;
    }

    public String SearchTable(String rem)
    {
        String user_id = "";
        String query = "SELECT userid FROM " + F_DATABASE_TABLE + " WHERE " + KEYF_NAME + " = '" + rem + "';";

        mDbHelper2 = new DatabaseHelper(mCtx2);
        mDb2 = mDbHelper2.getReadableDatabase();

        Cursor c = mDb2.rawQuery(query, null);

        if(c != null) {
            if(c.moveToFirst()) {
                do {
                    user_id = c.getString(c.getColumnIndex(KEYF_USERID));
                } while (c.moveToNext());
            }
        }

        c.close();
        return user_id;
    }

}

