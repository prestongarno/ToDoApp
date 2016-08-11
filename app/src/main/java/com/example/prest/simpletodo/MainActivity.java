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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //instance variables

    private notesManager manager;

    //private ArrayList<note> items;
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
                setDeleteMode(false);
                manager.saveNotes();
                return true;
            case R.id.main_menu_load_notes:
                setDeleteMode(false);
                manager.loadNotes();
                itemsAdapter.notifyDataSetChanged();
                this.setDeleteMode(false);
                return true;
            case R.id.main_menu_delete_note:
                wakeUpToolBar(act_main_toolbar);
                manager.deleteSelected();
                itemsAdapter.notifyDataSetChanged();
                item.setVisible(false);
                setDeleteMode(false);
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
                setDeleteMode(false);
                manager.add(new note(itemText, "tap here to add a description"));
                act_main_et_new_item.setText("");
                hideOrShowNoNotesMessage();
            } else {
                Toast.makeText(getApplicationContext(), "Enter a note!", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException n) {
            Util.getInstance().printStackTrace(n, TAG);
        }
    }

    private void setDeleteMode(boolean state) {
        if (state == true) {
            deleteButton.setVisible(true);
            wakeUpToolBar(act_main_toolbar);
            itemsAdapter.displayAllCheckBoxes(true);
        } else if (state == false) {
            deleteButton.setVisible(false);
            itemsAdapter.uncheckAll();
            itemsAdapter.displayAllCheckBoxes(false);
        }
    }

    private boolean isListInDeleteMode() {
        if (deleteButton.isVisible()) {
            return true;
        } else {
            return false;
        }
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
        itemsAdapter.setListViewReferenceHandle(act_main_lv_Items);
        addInitialTestNotes();
        hideOrShowNoNotesMessage();
    }

    private void setupListViewListener() {

        //LongClick Listener
        act_main_lv_Items.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                        boolean state = isListInDeleteMode();
                        setDeleteMode(!state);
                        itemsAdapter.setCheckBoxSelected(pos, true);
                        return true;
                    }
                });

        loader = (SwipeRefreshLayout) findViewById(R.id.act_main_swipe_refresh);
        if (loader != null) {
            //implement action interface listener
            loader.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    setDeleteMode(false);
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
                    Log.d(TAG, "onItemClick: isListInDeleteMode == true");
                    //itemsAdapter.setCheckBoxSelected(position);
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

    public void onSelectNote(View v) {
        CheckBox check = (CheckBox) v;
        View parent = (View) check.getParent();
        if (check.isChecked()) {
            parent.setSelected(true);
        } else if (!check.isChecked()) {
            parent.setSelected(false);
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
        private final ArrayList<note> notes;
        private final Activity activity;
        private ListView theListView;
        private NotesHolder holder;
        private Context context;
        private Toolbar t;
        private final int CHECKED = 1;
        private final int UNCHECKED = 0;
        private ArrayList<Integer> selectedRows;

        public CustomListViewAdapter(Context context, int resource, ArrayList<note> notes, Activity activity) {
            super(context, resource, notes);
            this.activity = activity;
            this.notes = notes;
            this.context = context;
            selectedRows = new ArrayList<>();
        }

        public void setCheckBoxSelected(int pos, boolean b) {
            CheckBox c = (CheckBox) theListView.getChildAt(pos).findViewById(R.id.selector);
            if (b) {
                c.setChecked(true);
            } else if (b == false){
                c.setChecked(false);
            }
        }

        public void uncheckAll() {
            if (notes.size() > 0) {
                int i = 0;
                for (note n : notes) {
                    CheckBox c = (CheckBox) theListView.getChildAt(i).findViewById(R.id.selector);
                    c.setChecked(false);
                    Log.d(TAG, "uncheckAll:");
                    Log.d(TAG, "Line #" + i + "\t-->\tisChecked() == " + c.isChecked());
                    i++;
                }
            }
        }

        public void displayAllCheckBoxes(boolean state) {
            if (notes.size() > 0) {
                int i = 0;
                for (note n : notes) {
                    CheckBox c = (CheckBox) theListView.getChildAt(i).findViewById(R.id.selector);
                    if (state == true) {
                        c.setVisibility(View.VISIBLE);
                    } else {
                        c.setVisibility(View.GONE);
                    }
                    i++;
                }
            }
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "getView: called!");
            holder = null;
            if (convertView == null) {
                convertView = new customListViewItem(theListView.getContext());
/*                holder = new NotesHolder();
                holder.Title = (TextView) convertView.findViewById(R.id.textView_note_title);
                holder.Description = (TextView) convertView.findViewById(R.id.note_description);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.selector);
                holder.Date = (TextView) convertView.findViewById(R.id.date);*/
                manager.registerView((NotesListObserver) convertView, position);
                //get index of the new object and set the text
            } else {
            holder = (NotesHolder) convertView.getTag();
/*                    holder.Title.setText(notes.get(position).getTitle());
                holder.Description.setText(notes.get(position).getDescription());
                holder.Date.setText(notes.get(position).getDate().toString());*/

                if (manager.noteSelected(position)) {
                    manager.selectNote(position, true);
                } else if(!manager.noteSelected(position)) {
                    manager.selectNote(position, false);
                }
            }



            return convertView;
        }

/*        private void addSelectedItem(int position) {
            if (!selectedRows.contains(position)){
                //Log.d(TAG, "addSelectedItem: Pos=" + position);
                selectedRows.add(Integer.valueOf(position));
            }
        }

        private void removeSelectedItem(int position) {
            if(selectedRows.contains(position)){
                //Log.d(TAG, "removeSelectedItem: Pos=" + position);
                selectedRows.remove(Integer.valueOf(position));
            }
        }*/

        public void setListViewReferenceHandle(ListView act_main_lv_Items) {
            this.theListView = act_main_lv_Items;
        }

    }

/*    private static class NotesHolder {
        private static final int CHECKED = 1;
        private static final int UNCHECKED = 0;

        TextView Title;
        TextView Description;
        CheckBox checkBox;
        TextView Date;
        int status;
    }*/
}