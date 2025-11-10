package com.example.hike_app;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SearchHikeActivity extends AppCompatActivity {
    EditText etQuery;
    Button btnSearch;
    TextView tvResult;
    com.example.hike_app.DatabaseHelper db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_search_hike);

        etQuery = findViewById(R.id.etQuery);
        btnSearch = findViewById(R.id.btnSearch);
        tvResult = findViewById(R.id.tvResult);
        db = new DatabaseHelper(this);

        btnSearch.setOnClickListener(v -> {
            String q = etQuery.getText().toString();
            Cursor c = db.getReadableDatabase().rawQuery(
                    "SELECT * FROM hikes WHERE name LIKE ?", new String[]{"%" + q + "%"});
            StringBuilder sb = new StringBuilder();
            while (c.moveToNext()) {
                sb.append("ID: ").append(c.getInt(0))
                        .append("\nName: ").append(c.getString(1))
                        .append("\nLocation: ").append(c.getString(2))
                        .append("\nDate: ").append(c.getString(3))
                        .append("\n\n");
            }
            if (sb.length() == 0)
                sb.append("No results found.");
            tvResult.setText(sb.toString());
        });
    }
}
