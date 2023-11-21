package com.ecom.fyp2023;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHandler extends SQLiteOpenHelper{


    private static final String DB_NAME = "FYPDB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "Users";
    private static final String ID_COL = "id";
    private static final String USER_NAME_COL = "username";
    // below variable id for our course duration column.
    private static final String USER_EMAIL_COL = "User Email Address";

    private static final String TABLE1_NAME = "Projects";

    private static final String ID1_COL = "id";
    private static final String PROJECT_TITLE_COL = "Project Title";
    private static final String PROJECT_DESCRIPTION_COL = "Description";
    private static final String PROJECT_START_DATE_COL = "Start Date";
    private static final String PROJECT_END_DATE_COL = "End Date";


    private static final String PROJECT_PRIORITY_COL = "Priority";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private final String CREATETABLE = " CREATE TABLE " + TABLE_NAME + " ( "
            + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USER_NAME_COL + " TEXT, "
            + USER_EMAIL_COL + " TEXT); ";

    private final String CREATETABLE1 = " CREATE TABLE " + TABLE1_NAME + " ( "
            + ID1_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PROJECT_TITLE_COL + " TEXT, "
            + PROJECT_DESCRIPTION_COL +" TEXT, "
            + PROJECT_START_DATE_COL + " TEXT, "
            + PROJECT_END_DATE_COL + " TEXT, "
            + PROJECT_PRIORITY_COL + " TEXT); ";

    // creating a constructor for our database handler.

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATETABLE);
        db.execSQL(CREATETABLE1);
    }

    // this method is use to add new course to our sqlite database.
    public void addUser(String username, String userEmail) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_NAME_COL, username);
        values.put(USER_EMAIL_COL, userEmail);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void addProject(String title, String desc, String startD, String endD, String priority) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(PROJECT_TITLE_COL, title);
        values.put(PROJECT_DESCRIPTION_COL, desc);
        values.put(PROJECT_START_DATE_COL, startD);
        values.put(PROJECT_END_DATE_COL, endD);
        values.put(PROJECT_PRIORITY_COL, priority );
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE1_NAME);
        onCreate(db);
    }
}


