package com.example.hike_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnAddHike, btnViewHikes, btnAddObservation, btnSearch, btnReset;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        btnAddHike = findViewById(R.id.btnAddHike);
        btnViewHikes = findViewById(R.id.btnViewHikes);
        btnAddObservation = findViewById(R.id.btnAddObservation);
        btnSearch = findViewById(R.id.btnSearch);
        btnReset = findViewById(R.id.btnReset);

        btnAddHike.setOnClickListener(v ->
                startActivity(new Intent(this, AddHikeActivity.class)));

        btnViewHikes.setOnClickListener(v ->
                startActivity(new Intent(this, ViewHikesActivity.class)));

        btnAddObservation.setOnClickListener(v ->
                startActivity(new Intent(this, AddObservationActivity.class)));

        btnSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchHikeActivity.class)));

        btnReset.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Reset")
                    .setMessage("Delete all hike and observation data?")
                    .setPositiveButton("Yes", (d, w) -> {
                        db.resetDB();
                        Toast.makeText(this, "Database reset successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}
