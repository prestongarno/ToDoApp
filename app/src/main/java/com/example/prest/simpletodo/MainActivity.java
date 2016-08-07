package com.example.prest.simpletodo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //instance variables
    private ArrayList<note> items;
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

    public Menu getMainActivityMenu() {
        return mainActivityMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //get the main layout, get handles for each of the items we need to access
        mainLayout = (RelativeLayout) this.findViewById(R.id.activity_main);
        noNotesMessage = new TextView(this);
        noNotesMessage.setText("No Notes");
        noNotesMessage.setTextColor(0xFFFFFF);
        setBackgroundToNoNotes();

        //setup the act_main_toolbar
        act_main_toolbar = (Toolbar) findViewById(R.id.act_main_toolbar);
        setupToolbar(act_main_toolbar);
        setSupportActionBar(act_main_toolbar);
        toFadeToolbar = new Handler();

        //instantiate main handler
        mainHandler = new Handler();

        setupListView(savedInstanceState);

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
                try{
                    toFadeToolbar.removeCallbacks(runFadeToolbar);
                } catch (NullPointerException n) {
                }

                if (v.getAlpha() == 1){
                    toFadeToolbar.removeCallbacks(runFadeToolbar);
                    toFadeToolbar.postDelayed(runFadeToolbar, 5000);
                }else  {
                    v.setAlpha(1f);
                    toFadeToolbar.postDelayed(runFadeToolbar, 5000);
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.mainActivityMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the act_main_toolbar's NavigationIcon as up/home button
            case R.id.main_menu_sync_notes:
                this.writeItems();
                return true;
            case R.id.main_menu_load_notes:
                this.readItems();
                return true;
            case R.id.main_menu_delete_note:
                itemsAdapter.deleteSelected();
                itemsAdapter.notifyDataSetChanged();
                item.setVisible(false);
                hideOrShowNoNotesMessage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){

        super.onPause();
    }

    public void onAddItem(View v) {

        EditText act_main_et_new_item = (EditText) findViewById(R.id.act_main_et_new_item);
        String itemText = null;
        try {
            itemText = act_main_et_new_item.getText().toString();
            itemText = itemText.trim();
            if(!itemText.equals("") && !itemText.equals("&-#-#-#-&")) {
                //drop focus off of the keyboard and close it
                act_main_et_new_item.clearFocus();
                Util.getInstance().hideKeyboard(this);
                //add to the adapter
                items.add(new note(itemText, "tap here to add a description..."));
                act_main_et_new_item.setText("");
                //update the adapter
                itemsAdapter.notifyDataSetChanged();
                hideOrShowNoNotesMessage();
            } else {
                Toast.makeText(getApplicationContext(), "Enter a note!", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException n) {

        }
    }

    private void setDeleteMode(boolean state){
        if(state) {
            deleteButton.setVisible(true);
            itemsAdapter.displayAllCheckBoxes(true);
        } else if (!state) {
            deleteButton.setVisible(false);
            itemsAdapter.displayAllCheckBoxes(false);
        }
    }

    private boolean isListInDeleteMode(){
        if(deleteButton.isVisible()) {
            return true;
        } else {
            return false;
        }
    }

    private void setupListView(Bundle savedInstanceState){
        //re-put the values if the user rotates phone, switches app or whatever
        if (savedInstanceState != null) {
            try{
                items = savedInstanceState.getParcelableArrayList("notes");
                act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
                itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, items, this);
                act_main_lv_Items.setAdapter(itemsAdapter);
                itemsAdapter.setListViewReferenceHandle(act_main_lv_Items);
                itemsAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Util.getInstance().printStackTrace(e, TAG);
            }
        } else if (items == null){
            //adding items to the listview --> move to it's own method to setup
            items = new ArrayList<>();
            act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
            itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, items, this);
            act_main_lv_Items.setAdapter(itemsAdapter);
            itemsAdapter.setListViewReferenceHandle(act_main_lv_Items);
        } else {
            //adding items to the listview --> move to it's own method to setup
            items = new ArrayList<>();
            act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
            itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, items, this);
            act_main_lv_Items.setAdapter(itemsAdapter);
            itemsAdapter.setListViewReferenceHandle(act_main_lv_Items);
            readItems();
        }

        this.addInitialTestNotes();
        deleteButton = getMainActivityMenu().findItem(R.id.main_menu_delete_note);
        setupListViewListener(deleteButton);
        hideOrShowNoNotesMessage();
    }

    private void setupListViewListener(final MenuItem deleteButton) {
        act_main_lv_Items.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                        itemsAdapter.setCheckBoxSelected(pos);
                        itemsAdapter.displayAllCheckBoxes(itemsAdapter.isInDeleteMode());

                        if(deleteButton.isVisible()) {
                            deleteButton.setVisible(false);
                        } else if (!deleteButton.isVisible()) {
                            deleteButton.setVisible(true);
                        }

                        return true;
                    }
                });
        loader = (SwipeRefreshLayout) findViewById(R.id.act_main_swipe_refresh);
        if (loader != null) {
            //implement action interface listener
            loader.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    int OldNoteCount = items.size();
                    readItems();
                    Toast.makeText(getApplicationContext(), items.size() - OldNoteCount + " new notes!", Toast.LENGTH_LONG).show();
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loader.setRefreshing(false);
                        }
                    }, 500);
                }
            });
        }

        //onClickListener
        act_main_lv_Items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemsAdapter.isInDeleteMode()) {
                    itemsAdapter.setCheckBoxSelected(act_main_lv_Items.getSelectedItemPosition());
                } else {
                    //inflate the selected item's edit layout/start activity
                }
            }
        });
    }

    private void readItems() {
        File filesDir = getFilesDir();
        File toDoFile = new File(filesDir, "todo.txt");
        ArrayList<note> tempArrayList = items;

        try {

            items.clear();

            List<String> itemsFromFIle = FileUtils.readLines(toDoFile);
            for(String s : itemsFromFIle) {
            }
            String[] read;
            for(int i = 0; i < itemsFromFIle.size(); i++) {
                read = itemsFromFIle.get(i).split("&-#-#-#-&");
                items.add(new note(read[0], read[1], new simpleDateString(read[2])));
            }
            itemsAdapter.notifyDataSetChanged();
            act_main_lv_Items.setAdapter(itemsAdapter);
        } catch (Exception e) {
            items = tempArrayList;
            itemsAdapter.notifyDataSetChanged();
        }
    }

    private void writeItems(){
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");

        try {
            FileUtils.writeLines(todoFile, items);
            for (note n : items) {
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error writing notes!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        writeItems();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState){
        writeItems();
        savedState.putParcelableArrayList("notes", items);
        super.onSaveInstanceState(savedState);
    }

    private void setupToolbar(Toolbar t) {
        t.setLogo(R.mipmap.ic_launcher);
    }

    private void setBackgroundToNoNotes(){
        RelativeLayout.LayoutParams myParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        myParams.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        mainLayout.addView(noNotesMessage, myParams);
    }

    private void hideOrShowNoNotesMessage(){
        //Spent far too long on this method and still on it a day later:(
        //initialize new animations for this method
        AlphaAnimation fadeIn = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.fade_in);
        AlphaAnimation fadeOut = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.fade_out);

        if(!items.isEmpty()) {

            if(!act_main_lv_Items.isShown()){;
                noNotesMessage.setAnimation(fadeOut);
                noNotesMessage.startAnimation(fadeOut);
                act_main_lv_Items.setAnimation(fadeIn);
                act_main_lv_Items.startAnimation(fadeOut);
                noNotesMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noNotesMessage.setVisibility(View.INVISIBLE);
                        act_main_lv_Items.setVisibility(View.VISIBLE);
                    }
                }, 800);
            } else {
                act_main_lv_Items.setVisibility(View.VISIBLE);
                noNotesMessage.setVisibility(View.INVISIBLE);
            }

        } else {
            if(act_main_lv_Items.isShown()){
                noNotesMessage.setAnimation(fadeOut);
                noNotesMessage.startAnimation(fadeOut);
                act_main_lv_Items.setAnimation(fadeIn);
                act_main_lv_Items.startAnimation(fadeOut);
                noNotesMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noNotesMessage.setVisibility(View.VISIBLE);
                        act_main_lv_Items.setVisibility(View.INVISIBLE);
                    }
                }, 800);
            } else {
                act_main_lv_Items.setVisibility(View.INVISIBLE);
                noNotesMessage.setVisibility(View.VISIBLE);
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

    private void addInitialTestNotes(){
        //add a few notes to the application until read() and write() are working
        if(items.isEmpty()) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    items.add(new note("This is the first test note!", "the description for the first note", new simpleDateString("04/24/1995")));
                    itemsAdapter.notifyDataSetChanged();
                }
            }, 2000);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    items.add(new note("ToDo: Use Parse SDK for back end", "use local storage and cache also though!", new simpleDateString("10/16/2016")));
                    itemsAdapter.notifyDataSetChanged();
                }
            }, 75);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    items.add(new note("ToDo: write menu item functionality", "separate methods for attaching the actionListeners though"));
                    itemsAdapter.notifyDataSetChanged();
                }
            }, 75);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    items.add(new note("ToDo: learn how to efficiently use activities and fragments", "how do callback etc work between main and sub activities?"));
                    itemsAdapter.notifyDataSetChanged();
                }
            }, 75);
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

        public CustomListViewAdapter(Context context, int resource, ArrayList<note> notes, Activity activity) {
            super(context, resource, notes);
            this.activity = activity;
            this.notes = notes;
            this.context = context;
        }

        public void setCheckBoxSelected(int index){
            CheckBox c = (CheckBox) theListView.getChildAt(index).findViewById(R.id.selector);
            if (c.isChecked()){
                c.setChecked(false);
            } else {
                c.setChecked(true);
            }
        }

        public void uncheckAll(){
            for(int i = 0; i < notes.size(); i++) {
                CheckBox c = (CheckBox) theListView.getChildAt(i).findViewById(R.id.selector);
                c.setChecked(false);
            }
        }

        public void displayAllCheckBoxes(boolean state){
            for(int i = 0; i < notes.size(); i++) {
                CheckBox c = (CheckBox) theListView.getChildAt(i).findViewById(R.id.selector);
                if (state){
                    c.setVisibility(View.VISIBLE);
                } else if (!state){
                    c.setVisibility(View.GONE);
                }
            }

            this.notifyDataSetChanged();
        }

        private boolean boxesDisplayed() {
            if (holder.checkBox.getVisibility() == View.VISIBLE) {
                return true;
            } else if (holder.checkBox.getVisibility() == View.GONE) {
                return false;
            } else {
                throw new Resources.NotFoundException("No such checkbox!");
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //theListView = (ListView) parent;
            holder = null;
            if(convertView == null) {
                //get activity layout inflater
                convertView = LayoutInflater.from(context).inflate(R.layout.list_view_item_layout, null);
                holder = new NotesHolder();
                holder.Title = (TextView) convertView.findViewById(R.id.textView_note_title);
                holder.Description = (TextView) convertView.findViewById(R.id.note_description);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.selector);
                holder.Date = (TextView) convertView.findViewById(R.id.date);
                //get index of the new object and set the text
            } else {
                holder = (NotesHolder)convertView.getTag();
            }

            holder.Title.setText(notes.get(position).getTitle());
            holder.Description.setText(notes.get(position).getDescription());
            holder.Date.setText(notes.get(position).getDate());

            //return the view
            convertView.setTag(holder);
            return convertView;
        }

        @Override
        public int getCount(){
            return notes.size();
        }

        public void deleteSelected() {
            for(int i = 0; i < theListView.getCount(); i++) {
                CheckBox c = (CheckBox) theListView.getChildAt(i).findViewById(R.id.selector);
                if (!c.isChecked()){
                    c.setVisibility(View.GONE);
                } else if (c.isChecked()){
                    items.remove(i);
                }
            }
            this.notifyDataSetChanged();
        }

        public void setListViewReferenceHandle(ListView act_main_lv_Items) {
            this.theListView = act_main_lv_Items;
        }

        public boolean isInDeleteMode(){
            return boxesDisplayed();
        }
    }

    private static class NotesHolder{
        TextView Title;
        TextView Description;
        CheckBox checkBox;
        TextView Date;
    }
}
