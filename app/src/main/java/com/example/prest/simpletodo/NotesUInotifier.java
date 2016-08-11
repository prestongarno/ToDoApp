package com.example.prest.simpletodo;

/**
 * Created by Preston Garno on 8/8/2016.
 */
public interface NotesUInotifier {
    public void registerListener(NotesListObserver Observer);
    public void removeListener(NotesListObserver Observer, int position);
    public void notifyObservers();
}
