
package com.example.hike_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "hike.db";
    public static final int DB_VERSION = 1;

    public DatabaseHelper(Context c) {
        super(c.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS hikes(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "location TEXT," +
                "date TEXT," +
                "parking INTEGER," +  // store 0/1
                "length TEXT," +
                "difficulty TEXT," +
                "description TEXT," +
                "weather TEXT," +
                "groupsize TEXT" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS observations(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hike_id INTEGER," +
                "observation TEXT," +
                "time TEXT," +
                "comments TEXT," +
                "FOREIGN KEY(hike_id) REFERENCES hikes(id)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS hikes");
        db.execSQL("DROP TABLE IF EXISTS observations");
        onCreate(db);
    }

    public long insertHike(String name, String location, String date, String parking,
                           String length, String difficulty, String description,
                           String weather, String groupSize) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("location", location);
        v.put("date", date);
        v.put("parking", convertParkingToInt(parking));
        v.put("length", length);
        v.put("difficulty", difficulty);
        v.put("description", description);
        v.put("weather", weather);
        v.put("groupsize", groupSize);
        return db.insert("hikes", null, v);
    }


    public boolean updateHike(long id, String name, String location, String date, String parking,
                              String length, String difficulty, String description,
                              String weather, String groupSize) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("location", location);
        v.put("date", date);
        v.put("parking", convertParkingToInt(parking));
        v.put("length", length);
        v.put("difficulty", difficulty);
        v.put("description", description);
        v.put("weather", weather);
        v.put("groupsize", groupSize);
        int rows = db.update("hikes", v, "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteHike(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete("hikes", "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }


    public Cursor getAllHikes() {
        return getReadableDatabase().rawQuery("SELECT * FROM hikes ORDER BY id DESC", null);
    }


    public Cursor getHikeById(long id) {
        return getReadableDatabase().rawQuery("SELECT * FROM hikes WHERE id=?", new String[]{String.valueOf(id)});
    }

    // ---------------- OBSERVATIONS CRUD ----------------


    public long insertObservation(Long hikeId, String observation, String time, String comments) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        if (hikeId != null && hikeId > 0) v.put("hike_id", hikeId);
        v.put("observation", observation);
        v.put("time", time);
        v.put("comments", comments);
        return db.insert("observations", null, v);
    }

    public boolean updateObservation(long id, Long hikeId, String observation, String time, String comments) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        if (hikeId != null && hikeId > 0) v.put("hike_id", hikeId);
        v.put("observation", observation);
        v.put("time", time);
        v.put("comments", comments);
        int rows = db.update("observations", v, "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }


    public boolean deleteObservation(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete("observations", "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }


    public Cursor getAllObservations() {
        return getReadableDatabase().rawQuery("SELECT * FROM observations ORDER BY id DESC", null);
    }

    public Cursor getObservationsForHike(long hikeId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM observations WHERE hike_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(hikeId)}
        );
    }


    public Cursor getObservationById(long id) {
        return getReadableDatabase().rawQuery("SELECT * FROM observations WHERE id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteAllObservations() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM observations");
    }

    public void resetDB() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM hikes");
        db.execSQL("DELETE FROM observations");
    }


    private int convertParkingToInt(String value) {
        if (value == null) return 0;
        value = value.trim().toLowerCase();
        return (value.equals("1") || value.equals("true") || value.equals("yes") || value.equals("có")) ? 1 : 0;
    }

    public static String convertParkingToText(int val) {
        return val == 1 ? "Yes" : "No";
    }
}
