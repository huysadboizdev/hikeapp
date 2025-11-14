
package com.example.hike_app;

import android.os.Parcel;
import android.os.Parcelable;

public class Hike implements Parcelable {
    public String name;
    public String location;
    public String date;
    public boolean parking;
    public double lengthKm;
    public String difficulty;
    public String description;
    public int elevationGain;
    public String trailType;
    public int groupSize;

    public Hike() {}

    protected Hike(Parcel in) {
        name = in.readString();
        location = in.readString();
        date = in.readString();
        parking = in.readByte() != 0;
        lengthKm = in.readDouble();
        difficulty = in.readString();
        description = in.readString();
        elevationGain = in.readInt();
        trailType = in.readString();
        groupSize = in.readInt();
    }

    public static final Creator<Hike> CREATOR = new Creator<Hike>() {
        @Override public Hike createFromParcel(Parcel in) { return new Hike(in); }
        @Override public Hike[] newArray(int size) { return new Hike[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(date);
        dest.writeByte((byte) (parking ? 1 : 0));
        dest.writeDouble(lengthKm);
        dest.writeString(difficulty);
        dest.writeString(description);
        dest.writeInt(elevationGain);
        dest.writeString(trailType);
        dest.writeInt(groupSize);
    }
}
