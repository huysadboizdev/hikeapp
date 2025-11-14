
package com.example.hike_app;

import android.content.Intent; // <-- Đã thêm import
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SearchHikeActivity extends AppCompatActivity {
    EditText etQuery;
    Button btnSearch, btnBack;
    TextView tvResult;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_search_hike);

        etQuery = findViewById(R.id.etQuery);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);
        tvResult = findViewById(R.id.tvResult);
        db = new DatabaseHelper(this);

        btnSearch.setOnClickListener(v -> doSearch());


        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SearchHikeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

    }

    private void doSearch() {
        String q = etQuery.getText().toString().trim();
        if (q.isEmpty()) {
            Toast.makeText(this, "Please enter a hike name to search.", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor c = null;
        StringBuilder sb = new StringBuilder();
        try {
            c = db.getReadableDatabase().rawQuery(
                    "SELECT * FROM hikes WHERE name LIKE ? COLLATE NOCASE",
                    new String[]{"%" + q + "%"}
            );

            if (c != null && c.moveToFirst()) {
                do {
                    String id = getSafe(c, "id");
                    String name = getSafe(c, "name");
                    String location = getSafe(c, "location");
                    String date = getSafe(c, "date");
                    String parking = getSafe(c, "parking");
                    String length = getSafe(c, "length");
                    String difficulty = getSafe(c, "difficulty");
                    String description = getSafe(c, "description");
                    String weather = getSafe(c, "weather");
                    String group = getSafe(c, "groupsize");

                    sb.append(" ID: ").append(id)
                            .append("\n Name: ").append(name)
                            .append("\nLocation: ").append(location)
                            .append("\n Date: ").append(date)
                            .append("\n Parking: ").append(parking)
                            .append("\n Length: ").append(length)
                            .append("\n Difficulty: ").append(difficulty)
                            .append("\n Group size: ").append(group)
                            .append("\n Weather: ").append(weather)
                            .append("\n Description: ").append(description)
                            .append("\n\n──────────────────────────────\n\n");
                } while (c.moveToNext());
            } else {
                sb.append("No results found for “").append(q).append("”.");
            }

        } catch (Exception e) {
            sb.setLength(0);
            sb.append("Error: ").append(e.getMessage());
        } finally {
            if (c != null) c.close();
        }

        tvResult.setText(sb.toString());
    }


    private String getSafe(Cursor c, String colName) {
        if (c == null || colName == null) return "";
        try {
            int idx = c.getColumnIndex(colName);
            if (idx >= 0 && idx < c.getColumnCount()) {
                String v = c.getString(idx);
                return v == null ? "" : v.trim();
            }
        } catch (Exception ignored) {}
        return "";
    }
}