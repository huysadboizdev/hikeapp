package com.example.hike_app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "hike.db";
    public static final int DB_VERSION = 1;

    public DatabaseHelper(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE hikes(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "location TEXT," +
                "date TEXT," +
                "parking TEXT," +
                "length TEXT," +
                "difficulty TEXT," +
                "description TEXT," +
                "weather TEXT," +
                "groupsize TEXT)");

        db.execSQL("CREATE TABLE observations(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hike_id INTEGER," +
                "observation TEXT," +
                "time TEXT," +
                "comments TEXT," +
                "FOREIGN KEY(hike_id) REFERENCES hikes(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS hikes");
        db.execSQL("DROP TABLE IF EXISTS observations");
        onCreate(db);
    }

    public void insertHike(String n, String l, String d, String p, String len,
                           String diff, String desc, String w, String g) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", n);
        v.put("location", l);
        v.put("date", d);
        v.put("parking", p);
        v.put("length", len);
        v.put("difficulty", diff);
        v.put("description", desc);
        v.put("weather", w);
        v.put("groupsize", g);
        db.insert("hikes", null, v);
    }

    public Cursor getAllHikes() {
        return getReadableDatabase().rawQuery("SELECT * FROM hikes", null);
    }

    public void deleteHike(int id) {
        getWritableDatabase().delete("hikes", "id=?", new String[]{String.valueOf(id)});
    }

    public void resetDB() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM hikes");
        db.execSQL("DELETE FROM observations");
    }
}
