package com.example.prest.simpletodo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by prest on 8/1/2016.
 */
public class note implements Parcelable, NotesUInotifier {

    private String title, description;
    private simpleDateString date;
    private boolean isSelected;
    private ArrayList<NotesListObserver> observersList;

    public note(String title) {
        observersList = new ArrayList<>();
        isSelected = false;
        Calendar c = Calendar.getInstance();
        this.title = title;
        this.description = "";
        this.date = new simpleDateString(Integer.toString(c.get(Calendar.DAY_OF_WEEK)), Integer.toString(c.get(Calendar.MONTH)), Integer.toString(c.get(Calendar.YEAR)));
    }

    public note(String title, String description) {
        observersList = new ArrayList<>();
        isSelected = false;
        Calendar c = Calendar.getInstance();
        this.title = title;
        this.description = description;
        this.date = new simpleDateString(Integer.toString(c.get(Calendar.DAY_OF_WEEK)), Integer.toString(c.get(Calendar.MONTH)), Integer.toString(c.get(Calendar.YEAR)));
    }

    public note(String title, String description, simpleDateString date) {
        observersList = new ArrayList<>();
        isSelected = false;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    protected note(Parcel in) {
        observersList = new ArrayList<>();
        title = in.readString();
        description = in.readString();
        date = new simpleDateString(in.readString());
    }

    public note(String title, String description, simpleDateString date, NotesListObserver o) {
        observersList = new ArrayList<>();
        observersList.add(o);
        isSelected = false;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyObservers();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyObservers();
    }

    public simpleDateString getDate() {
        return date;
    }

    public void setDate(simpleDateString date) {
        this.date = date;
        notifyObservers();
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

    public ArrayList<NotesListObserver> getListeners(){
        return observersList;
    }

    @Override
    public void registerListener(NotesListObserver Observer) {
        observersList.add(Observer);
    }

    @Override
    public void removeListener(NotesListObserver Observer, int position) {
        observersList.remove(position);
    }

    @Override
    public void notifyObservers() {
        for(NotesListObserver o : observersList) {
            o.update(this);
        }
    }
}
