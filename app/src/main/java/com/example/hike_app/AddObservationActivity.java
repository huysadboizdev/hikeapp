
package com.example.hike_app;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class AddObservationActivity extends AppCompatActivity {
    private TextView tvTime;
    private EditText etObservation, etComments;
    private Button btnSaveObs, btnBack;
    private ListView listObservations;

    private DatabaseHelper db;
    private ArrayList<Long> obsIds = new ArrayList<>();
    private ArrayList<String> obsItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    // if passed from intent to associate an observation with a hike
    private long filterHikeId = -1L;

    // if >=0 then we are editing this observation id
    private long editingObsId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_observation); // your single combined layout

        tvTime = findViewById(R.id.tvTime);
        etObservation = findViewById(R.id.etObservation);
        etComments = findViewById(R.id.etComments);
        btnSaveObs = findViewById(R.id.btnSaveObs);
        btnBack = findViewById(R.id.btnBack);
        listObservations = findViewById(R.id.listObservations);

        db = new DatabaseHelper(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, obsItems);
        listObservations.setAdapter(adapter);

        // read optional hike id from intent
        if (getIntent() != null && getIntent().hasExtra("hike_id")) {
            filterHikeId = getIntent().getLongExtra("hike_id", -1L);
        }

        // set current time
        tvTime.setText(nowDisplay());

        btnSaveObs.setOnClickListener(v -> onSaveClicked());
        btnBack.setOnClickListener(v -> finish());

        listObservations.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < obsIds.size()) {
                long obsId = obsIds.get(position);
                showItemOptions(obsId);
            }
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void onSaveClicked() {
        String obsText = etObservation.getText().toString().trim();
        String comments = etComments.getText().toString().trim();
        String timeText = tvTime.getText().toString().replaceFirst("^Time:\\s*", "").trim();
        if (timeText.isEmpty()) timeText = nowIso();

        if (obsText.isEmpty()) {
            etObservation.setError("Required");
            return;
        }

        ContentValues v = new ContentValues();
        if (filterHikeId > 0) v.put("hike_id", filterHikeId);
        v.put("observation", obsText);
        v.put("time", timeText);
        v.put("comments", comments);

        if (editingObsId > 0) {
            // update
            int rows = db.getWritableDatabase().update("observations", v, "id=?", new String[]{String.valueOf(editingObsId)});
            if (rows > 0) {
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                clearForm();
                loadData();
                editingObsId = -1;
                btnSaveObs.setText("Save Observation");
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            // insert
            long id = db.getWritableDatabase().insert("observations", null, v);
            if (id > 0) {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                clearForm();
                loadData();
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showItemOptions(long obsId) {
        // load full row to show summary
        Cursor c = null;
        try {
            c = db.getReadableDatabase().rawQuery(
                    "SELECT observation, time, comments, hike_id FROM observations WHERE id = ?",
                    new String[]{String.valueOf(obsId)}
            );
            if (c != null && c.moveToFirst()) {
                String observation = safeGetString(c, 0);
                String time = safeGetString(c, 1);
                String comments = safeGetString(c, 2);
                String msg = (time.isEmpty() ? "" : "Time: " + time + "\n\n") +
                        "Observation:\n" + (observation.isEmpty() ? "(none)" : observation) +
                        (comments.isEmpty() ? "" : "\n\nComments:\n" + comments);

                new AlertDialog.Builder(this)
                        .setTitle("Observation")
                        .setMessage(msg)
                        .setPositiveButton("Edit", (d, w) -> startEdit(obsId))
                        .setNegativeButton("Delete", (d, w) -> confirmDelete(obsId))
                        .setNeutralButton("Close", null)
                        .show();
            } else {
                Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error reading item", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }
    }

    private void startEdit(long obsId) {
        Cursor c = null;
        try {
            c = db.getReadableDatabase().rawQuery(
                    "SELECT observation, time, comments FROM observations WHERE id = ?",
                    new String[]{String.valueOf(obsId)}
            );
            if (c != null && c.moveToFirst()) {
                String observation = safeGetString(c, 0);
                String time = safeGetString(c, 1);
                String comments = safeGetString(c, 2);

                etObservation.setText(observation);
                etComments.setText(comments);
                tvTime.setText("Time: " + (time.isEmpty() ? nowIso() : time));

                editingObsId = obsId;
                btnSaveObs.setText("Update Observation");
                // scroll to top of form if needed (optional)
            } else {
                Toast.makeText(this, "Item not found for edit", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error preparing edit", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }
    }

    private void confirmDelete(long obsId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete observation?")
                .setMessage("This will permanently delete the observation.")
                .setPositiveButton("Delete", (d, w) -> {
                    int rows = db.getWritableDatabase().delete("observations", "id=?", new String[]{String.valueOf(obsId)});
                    if (rows > 0) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        // if we were editing this id, clear form
                        if (editingObsId == obsId) {
                            clearForm();
                            editingObsId = -1;
                            btnSaveObs.setText("Save Observation");
                        }
                        loadData();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadData() {
        obsIds.clear();
        obsItems.clear();

        Cursor c = null;
        try {
            if (filterHikeId > 0) {
                c = db.getReadableDatabase().rawQuery(
                        "SELECT id, time, observation FROM observations WHERE hike_id=? ORDER BY id DESC",
                        new String[]{String.valueOf(filterHikeId)});
            } else {
                c = db.getReadableDatabase().rawQuery(
                        "SELECT id, time, observation FROM observations ORDER BY id DESC", null);
            }

            if (c == null) {
                obsItems.add("Error querying observations.");
                adapter.notifyDataSetChanged();
                return;
            }

            if (!c.moveToFirst()) {
                obsItems.add("No observations found.");
                adapter.notifyDataSetChanged();
                return;
            }

            do {
                long id = safeGetLong(c, 0);
                String time = safeGetString(c, 1);
                String obs = safeGetString(c, 2);

                obsIds.add(id);
                String snippet = (time.isEmpty() ? "" : time + " • ") +
                        (obs == null ? "(no text)" : (obs.length() > 80 ? obs.substring(0, 80) + "…" : obs));
                obsItems.add(snippet);
            } while (c.moveToNext());
        } catch (Exception e) {
            obsItems.clear();
            obsItems.add("Error loading observations: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) c.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void clearForm() {
        etObservation.setText("");
        etComments.setText("");
        tvTime.setText(nowDisplay());
        editingObsId = -1;
        btnSaveObs.setText("Save Observation");
    }

    // helpers
    private String safeGetString(Cursor c, int idx) {
        try {
            if (idx >= 0 && idx < c.getColumnCount()) {
                String v = c.getString(idx);
                return v == null ? "" : v;
            }
        } catch (Exception ignored) {}
        return "";
    }

    private long safeGetLong(Cursor c, int idx) {
        try {
            if (idx >= 0 && idx < c.getColumnCount()) return c.getLong(idx);
        } catch (Exception ignored) {}
        return 0L;
    }

    private String nowIso() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private String nowDisplay() {
        return "Time: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}
