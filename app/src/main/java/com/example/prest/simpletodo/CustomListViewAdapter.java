package com.example.prest.simpletodo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Preston Garno on 9/4/2016.
 */
public class CustomListViewAdapter extends ArrayAdapter {
    private final Activity activity;
    private Context context;
    public boolean checkboxVisible;
    private notesManager manager;
    //private NoteHolder holder;

    public CustomListViewAdapter(Context context, int resource, ArrayList<note> notes, Activity activity, notesManager manager) {
        super(context, resource, manager.getNotesList());
        this.activity = activity;
        this.context = context;
        checkboxVisible = false;
        this.manager = manager;
    }

    public void displayCheckBoxes(boolean state) {
        checkboxVisible = state;
        this.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (manager != null) {
            manager.notifyAllObservers();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //holder = null;
        if (convertView == null) {
            //holder = new NoteHolder();
            convertView = new customListViewItem(context);
            manager.registerView((NotesListObserver) convertView, position);
        } else if (convertView instanceof NotesListObserver) {
            manager.registerView((NotesListObserver) convertView, position);
            ((NotesListObserver) convertView).update(manager.getNote(position));
        }
        //
        if (checkboxVisible) {
            ((customListViewItem) convertView).getNoteCheckBox().setVisibility(View.VISIBLE);
        } else {
            ((customListViewItem) convertView).getNoteCheckBox().setVisibility(View.GONE);
        }

        return convertView;
    }
    //convertView.setTag(holder);
    @Override
    public int getCount() {
        return manager.getSize();
    }
}
