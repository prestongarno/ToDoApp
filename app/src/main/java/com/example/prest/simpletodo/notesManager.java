package com.example.prest.simpletodo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Preston Garno on 8/8/2016.
 * <p/>
 * Manager for the list of notesList
 */
public class notesManager {

    private ArrayList<note> notesList;
    private Activity handlingActivity;
    private Context handlingContext;

    public notesManager(Context c, Activity a) {
        notesList = new ArrayList<>();
        this.handlingActivity = a;
        this.handlingContext = c;
    }

    public void add(note n) {
        notesList.add(n);
    }

    public void add(note n, int pos) {
        notesList.add(pos, n);
    }

    public int getSize() {
        return notesList.size();
    }

    public void delete(Object o) {
        notesList.remove(o);
    }

    /**
     * These delete methods should really not be public if true to MVC design?
     * Register listeners with the delete buttons and get the controller notified when an
     * item needs to be deleted
     *
     * @param pos
     */
    public void delete(int pos) {
        notesList.remove(pos);
    }

    public note getNote(int pos) {
        return notesList.get(pos);
    }

    public void saveNotes() {
        File filesDir = handlingActivity.getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");

        try {
            FileUtils.writeLines(todoFile, notesList);
            for (note n : notesList) {
                n.updateParse();
            }
        } catch (IOException e) {
            Toast.makeText(handlingActivity.getApplicationContext(), "Error writing notes!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateData(final CustomListViewAdapter itemsAdapter, final MainActivity m){
        ParseQuery<note> query = ParseQuery.getQuery(note.class);
        query.findInBackground(new FindCallback<note>() {
            @Override
            public void done(List<note> notes, ParseException error) {
                if(notes != null){
                    notesList.clear();
                    notesList.addAll(notes);
                    itemsAdapter.notifyDataSetChanged();
                    m.hideOrShowNoNotesMessage();
                }
            }
        });
        this.notifyAllObservers();
    }

    /**
     * Returns the Title of the note specified with parameter pos
     *
     * @param pos parameter of the note to get Title from
     * @return Title of the note
     */
    public String getNoteTitle(int pos) {
        return notesList.get(pos).getTitle();
    }

    /**
     * Returns the Description of the note specified with parameter pos
     *
     * @param pos parameter of the note to get Title from
     * @return Title of the note
     */
    public String getNoteDescription(int pos) {
        return notesList.get(pos).getDescription();
    }

    /**
     * Returns the note's date created specified at index
     *
     * @param pos parameter of the note to get Title from
     * @return Date created
     */
    public simpleDateString getNoteDate(int pos) {
        return notesList.get(pos).getDate();
    }

    public void sortByDate() {

    }

    public void sortByTitle(boolean ascending) {

    }

    public void sortByLength(boolean ascending) {

    }

    public void searchByRegex(String regex) {

    }

    public void clearAllNotes() {
        notesList.clear();
    }

    public int getCount() {
        return notesList.size();
    }

    public void setNotesList(ArrayList<note> newList) {
        notesList.clear();
        notesList.addAll(newList);
    }

    public ArrayList<note> getNotesList() {
        return notesList;
    }

    public void registerView(NotesListObserver Observer, int position) {
        notesList.get(position).registerListener(Observer);
        Observer.update(notesList.get(position));
    }

    public void notifyAllObservers() {
        if (notesList.size() > 0){
            for (note n : notesList) {
                if (n.getListeners()!=null){
                    n.getListeners().update(n);
                }
            }
        }
    }

    public void deleteSelected() {
        for(int index = notesList.size()-1; index > -1; index--) {
            NotesListObserver osr = notesList.get(index).getListeners();
            if (osr instanceof customListViewItem) {
                if(((customListViewItem) osr).getNoteCheckBox().isChecked()){
                    try{
                        notesList.get(index).delete();
                    } catch (Exception ex) {
                        Toast.makeText(this.handlingContext, "Error deleting notes!", Toast.LENGTH_SHORT).show();
                    }
                    notesList.remove(index);
                }
            }
        }
    }

    public void selectNote(int index, boolean state){
        if (state) {
            notesList.get(index).setSelected(state);
            notifyAllObservers();
        } else if (!state) {
            notesList.get(index).setSelected(state);
            notifyAllObservers();
        }
    }

    public void selectAllNotes(boolean checked){
        for (note n : notesList) {
            n.setSelected(checked);
        }
    }

    public void setNoteTitle(int index, String Title) {
        notesList.get(index).setTitle(Title);
    }

    public void setNoteDescription(int index, String description) {
        notesList.get(index).setDescription(description);
    }

    public boolean noteSelected(int pos) {
        if (notesList.get(pos).isSelected()) {
            return true;
        } else {
            return false;
        }
    }
}
