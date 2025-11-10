package com.example.hike_app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class AddHikeActivity extends AppCompatActivity {
    EditText etName, etLocation, etDate, etLength, etDescription, etWeather, etGroupSize;
    Spinner spParking, spDifficulty;
    Button btnSave;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hike);

        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etLength = findViewById(R.id.etLength);
        etDescription = findViewById(R.id.etDescription);
        etWeather = findViewById(R.id.etWeather);
        etGroupSize = findViewById(R.id.etGroupSize);
        spParking = findViewById(R.id.spParking);
        spDifficulty = findViewById(R.id.spDifficulty);
        btnSave = findViewById(R.id.btnSave);

        // GÁN ADAPTER CHO SPINNER (FIX NPE)
        ArrayAdapter<CharSequence> parkingAdapter = ArrayAdapter.createFromResource(
                this, R.array.parking_options, android.R.layout.simple_spinner_item);
        parkingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spParking.setAdapter(parkingAdapter);

        ArrayAdapter<CharSequence> diffAdapter = ArrayAdapter.createFromResource(
                this, R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(diffAdapter);

        db = new DatabaseHelper(this);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String length = etLength.getText().toString().trim();
            String parking = spParking.getSelectedItem() != null ? spParking.getSelectedItem().toString() : "";
            String difficulty = spDifficulty.getSelectedItem() != null ? spDifficulty.getSelectedItem().toString() : "";
            String desc = etDescription.getText().toString().trim();
            String weather = etWeather.getText().toString().trim();
            String groupSize = etGroupSize.getText().toString().trim();

            if (name.isEmpty() || location.isEmpty() || date.isEmpty() || length.isEmpty()
                    || parking.isEmpty() || difficulty.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            db.insertHike(name, location, date, parking, length, difficulty, desc, weather, groupSize);

            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage(
                            "Name: " + name + "\n" +
                                    "Location: " + location + "\n" +
                                    "Date: " + date + "\n" +
                                    "Parking: " + parking + "\n" +
                                    "Length: " + length + " km\n" +
                                    "Difficulty: " + difficulty + "\n" +
                                    "Description: " + desc + "\n" +
                                    "Weather: " + weather + "\n" +
                                    "Group size: " + groupSize
                    )
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Edit", null)
                    .show();
        });
    }
}
