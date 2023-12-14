package com.example.laba4genba;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Megabytes.db";
    public static int SCHEMA = 1;
    public static final String TABLE = "songs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MUSICIAN = "musician";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TIMEADD = "timeAdd";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS songs ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MUSICIAN + " TEXT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_TIMEADD + " TEXT);"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }
}