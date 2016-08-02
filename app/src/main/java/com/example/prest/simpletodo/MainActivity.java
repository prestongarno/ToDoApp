package com.example.prest.simpletodo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
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

        //get the main layout
        mainLayout = (RelativeLayout) this.findViewById(R.id.activity_main);
        noNotesMessage = new TextView(this);
        noNotesMessage.setText("No Notes");
        noNotesMessage.setTextColor(0xFFFFFF);
        setBackgroundToNoNotes();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        setSupportActionBar(toolbar);


        //adding items to the listview
        items = new ArrayList<>();
        lvItems = (ListView) findViewById(R.id.lvItems);
        readItems();
        itemsAdapter = new ArrayAdapter<>(this, R.layout.text_list, items);
        lvItems.setAdapter(itemsAdapter);
        //attach longClicklistener
        setupListViewListener();
        hideOrShowNoNotesMessage();

        //set up fade out toolbar
        toFadeToolbar = new Handler();

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
                itemsAdapter.add(itemText);
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
        File filesDir = getFilesDir();
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
        }
    }

    private void writeItems(){
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");

        try {
            FileUtils.writeLines(todoFile, items);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error saving data!", Toast.LENGTH_LONG).show();
        }
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

}
