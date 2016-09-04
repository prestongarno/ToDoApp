package com.example.prest.simpletodo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Calendar;

/**
 * Created by prest on 8/1/2016.
 */
@ParseClassName("note")
public class note extends ParseObject implements Parcelable, NotesUInotifier {

    private String title, description;
    private simpleDateString date;
    private boolean isSelected;
    private NotesListObserver viewObserver;

    public note(){}

    public note(String title) {
        isSelected = false;
        Calendar c = Calendar.getInstance();
        this.title = title;
        this.description = "";
        this.date = new simpleDateString(Integer.toString(c.get(Calendar.DAY_OF_WEEK)), Integer.toString(c.get(Calendar.MONTH)), Integer.toString(c.get(Calendar.YEAR)));
        put("title", title);
        put("description", "no description");
        put("dateCreated", this.date.toString());
    }

    public note(String title, String description) {
        isSelected = false;
        Calendar c = Calendar.getInstance();
        this.title = title;
        this.description = description;
        this.date = new simpleDateString(Integer.toString(c.get(Calendar.DATE)), Integer.toString(c.get(Calendar.MONTH)), Integer.toString(c.get(Calendar.YEAR)));
        put("title", title);
        put("description", description);
        put("dateCreated", this.date.toString());
    }

    public note(String title, String description, simpleDateString date) {
        isSelected = false;
        this.title = title;
        this.description = description;
        this.date = date;

        put("title", this.title);
        put("description", this.description);
        put("dateCreated", this.date.toString());
    }

    protected note(Parcel in) {
        title = in.readString();
        description = in.readString();
        date = new simpleDateString(in.readString());
    }

    public note(String title, String description, simpleDateString date, NotesListObserver o) {
        viewObserver = o;
        isSelected = false;
        this.title = title;
        this.description = description;
        this.date = date;

        put("title", title);
        put("description", description);
        put("dateCreated", date.toString());
    }

    public void setAll(String title, String description, simpleDateString dateCreated) {
        setTitle(title);
        setDescription(description);
        setDate(dateCreated);
    }

    public String getTitle() {
        return (String) get("title");
    }

    public void setTitle(String title) {
        this.title = title;
        put("title", title);
        notifyObservers();
    }

    public String getDescription() {
        return (String) get("description");
    }

    public void setDescription(String description) {
        this.description = description;
        put("description", description);
        notifyObservers();
    }

    public simpleDateString getDate() {
        return new simpleDateString((String) get("dateCreated"));
    }

    public void setDate(simpleDateString date) {
        this.date = date;
        put("dateCreated", date.toString());
        notifyObservers();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getTitle());
        dest.writeString(getDescription());
        dest.writeString(getDate().toString());
    }

    @Override
    public String toString() {
        return title + "&-#-#-#-&" + description + "&-#-#-#-&" + date.toString();
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

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        notifyObservers();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public NotesListObserver getListeners(){
        return viewObserver;
    }

    @Override
    public void registerListener(NotesListObserver Observer) {
        viewObserver = Observer;
    }

    @Override
    public void removeListener(NotesListObserver Observer, int position) {
        viewObserver = null;
    }

    @Override
    public void notifyObservers() {
        if (viewObserver != null) {
            viewObserver.update(this);
        } else {
            Log.d("NoteClass", "notifyObservers: Null observer for this note: " + this.getTitle());
        }
    }

    public void updateParse(){
        put("title", title);
        put("description", description);
        put("dateCreated", getDate().toString());
        this.saveInBackground();
    }
}
