package com.example.prest.simpletodo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //instance variables
    private ArrayList<note> items;
    private ArrayAdapter<note> itemsAdapter;
    private ListView lvItems;

    private Toolbar toolbar;
    private RelativeLayout mainLayout;
    private TextView noNotesMessage;
    private Handler toFadeToolbar;
    private Runnable runFadeToolbar;
    private final static float OPAQUE = 0.3f;
    private SearchView searchViewButton;

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

        setupListView();

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
                    Toast.makeText(getApplicationContext(), "null callbacks removed", Toast.LENGTH_SHORT).show();
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
        return true;
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
                itemsAdapter.add(new note(itemText));
                etNewItem.setText("");
                writeItems();
                hideOrShowNoNotesMessage();
            } else {
                Toast.makeText(getApplicationContext(), "Enter a note!", Toast.LENGTH_SHORT).show();
            }


        } catch (NullPointerException n) {
            Toast.makeText(getApplicationContext(), n.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListView(){
        //adding items to the listview --> move to it's own method to setup
        items = new ArrayList<>();
        lvItems = (ListView) findViewById(R.id.lvItems);
        readItems();

        //currently uses (context, int resource, List<>)
        //need to use ArrayAdapter(context, int resource, int textViewResourceId, List<T> objects)
        itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, items, this);
        lvItems.setAdapter(itemsAdapter);

        //attach longClicklistener
        setupListViewListener();
        hideOrShowNoNotesMessage();
    }

    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                        try {
                            // Remove the item within array at position
                            items.remove(pos);
                            //call our custom animation that took 2 hours to write
                            Util.getInstance().fadeListViewChildViewOut(lvItems, itemsAdapter, pos, 800);
                            //don't have to notify the adapter here because that's done from our singleton
                            //class so the animation can play
                            writeItems();
                            // Return true consumes the long click event (marks it handled)
                            hideOrShowNoNotesMessage();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        return true;
                    }
                });
    }

    //methods for reading and writing apps
    private void readItems() {
/*        File filesDir = getFilesDir();
        File toDoFile = new File(filesDir, "todo.txt");

        try {
            List<String> itemsFromFIle = FileUtils.readLines(toDoFile);
            items.clear();
            items.addAll(itemsFromFIle);

            Toast.makeText(getApplicationContext(), items.size() + " items read from file:)", Toast.LENGTH_LONG);
        } catch (Exception e) {
            //itemsAdapter.add("Error reading the old notes from file\nDon't know what's wrong with this exactly...");
            Toast.makeText(getApplicationContext(), "Error loading notes!", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }*/
    }

    private void writeItems(){
/*        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");

        try {
            FileUtils.writeLines(todoFile, items);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error saving data!", Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(getApplicationContext(), items.size() + " Items written to file!", Toast.LENGTH_LONG).show();
        writeItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        readItems();
    }

    private void setupToolbar(Toolbar t) {
        t.setLogo(R.mipmap.ic_launcher);
        //t.addView(findViewById(R.id.title_text));
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

    //private extension of arrayadapter to easily update the listview
    private class CustomListViewAdapter extends ArrayAdapter {

        //private final Activity activity;
        private final ArrayList<note> notes;
        private final Activity activity;

        public CustomListViewAdapter(Context context, int resource, List notes, Activity activity) {
            super(context, resource, notes);
            this.activity = activity;

            this.notes = (ArrayList<note>) notes;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View customView = convertView;

            if(customView == null) {
                //get activity layout inflater
                LayoutInflater inflater = activity.getLayoutInflater();
                customView = inflater.inflate(R.layout.list_view_item_layout, null);

                //get index of the new object and set the text
                TextView title = (TextView) customView.findViewById(R.id.textView_note_title);
                TextView description = (TextView) customView.findViewById(R.id.note_description);
                title.setText(notes.get(position).getTitle());
                description.setText(notes.get(position).getDescription());
            }
            //return the view
            return customView;
        }
    }

}
