package com.example.hike_app;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddObservationActivity extends AppCompatActivity {
    EditText etObservation, etComments;
    TextView tvTime;
    Button btnSaveObs;
    DatabaseHelper db;
    int hikeId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_observation);

        etObservation = findViewById(R.id.etObservation);
        etComments = findViewById(R.id.etComments);
        tvTime = findViewById(R.id.tvTime);
        btnSaveObs = findViewById(R.id.btnSaveObs);

        hikeId = getIntent().getIntExtra("hike_id", -1);
        db = new DatabaseHelper(this);

        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        tvTime.setText(currentTime);

        btnSaveObs.setOnClickListener(v -> {
            if (etObservation.getText().toString().isEmpty()) {
                Toast.makeText(this, "Observation required", Toast.LENGTH_SHORT).show();
                return;
            }
            db.getWritableDatabase().execSQL("INSERT INTO observations (hike_id,observation,time,comments) VALUES(?,?,?,?)",
                    new Object[]{hikeId, etObservation.getText().toString(), currentTime, etComments.getText().toString()});
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        });
    }
}
