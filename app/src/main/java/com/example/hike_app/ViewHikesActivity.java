// File: app/src/main/java/com/example/hike_app/ViewHikeActivity.java
package com.example.hike_app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class ViewHikesActivity extends AppCompatActivity {
    ListView listView;
    DatabaseHelper db;
    HikeListAdapter adapter;
    List<HikeItem> items = new ArrayList<>();
    Button btnAdd, btnDeleteAll, btnBack;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_view_hikes);

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnBack = findViewById(R.id.btnBack);

        db = new DatabaseHelper(this);

        adapter = new HikeListAdapter();
        listView.setAdapter(adapter);

        // row click -> details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < items.size()) showDetailsDialog(items.get(position));
        });

        // Add -> open AddHikeActivity
        btnAdd.setOnClickListener(v -> startActivity(new Intent(ViewHikesActivity.this, AddHikeActivity.class)));

        // Delete all -> confirm -> reset db
        btnDeleteAll.setOnClickListener(v -> new AlertDialog.Builder(ViewHikesActivity.this)
                .setTitle("Delete all hikes?")
                .setMessage("This will permanently remove all hike records. Continue?")
                .setPositiveButton("Delete All", (d, w) -> {
                    db.resetDB();
                    Toast.makeText(ViewHikesActivity.this, "All hikes deleted", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show());

        // Back -> return to MainActivity (clear above)
        btnBack.setOnClickListener(v -> {
            Intent it = new Intent(ViewHikesActivity.this, MainActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(it);
            finish();
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    void loadData() {
        items.clear();

        Cursor c = null;
        try {
            c = db.getAllHikes();
            if (c == null) {
                Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                return;
            }

            if (c.moveToFirst()) {
                do {
                    long id = safeGetLong(c, 0);
                    String name = safeGetString(c, 1);
                    String location = safeGetString(c, 2);

                    // date / description
                    String date = firstNonEmpty(
                            getColumnValueSafely(c, "date", -1),
                            getColumnValueSafely(c, "hike_date", -1),
                            getColumnValueSafely(c, "day", -1)
                    );
                    String description = firstNonEmpty(
                            getColumnValueSafely(c, "description", -1),
                            getColumnValueSafely(c, "notes", -1)
                    );

                    // parking: prefer reading integer column if present
                    boolean parking = false;
                    int parkingIdx = c.getColumnIndex("parking");
                    if (parkingIdx >= 0) {
                        try { parking = c.getInt(parkingIdx) == 1; } catch (Exception ex) { /*fallback*/ }
                    } else {
                        String parkingStr = firstNonEmpty(
                                getColumnValueSafely(c, "parking", -1),
                                getColumnValueSafely(c, "has_parking", -1)
                        );
                        parking = "1".equals(parkingStr) || "true".equalsIgnoreCase(parkingStr) || "yes".equalsIgnoreCase(parkingStr);
                    }

                    // length: try multiple names
                    String lengthStr = firstNonEmpty(
                            getColumnValueSafely(c, "length", -1),
                            getColumnValueSafely(c, "lengthKm", -1),
                            getColumnValueSafely(c, "len", -1),
                            findColumnValueByContains(c, "length"),
                            findColumnValueByContains(c, "len")
                    );
                    double length = tryParseDouble(lengthStr);

                    // group size
                    String groupStr = firstNonEmpty(
                            getColumnValueSafely(c, "groupsize", -1),
                            getColumnValueSafely(c, "groupSize", -1),
                            getColumnValueSafely(c, "group_size", -1),
                            getColumnValueSafely(c, "group", -1),
                            findColumnValueByContains(c, "group")
                    );
                    int groupSize = tryParseInt(groupStr);

                    String difficulty = firstNonEmpty(
                            getColumnValueSafely(c, "difficulty", -1),
                            getColumnValueSafely(c, "level", -1)
                    );

                    int elevation = tryParseInt(firstNonEmpty(
                            getColumnValueSafely(c, "elevationGain", -1),
                            getColumnValueSafely(c, "elevation", -1),
                            findColumnValueByContains(c, "elevation")
                    ));

                    String trailType = firstNonEmpty(
                            getColumnValueSafely(c, "trailType", -1),
                            getColumnValueSafely(c, "trail_type", -1)
                    );

                    // ===== THAY ĐỔI 2: ĐỌC DỮ LIỆU WEATHER =====
                    String weather = firstNonEmpty(
                            getColumnValueSafely(c, "weather", -1)
                    );
                    // ======================================

                    HikeItem hi = new HikeItem(id, name, location, date, parking, length, difficulty, description, elevation, trailType, groupSize, weather); // <-- Đã thêm 'weather'
                    items.add(hi);
                } while (c.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        } finally {
            if (c != null) c.close();
        }

        adapter.notifyDataSetChanged();
    }

    // safe getters
    private String safeGetString(Cursor c, int index) {
        try {
            if (index >= 0 && index < c.getColumnCount()) {
                String v = c.getString(index);
                return v == null ? "" : v;
            }
        } catch (Exception ignored) {}
        return "";
    }

    private long safeGetLong(Cursor c, int index) {
        try {
            if (index >= 0 && index < c.getColumnCount()) {
                return c.getLong(index);
            }
        } catch (Exception ignored) {}
        return 0L;
    }

    // tries to read column by name; if not present and fallbackIndex>=0 uses that index.
    private String getColumnValueSafely(Cursor c, String columnName, int fallbackIndex) {
        try {
            int idx = c.getColumnIndex(columnName);
            if (idx >= 0) {
                String v = c.getString(idx);
                return v == null ? "" : v;
            } else if (fallbackIndex >= 0 && fallbackIndex < c.getColumnCount()) {
                String v = c.getString(fallbackIndex);
                return v == null ? "" : v;
            }
        } catch (Exception ignored) {}
        return "";
    }

    // find first column whose name contains `substr` (case-insensitive) and return its value
    private String findColumnValueByContains(Cursor c, String substr) {
        if (c == null || c.getColumnCount() == 0 || substr == null) return "";
        substr = substr.toLowerCase();
        try {
            for (int i = 0; i < c.getColumnCount(); i++) {
                String col = c.getColumnName(i);
                if (col == null) continue;
                String lower = col.toLowerCase();
                if (lower.contains(substr)) {
                    String v = c.getString(i);
                    return v == null ? "" : v;
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String firstNonEmpty(String... vals) {
        if (vals == null) return "";
        for (String s : vals) if (s != null && !s.trim().isEmpty()) return s.trim();
        return "";
    }

    private double tryParseDouble(String s) {
        if (s == null || s.isEmpty()) return 0;
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private int tryParseInt(String s) {
        if (s == null || s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    // ===== THAY ĐỔI 1: CẬP NHẬT HIKEITEM MODEL =====
    static class HikeItem {
        long id;
        String name, location, date, difficulty, description, trailType, weather; // <-- Đã thêm 'weather'
        boolean parking;
        double length;
        int elevation;
        int groupSize;

        HikeItem(long id, String name, String location, String date, boolean parking,
                 double length, String difficulty, String description, int elevation, String trailType, int groupSize,
                 String weather) { // <-- Đã thêm 'weather' vào constructor
            this.id = id;
            this.name = name;
            this.location = location;
            this.date = date;
            this.parking = parking;
            this.length = length;
            this.difficulty = difficulty;
            this.description = description;
            this.elevation = elevation;
            this.trailType = trailType;
            this.groupSize = groupSize;
            this.weather = weather; // <-- Đã thêm
        }
    }
    // ===========================================

    // Custom adapter for row with buttons
    class HikeListAdapter extends BaseAdapter {
        LayoutInflater inflater = LayoutInflater.from(ViewHikesActivity.this);

        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return items.get(position).id; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VH vh;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_hike, parent, false);
                vh = new VH();
                vh.tvName = convertView.findViewById(R.id.tvName);
                vh.tvDetails = convertView.findViewById(R.id.tvDetails);
                vh.btnView = convertView.findViewById(R.id.btnView);
                vh.btnEdit = convertView.findViewById(R.id.btnEdit);
                vh.btnDelete = convertView.findViewById(R.id.btnDelete);
                // ensure buttons don't steal focus
                vh.btnView.setFocusable(false);
                vh.btnView.setFocusableInTouchMode(false);
                vh.btnEdit.setFocusable(false);
                vh.btnEdit.setFocusableInTouchMode(false);
                vh.btnDelete.setFocusable(false);
                vh.btnDelete.setFocusableInTouchMode(false);
                convertView.setTag(vh);
            } else {
                vh = (VH) convertView.getTag();
            }

            final HikeItem hi = items.get(position);

            vh.tvName.setText(hi.name != null && !hi.name.isEmpty() ? hi.name : "(no name)");

            // Format length
            String lengthStr = "";
            if (hi.length > 0.0001) lengthStr = String.format("%.1f km", hi.length);

            StringBuilder details = new StringBuilder();
            if (hi.location != null && !hi.location.isEmpty()) details.append(hi.location);
            if (hi.date != null && !hi.date.isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append(hi.date);
            }
            if (!lengthStr.isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append(lengthStr);
            }
            // append group size if present
            if (hi.groupSize > 0) {
                if (details.length() > 0) details.append(" • ");
                details.append("Group: ").append(hi.groupSize);
            }

            vh.tvDetails.setText(details.toString());

            vh.btnView.setOnClickListener(v -> showDetailsDialog(hi));
            vh.btnEdit.setOnClickListener(v -> launchEdit(hi));
            vh.btnDelete.setOnClickListener(v -> confirmDelete(hi));

            return convertView;
        }

        class VH {
            TextView tvName, tvDetails;
            Button btnView, btnEdit, btnDelete;
        }
    }

    // ===== THAY ĐỔI 3: HIỂN THỊ TRONG DIALOG CHI TIẾT =====
    private void showDetailsDialog(HikeItem hi) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(hi.name).append("\n");
        sb.append("Location: ").append(hi.location).append("\n");
        if (hi.date != null && !hi.date.isEmpty()) sb.append("Date: ").append(hi.date).append("\n");
        sb.append("Parking: ").append(hi.parking ? "Yes" : "No").append("\n");
        if (hi.length > 0.0001) sb.append("Length: ").append(String.format("%.1f km", hi.length)).append("\n");
        if (hi.groupSize > 0) sb.append("Group size: ").append(hi.groupSize).append("\n");
        if (hi.difficulty != null && !hi.difficulty.isEmpty()) sb.append("Difficulty: ").append(hi.difficulty).append("\n");
        if (hi.elevation > 0) sb.append("Elevation: ").append(hi.elevation).append(" m\n");
        if (hi.trailType != null && !hi.trailType.isEmpty()) sb.append("Trail: ").append(hi.trailType).append("\n");

        // --- DÒNG ĐÃ THÊM ---
        if (hi.weather != null && !hi.weather.isEmpty()) sb.append("Weather: ").append(hi.weather).append("\n");
        // ---------------------

        if (hi.description != null && !hi.description.isEmpty()) sb.append("Notes: ").append(hi.description).append("\n");

        new AlertDialog.Builder(this)
                .setTitle(hi.name)
                .setMessage(sb.toString())
                .setPositiveButton("Edit", (d, w) -> launchEdit(hi))
                .setNegativeButton("Close", null)
                .show();
    }
    // ==================================================

    // ===== THAY ĐỔI 4: TRUYỀN DỮ LIỆU KHI EDIT =====
    // send all fields as primitive extras (no Parcelable) to avoid unresolved-symbol errors
    private void launchEdit(HikeItem hi) {
        Intent i = new Intent(this, AddHikeActivity.class);
        i.putExtra("edit_id", hi.id);

        if (hi.name != null) i.putExtra("name", hi.name);
        if (hi.location != null) i.putExtra("location", hi.location);
        if (hi.date != null) i.putExtra("date", hi.date);
        i.putExtra("parking", hi.parking);
        i.putExtra("lengthKm", hi.length);
        if (hi.difficulty != null) i.putExtra("difficulty", hi.difficulty);
        if (hi.description != null) i.putExtra("description", hi.description);
        i.putExtra("elevationGain", hi.elevation);
        if (hi.trailType != null) i.putExtra("trailType", hi.trailType);
        i.putExtra("groupSize", hi.groupSize);

        // --- DÒNG ĐÃ THÊM ---
        if (hi.weather != null) i.putExtra("weather", hi.weather);
        // ---------------------

        try {
            startActivity(i);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Edit target not found: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete(HikeItem hi) {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete \"" + hi.name + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    boolean ok = db.deleteHike(hi.id); // ensure your DatabaseHelper implements this
                    if (ok) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}