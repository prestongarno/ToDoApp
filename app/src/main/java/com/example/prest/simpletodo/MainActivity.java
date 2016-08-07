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
    private ListView lvItems;

    private Toolbar toolbar;
    private Menu mainActivityMenu;
    private RelativeLayout mainLayout;
    private TextView noNotesMessage;
    private Handler toFadeToolbar;
    private Runnable runFadeToolbar;
    private SwipeRefreshLayout loader;
    private Handler mainHandler;
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

        //setup the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        setSupportActionBar(toolbar);
        toFadeToolbar = new Handler();

        //instantiate main handler
        mainHandler = new Handler();

        setupListView(savedInstanceState);

        runFadeToolbar = new Runnable() {
            @Override
            public void run() {
                Toolbar t = (Toolbar) findViewById(R.id.toolbar);
                Util.getInstance().fadeToHalfTransparent(t);
                t.setAlpha(MainActivity.OPAQUE);
            }
        };

        //start the timer at the start of the application
        toFadeToolbar.postDelayed(runFadeToolbar, 2000);

        //attach listeners
        toolbar.setOnClickListener(new View.OnClickListener() {
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
            // Respond to the toolbar's NavigationIcon as up/home button
            case R.id.sync_notes:
                this.writeItems();
                return true;
            case R.id.load_notes:
                this.readItems();
                return true;
            case R.id.delete_note:
                itemsAdapter.deleteSelected();
                itemsAdapter.notifyDataSetChanged();
                item.setVisible(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){

        super.onPause();
    }

    public void onAddItem(View v) {

        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        String itemText = null;
        try {
            itemText = etNewItem.getText().toString();
            itemText = itemText.trim();
            if(!itemText.equals("")) {
                //drop focus off of the keyboard and close it
                etNewItem.clearFocus();
                Util.getInstance().hideKeyboard(this);
                //add to the adapter
                items.add(new note(itemText, "tap here to add a description..."));
                etNewItem.setText("");
                //update the adapter
                itemsAdapter.notifyDataSetChanged();
                hideOrShowNoNotesMessage();
            } else {
                Toast.makeText(getApplicationContext(), "Enter a note!", Toast.LENGTH_SHORT).show();
            }


        } catch (NullPointerException n) {
        }
    }

    private void setupListView(Bundle savedInstanceState){
        //re-put the values if the user rotates phone, switches app or whatever
        if (savedInstanceState != null) {
            try{
                items = savedInstanceState.getParcelableArrayList("notes");
                lvItems = (ListView) findViewById(R.id.lvItems);
                itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, items, this);
                lvItems.setAdapter(itemsAdapter);
                itemsAdapter.setListViewReferenceHandle(lvItems);
                itemsAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Util.getInstance().printStackTrace(e, TAG);
            }
        } else if (items == null){
            //adding items to the listview --> move to it's own method to setup
            items = new ArrayList<>();
            lvItems = (ListView) findViewById(R.id.lvItems);
            itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, items, this);
            lvItems.setAdapter(itemsAdapter);
            itemsAdapter.setListViewReferenceHandle(lvItems);
        } else {
            //adding items to the listview --> move to it's own method to setup
            items = new ArrayList<>();
            lvItems = (ListView) findViewById(R.id.lvItems);
            itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, items, this);
            lvItems.setAdapter(itemsAdapter);
            itemsAdapter.setListViewReferenceHandle(lvItems);
            readItems();
        }

        setupListViewListener();
        hideOrShowNoNotesMessage();
    }

    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                        itemsAdapter.setSelected(pos);
                        itemsAdapter.displayAllCheckBoxes();
                        MenuItem deleteButton = getMainActivityMenu().findItem(R.id.delete_note);

                        if(deleteButton.isVisible()) {
                            deleteButton.setVisible(false);
                        } else if (!deleteButton.isVisible()) {
                            deleteButton.setVisible(true);
                        }

                        return true;
                    }
                });
        loader = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
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
            lvItems.setAdapter(itemsAdapter);
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

            if(!lvItems.isShown()){;
                noNotesMessage.setAnimation(fadeOut);
                noNotesMessage.startAnimation(fadeOut);
                lvItems.setAnimation(fadeIn);
                lvItems.startAnimation(fadeOut);
                noNotesMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noNotesMessage.setVisibility(View.INVISIBLE);
                        lvItems.setVisibility(View.VISIBLE);
                    }
                }, 800);
            } else {
                lvItems.setVisibility(View.VISIBLE);
                noNotesMessage.setVisibility(View.INVISIBLE);
            }

        } else {
            if(lvItems.isShown()){
                noNotesMessage.setAnimation(fadeOut);
                noNotesMessage.startAnimation(fadeOut);
                lvItems.setAnimation(fadeIn);
                lvItems.startAnimation(fadeOut);
                noNotesMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noNotesMessage.setVisibility(View.VISIBLE);
                        lvItems.setVisibility(View.INVISIBLE);
                    }
                }, 800);
            } else {
                lvItems.setVisibility(View.INVISIBLE);
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

    //private extension of arrayadapter to easily update the listview
    private class CustomListViewAdapter extends ArrayAdapter {
        private final ArrayList<note> notes;
        private final Activity activity;
        private ListView theListView;
        private NotesHolder holder;
        private Context context;

        public CustomListViewAdapter(Context context, int resource, ArrayList<note> notes, Activity activity) {
            super(context, resource, notes);
            this.activity = activity;
            this.notes = notes;
            this.context = context;
        }

        public void setSelected(int index){
            CheckBox c = (CheckBox) theListView.getChildAt(index).findViewById(R.id.selector);
            if (c.isChecked()){
                c.setChecked(false);
            } else {
                c.setChecked(true);
            }
        }

        public void displayAllCheckBoxes(){


            for(int i = 0; i < notes.size(); i++) {
                CheckBox c = (CheckBox) theListView.getChildAt(i).findViewById(R.id.selector);
                if (!boxesDisplayed()){
                    c.setVisibility(View.VISIBLE);
                } else if (boxesDisplayed()){
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

        public void setListViewReferenceHandle(ListView lvItems) {
            this.theListView = lvItems;
        }
    }

    private static class NotesHolder{
        TextView Title;
        TextView Description;
        CheckBox checkBox;
        TextView Date;
    }

}
