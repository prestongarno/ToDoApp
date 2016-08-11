package com.example.prest.simpletodo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //instance variables

    private notesManager manager;

    private CustomListViewAdapter itemsAdapter;
    private ListView act_main_lv_Items;
    private Toolbar act_main_toolbar;
    private Menu mainActivityMenu;
    private RelativeLayout mainLayout;
    private TextView noNotesMessage;
    private Handler toFadeToolbar;
    private Runnable runFadeToolbar;
    private SwipeRefreshLayout loader;
    private Handler mainHandler;
    private MenuItem deleteButton;
    public final static String TAG = "MainActivity";
    private final static float OPAQUE = 0.3f;
    private final int CHECKED = 1;
    private final int UNCHECKED = 0;

    public Menu getMainActivityMenu() {
        return mainActivityMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = new notesManager(getApplicationContext(), this);

        //get the main layout, get handles for each of the items we need to access
        mainLayout = (RelativeLayout) this.findViewById(R.id.activity_main);
        noNotesMessage = new TextView(this);
        noNotesMessage.setText("No Notes");
        noNotesMessage.setTextColor(0xFFFFFF);
        setBackgroundToNoNotes();

        //setup the act_main_toolbar
        act_main_toolbar = (Toolbar) findViewById(R.id.act_main_toolbar);
        setupToolbar(act_main_toolbar);

        //instantiate main handler
        mainHandler = new Handler();

        setupListView(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.mainActivityMenu = menu;
        deleteButton = getMainActivityMenu().findItem(R.id.main_menu_delete_note);
        deleteButton.setVisible(false);
        return true;
    }

    private void wakeUpToolBar(View v) {
        try {
            toFadeToolbar.removeCallbacks(runFadeToolbar);
        } catch (NullPointerException n) {
        }

        if (v.getAlpha() == 1) {
            toFadeToolbar.removeCallbacks(runFadeToolbar);
            toFadeToolbar.postDelayed(runFadeToolbar, 5000);
        } else {
            v.setAlpha(1f);
            toFadeToolbar.postDelayed(runFadeToolbar, 5000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the act_main_toolbar's NavigationIcon as up/home button
            case R.id.main_menu_sync_notes:
                manager.selectAllNotes(false);
                itemsAdapter.displayCheckBoxes(false);
                manager.saveNotes();
                return true;
            case R.id.main_menu_load_notes:
                manager.selectAllNotes(false);
                itemsAdapter.displayCheckBoxes(false);
                manager.loadNotes();
                itemsAdapter.notifyDataSetChanged();
                manager.selectAllNotes(false);
                itemsAdapter.displayCheckBoxes(false);
                return true;
            case R.id.main_menu_delete_note:
                wakeUpToolBar(act_main_toolbar);
                manager.deleteSelected();
                itemsAdapter.notifyDataSetChanged();
                item.setVisible(false);
                manager.selectAllNotes(false);
                itemsAdapter.displayCheckBoxes(false);
                hideOrShowNoNotesMessage();
                return true;
            case R.id.main_menu_settings:
                return true;
            case R.id.main_menu_debug_action:
                manager.setNoteTitle(0, "This item has been changed");
                manager.setNoteDescription(0, "the listeners like me have been notified");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    public void onAddItem(View v) {

        EditText act_main_et_new_item = (EditText) findViewById(R.id.act_main_et_new_item);
        String itemText;

        try {
            itemText = act_main_et_new_item.getText().toString();
            itemText = itemText.trim();
            if (!itemText.equals("") && !itemText.equals("&-#-#-#-&")) {
                Util.getInstance().hideKeyboard(this);
                manager.selectAllNotes(false);
                itemsAdapter.displayCheckBoxes(false);
                manager.add(new note(itemText, "tap here to add a description"));
                itemsAdapter.notifyDataSetChanged();
                act_main_et_new_item.setText("");
                hideOrShowNoNotesMessage();
            } else {
                Toast.makeText(getApplicationContext(), "Enter a note!", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException n) {
            Util.getInstance().printStackTrace(n, TAG);
        }
    }

    private boolean isListInDeleteMode() {
        if (deleteButton.isVisible()) {
            return true;
        } else {
            return false;
        }
    }

    private void setDeleteMode(int selectedItem, boolean state) {
        Log.d(TAG, "setDeleteMode:\nValue of 'selectedItem' parameter:\t\t" + selectedItem);
        manager.selectAllNotes(!state);
        itemsAdapter.displayCheckBoxes(state);
        manager.selectNote(selectedItem, state);
        deleteButton.setVisible(state);
    }

    private void setupListView(Bundle savedInstanceState) {
        //re-put the values if the user rotates phone, switches app or whatever
        if (savedInstanceState != null) {
            try {
                ArrayList<note> t = savedInstanceState.getParcelableArrayList("notes");
                manager.setNotesList(t);
                act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
                itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, manager.getNotesList(), this);
                act_main_lv_Items.setAdapter(itemsAdapter);
                itemsAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Util.getInstance().printStackTrace(e, TAG);
            }
        } else {
            manager.loadNotes();
            act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
            itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, manager.getNotesList(), this);
            act_main_lv_Items.setAdapter(itemsAdapter);
        }

        setupListViewListener();
        addInitialTestNotes();
        hideOrShowNoNotesMessage();
    }

    private void setupListViewListener() {

        //LongClick Listener
        act_main_lv_Items.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                        setDeleteMode(pos, !isListInDeleteMode());
                        return true;
                    }
                });

        loader = (SwipeRefreshLayout) findViewById(R.id.act_main_swipe_refresh);
        if (loader != null) {
            //implement action interface listener
            loader.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    manager.selectAllNotes(false);
                    itemsAdapter.displayCheckBoxes(false);
                    // TODO: 8/10/2016 change loadnotes to a boolean return so we know if the data changed
                    manager.loadNotes();
                    itemsAdapter.notifyDataSetChanged();
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loader.setRefreshing(false);
                        }
                    }, 800);
                }
            });
        }

        //onClickListener
        act_main_lv_Items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isListInDeleteMode() == true) {
                    manager.selectNote(position, !manager.getNote(position).isSelected());
                } else {
                    //inflate the selected item's edit layout/start activity
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        manager.saveNotes();
        savedState.putParcelableArrayList("notes", manager.getNotesList());
        super.onSaveInstanceState(savedState);
    }

    private void setupToolbar(Toolbar t) {
        toFadeToolbar = new Handler();
        t.setLogo(R.mipmap.ic_launcher_main);

        runFadeToolbar = new Runnable() {
            @Override
            public void run() {
                Toolbar t = (Toolbar) findViewById(R.id.act_main_toolbar);
                Util.getInstance().fadeToHalfTransparent(t);
                t.setAlpha(MainActivity.OPAQUE);
            }
        };

        //start the timer at the start of the application
        toFadeToolbar.postDelayed(runFadeToolbar, 2000);

        //attach listeners
        act_main_toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wakeUpToolBar(v);
            }
        });

        setSupportActionBar(act_main_toolbar);
    }

    private void setBackgroundToNoNotes() {
        RelativeLayout.LayoutParams myParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        myParams.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        mainLayout.addView(noNotesMessage, myParams);
    }

    private void hideOrShowNoNotesMessage() {
        AlphaAnimation fadeIn = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.fade_in);
        AlphaAnimation fadeOut = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.fade_out);


        //if items arraylist has items in it still
        /**********************************************/
        if (manager.getNotesList().isEmpty() == false) {

            if (act_main_lv_Items.isShown() == false) {
                noNotesMessage.startAnimation(fadeOut);
                act_main_lv_Items.startAnimation(fadeIn);
                noNotesMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noNotesMessage.setVisibility(View.INVISIBLE);
                        act_main_lv_Items.setVisibility(View.VISIBLE);
                    }
                }, 800);
            }
            /**********************************************/


            //else if items ArrayList is actually empty
            /**********************************************/
        } else if (manager.getNotesList().isEmpty() == true) {

            if (act_main_lv_Items.isShown() == true) {
                noNotesMessage.startAnimation(fadeIn);
                act_main_lv_Items.startAnimation(fadeOut);
                noNotesMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noNotesMessage.setVisibility(View.VISIBLE);
                        act_main_lv_Items.setVisibility(View.INVISIBLE);
                    }
                }, 800);
            }
        }
    }

    private void addInitialTestNotes() {
        //add a few notes to the application until read() and write() are working
        if (manager.getNotesList().isEmpty()) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    manager.add(new note("This is the first test note!", "the description for the first note", new simpleDateString("04/24/1995")));
                    manager.add(new note("ToDo: Use Parse SDK for back end", "use local storage and cache also though!", new simpleDateString("10/16/2016")));
                    manager.add(new note("ToDo: write menu item functionality", "separate methods for attaching the actionListeners though"));
                    manager.add(new note("ToDo: learn how to efficiently use activities and fragments", "how do callback etc work between main and sub activities?"));
                    manager.saveNotes();
                    itemsAdapter.notifyDataSetChanged();
                }
            }, 2000);
        }
    }

    //private extension of arrayadapter to easily update the listview
    private class CustomListViewAdapter extends ArrayAdapter {
        private final Activity activity;
        private ListView theListView;
        private Context context;
        private Toolbar t;
        private boolean checkboxVisible;
        private NoteHolder holder;

        public CustomListViewAdapter(Context context, int resource, ArrayList<note> notes, Activity activity) {
            super(context, resource, manager.getNotesList());
            this.activity = activity;
            this.context = context;
            checkboxVisible = false;
        }

        public void displayCheckBoxes(boolean state){
            checkboxVisible = state;
            this.notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged(){
            super.notifyDataSetChanged();
            manager.notifyAllObservers();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            holder = null;
            if (convertView == null) {
                holder = new NoteHolder();
                convertView = new customListViewItem(context);
                manager.registerView((NotesListObserver) convertView, position);
            } else if (convertView instanceof NotesListObserver) {
                ((NotesListObserver) convertView).update(manager.getNote(position));
            }

            if (convertView instanceof customListViewItem) {
                if (checkboxVisible) {
                    ((customListViewItem) convertView).getNoteCheckBox().setVisibility(View.VISIBLE);
                } else {
                    ((customListViewItem) convertView).getNoteCheckBox().setVisibility(View.GONE);
                }
            }

            convertView.setTag(holder);
            return convertView;
        }
    }

    public static class NoteHolder {
        int index;
    }
}