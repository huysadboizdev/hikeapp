package com.example.hike_app;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ViewHikesActivity extends AppCompatActivity {
    ListView listView;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_view_hikes);

        listView = findViewById(R.id.listView);
        db = new DatabaseHelper(this);
        loadData();
    }

    void loadData() {
        Cursor c = db.getAllHikes();
        StringBuilder sb = new StringBuilder();
        while (c.moveToNext()) {
            sb.append("ID: ").append(c.getInt(0))
                    .append(" | Name: ").append(c.getString(1))
                    .append(" | Location: ").append(c.getString(2))
                    .append("\n");
        }
        TextView tv = new TextView(this);
        tv.setText(sb.toString());
        new AlertDialog.Builder(this)
                .setTitle("All Hikes")
                .setView(tv)
                .setPositiveButton("OK", null)
                .show();
    }
}
