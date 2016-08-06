package com.example.prest.simpletodo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by prest on 8/1/2016.
 */
public class note implements Parcelable {

    private String title, description;
    private simpleDateString date;

    public note(String title) {
        this.title = title;
        this.description = "";
        this.date = new simpleDateString(Integer.toString(Calendar.DAY_OF_WEEK), Integer.toString(Calendar.MONTH), Integer.toString(Calendar.YEAR));
    }

    public note(String title, String description) {
        this.title = title;
        this.description = description;
        this.date = new simpleDateString(Integer.toString(Calendar.DAY_OF_WEEK), Integer.toString(Calendar.MONTH), Integer.toString(Calendar.YEAR));
    }

    public note(String title, String description, simpleDateString date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    protected note(Parcel in) {
        title = in.readString();
        description = in.readString();
        date = new simpleDateString(in.readString());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date.toString();
    }

    public void setDate(simpleDateString date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(date.toString());
    }

    @Override
    public String toString() {
        return title + "$" + description + "$" + date.toString();
    }

    public static final Creator<note> CREATOR = new Creator<note>() {
        @Override
        public note createFromParcel(Parcel in) {
            return new note(in);
        }

        @Override
        public note[] newArray(int size) {
            return new note[size];
        }
    };


}
