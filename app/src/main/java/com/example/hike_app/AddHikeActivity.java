
package com.example.hike_app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AddHikeActivity extends AppCompatActivity {
    EditText etName, etLocation, etLength, etDescription, etWeather, etGroupSize;
    TextView tvDate;
    Switch swParking;
    Spinner spDifficulty;
    Button btnPickDate, btnSave, btnCancel;
    DatabaseHelper db;

    // if >0 => editing existing row
    long currentEditId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hike);

        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        tvDate = findViewById(R.id.tvDate);
        etLength = findViewById(R.id.etLength);
        etDescription = findViewById(R.id.etDescription);
        etWeather = findViewById(R.id.etWeather);
        etGroupSize = findViewById(R.id.etGroupSize);
        swParking = findViewById(R.id.swParking);
        spDifficulty = findViewById(R.id.spDifficulty);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> diffAdapter = ArrayAdapter.createFromResource(
                this, R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(diffAdapter);

        db = new DatabaseHelper(this);

        btnPickDate.setOnClickListener(v -> showDatePicker());

        // handle save (insert or update depending on currentEditId)
        btnSave.setOnClickListener(v -> onSaveClicked());

        // Cancel => back to main
        btnCancel.setOnClickListener(v -> {
            Intent back = new Intent(this, MainActivity.class);
            back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(back);
            finish();
        });

        // ----- If opened for editing, prefill fields -----
        Intent it = getIntent();
        if (it != null && it.hasExtra("edit_id")) {
            currentEditId = it.getLongExtra("edit_id", -1L);

            // read possible extras (ViewHikesActivity sends these keys)
            String name = it.hasExtra("name") ? it.getStringExtra("name") : null;
            String location = it.hasExtra("location") ? it.getStringExtra("location") : null;
            String date = it.hasExtra("date") ? it.getStringExtra("date") : null;

            // parking may be boolean extra or string; handle both
            boolean parkingBool = false;
            if (it.hasExtra("parking")) {
                try { parkingBool = it.getBooleanExtra("parking", false); }
                catch (Exception ignored) {
                    String p = it.getStringExtra("parking");
                    parkingBool = (p != null) && (p.equals("1") || p.equalsIgnoreCase("true") || p.equalsIgnoreCase("yes"));
                }
            } else {
                String p = it.hasExtra("parking_text") ? it.getStringExtra("parking_text") : null;
                parkingBool = (p != null) && (p.equals("1") || p.equalsIgnoreCase("true") || p.equalsIgnoreCase("yes"));
            }

            // length may be double extra or string
            String lengthStr = "";
            if (it.hasExtra("lengthKm")) {
                try {
                    double len = it.getDoubleExtra("lengthKm", 0.0);
                    if (len > 0) lengthStr = String.valueOf(len);
                } catch (Exception ignored) {}
            }
            if (lengthStr.isEmpty() && it.hasExtra("length")) {
                lengthStr = it.getStringExtra("length");
            }

            String difficulty = it.hasExtra("difficulty") ? it.getStringExtra("difficulty") : null;
            String description = it.hasExtra("description") ? it.getStringExtra("description") : null;
            String weather = it.hasExtra("weather") ? it.getStringExtra("weather") : null;

            // elevation and groupSize may be ints
            String groupSizeStr = "";
            if (it.hasExtra("groupSize")) {
                try { groupSizeStr = String.valueOf(it.getIntExtra("groupSize", 0)); } catch (Exception ignored) {}
            } else if (it.hasExtra("groupsize")) {
                groupSizeStr = it.getStringExtra("groupsize");
            }

            // apply to UI (null-checks)
            if (name != null) etName.setText(name);
            if (location != null) etLocation.setText(location);
            if (date != null) tvDate.setText(date);
            if (!lengthStr.isEmpty()) etLength.setText(lengthStr);
            swParking.setChecked(parkingBool);
            if (difficulty != null && !difficulty.isEmpty()) {
                // try to set spinner selection
                try {
                    ArrayAdapter adapter = (ArrayAdapter) spDifficulty.getAdapter();
                    int pos = adapter.getPosition(difficulty);
                    if (pos >= 0) spDifficulty.setSelection(pos);
                } catch (Exception ignored) {}
            }
            if (description != null) etDescription.setText(description);
            if (weather != null) etWeather.setText(weather);
            if (groupSizeStr != null && !groupSizeStr.isEmpty()) etGroupSize.setText(groupSizeStr);

            // change button label to Update
            btnSave.setText("Update");
        }
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dlg = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String mm = String.format("%02d", month + 1);
            String dd = String.format("%02d", dayOfMonth);
            tvDate.setText(dd + "/" + mm + "/" + year);
        }, y, m, d);
        dlg.show();
    }

    private void onSaveClicked() {
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date = tvDate.getText().toString().trim();
        String length = etLength.getText().toString().trim();
        String parking = swParking.isChecked() ? "Yes" : "No";
        String difficulty = spDifficulty.getSelectedItem() != null ? spDifficulty.getSelectedItem().toString() : "";
        String desc = etDescription.getText().toString().trim();
        String weather = etWeather.getText().toString().trim();
        String groupSize = etGroupSize.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty() || date.isEmpty() || length.isEmpty() || difficulty.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // If editing -> call updateHike; else insert new
        if (currentEditId > 0) {
            boolean ok = db.updateHike(currentEditId,
                    name, location, date,
                    parking, length, difficulty, desc, weather, groupSize);
            if (ok) {
                Toast.makeText(this, "Hike updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        long rowId = db.insertHike(name, location, date, parking, length, difficulty, desc, weather, groupSize);

        String message = "Name: " + name + "\n" +
                "Location: " + location + "\n" +
                "Date: " + date + "\n" +
                "Parking: " + parking + "\n" +
                "Length: " + length + " km\n" +
                "Difficulty: " + difficulty + "\n" +
                "Description: " + (desc.isEmpty() ? "(none)" : desc) + "\n" +
                "Weather: " + (weather.isEmpty() ? "(none)" : weather) + "\n" +
                "Group size: " + (groupSize.isEmpty() ? "(none)" : groupSize);

        new AlertDialog.Builder(this)
                .setTitle(rowId > 0 ? "Saved" : "Confirm")
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> {
                    if (rowId > 0) {
                        Toast.makeText(this, "Hike saved (id=" + rowId + ")", Toast.LENGTH_SHORT).show();
                        finish();
                    } else Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Edit", (d, w) -> d.dismiss())
                .show();
    }
}
