package com.example.om.myapplicationreg_new.Activities;

/**
 * Created by om on 4/8/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by gpw on 3/22/2016.
 */
public class DBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_NUMBER = "number";

    private static final String TAG = "DBAdapter";
    private static final String DATABASE_NAME = "MyDB";
    private static final String DATABASE_TABLE = "contacts";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "create table contacts (_id integer primary key autoincrement, "
                    + "mobile integer not null, number integer not null);";
    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }


    //---opens the database---
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }


    //---closes the database---
    public void close()
    {
        DBHelper.close();
    }


    //---insert a contact into the database---
    public long insertContact(String mobile, String number)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MOBILE, mobile);
        initialValues.put(KEY_NUMBER, number);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }



    //---retrieves a particular contact---
    public Cursor getContact(long rowId) throws SQLException
    {
        Cursor mCursor =db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_MOBILE, KEY_NUMBER}, KEY_MOBILE + "=" + rowId, null,
                null, null, null, null);




        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    //---updates a contact---
    public boolean updateContact(long rowId, String mobile, String number)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_MOBILE, mobile);
        args.put(KEY_NUMBER, number);
        return db.update(DATABASE_TABLE, args, KEY_MOBILE + "=" + rowId, null) > 0;
    }
}