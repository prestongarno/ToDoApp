package com.example.prest.simpletodo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private notesManager manager;

    private CustomListViewAdapter itemsAdapter;
    private ListView act_main_lv_Items;
    private Toolbar act_main_toolbar;
    private RelativeLayout mainLayout;
    private TextView noNotesMessage;
    private Handler toFadeToolbar;
    private Runnable runFadeToolbar;
    private SwipeRefreshLayout loader;
    private Handler mainHandler;
    private MenuItem deleteButton;
    public final static String TAG = "MainActivity";
    private final static float OPAQUE = 0.3f;
    private final int REQUEST_CODE_EDIT_NOTE = 35;
    private final int REQUEST_CODE_LOGIN_CREDENTIALS = 99;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);

        String result = getIntent().getStringExtra("LOGIN_RESULT");

        //Initialize Parse
        if (result == null) {
            this.startActivity(intentLogin, null);
            finish();
        } else if (result.equals("ACCESS_GRANTED")) {
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

            mainHandler = new Handler();

            //instantiate main handler
            setupListView(savedInstanceState);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        deleteButton = menu.findItem(R.id.main_menu_delete_note);
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
            case R.id.main_menu_sync_notes:
                manager.selectAllNotes(false);
                itemsAdapter.displayCheckBoxes(false);
                manager.saveNotes();
                itemsAdapter.notifyDataSetChanged();
                return true;
            case R.id.main_menu_load_notes:
                wakeUpToolBar(act_main_toolbar);
                manager.selectAllNotes(false);
                itemsAdapter.displayCheckBoxes(false);
                manager.updateData(itemsAdapter, this);
                itemsAdapter.notifyDataSetChanged();
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
            case R.id.main_menu_action_search:
                wakeUpToolBar(act_main_toolbar);
                return true;
            case R.id.main_menu_settings:
                return true;
            case R.id.main_menu_debug_action:
                Calendar c = Calendar.getInstance();
                Toast.makeText(this.getApplicationContext(), Integer.toString(c.get(Calendar.MONTH)) + Integer.toString(c.get(Calendar.DATE)) + Integer.toString(c.get(Calendar.YEAR)), Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
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
                note n = new note(itemText, "tap to add a description");
                manager.add(n);
                n.saveEventually();
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
        if (deleteButton.isVisible() && itemsAdapter.checkboxVisible) {
            return true;
        } else {
            return false;
        }
    }

    private void setDeleteMode(int selectedItem, boolean state) {
        manager.selectAllNotes(false);
        itemsAdapter.displayCheckBoxes(state);
        manager.selectNote(selectedItem, state);
        deleteButton.setVisible(state);
        act_main_lv_Items.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    private void setupListView(Bundle savedInstanceState) {
        //re-put the values if the user rotates phone, switches app or whatever
        if (savedInstanceState != null) {
            try {
                ArrayList<note> t = savedInstanceState.getParcelableArrayList("notes");
                manager.setNotesList(t);
                act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
                itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, manager.getNotesList(), this, manager);
                act_main_lv_Items.setAdapter(itemsAdapter);
                itemsAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Util.getInstance().printStackTrace(e, TAG);
                act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
                itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, manager.getNotesList(), this, manager);
                manager.updateData(itemsAdapter, this);
                act_main_lv_Items.setAdapter(itemsAdapter);
            }
            noNotesMessage.setVisibility(View.INVISIBLE);
            hideOrShowNoNotesMessage();
        } else {
            act_main_lv_Items = (ListView) findViewById(R.id.act_main_lv_Items);
            itemsAdapter = new CustomListViewAdapter(this, R.layout.list_view_item_layout, manager.getNotesList(), this, manager);
            manager.updateData(itemsAdapter, this);
            act_main_lv_Items.setAdapter(itemsAdapter);
            itemsAdapter.notifyDataSetChanged();
            manager.notifyAllObservers();
        }

        setupListViewListener();
        itemsAdapter.notifyDataSetChanged();
        manager.notifyAllObservers();
    }

    private void setupListViewListener() {

        //onClickListener
        act_main_lv_Items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //inflate the selected item's edit layout/start activity
                if (isListInDeleteMode()) {
                    manager.selectNote(position, !manager.getNote(position).isSelected());
                    manager.getNote(position).notifyObservers();
                    //itemsAdapter.notifyDataSetChanged();
                } else {
                    //inflate the selected item's edit layout/start activity
                    Intent i = new Intent(MainActivity.this, EditNoteActivity.class);
                    i.putExtra("NOTE_TITLE", manager.getNoteTitle(position));
                    i.putExtra("NOTE_DESCRIPTION", manager.getNoteDescription(position));
                    i.putExtra("NOTE_INDEX", position);
                    startActivityForResult(i, REQUEST_CODE_EDIT_NOTE);
                }
            }
        });

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
            loader.setVisibility(View.VISIBLE);
            loader.bringToFront();
            //implement action interface listener
            loader.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    manager.selectAllNotes(false);
                    itemsAdapter.displayCheckBoxes(false);
                    // TODO: 8/10/2016 change loadnotes to a boolean return so we know if the data changed
                    manager.updateData(itemsAdapter, MainActivity.this);
                    itemsAdapter.notifyDataSetChanged();
                    manager.notifyAllObservers();
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loader.setRefreshing(false);
                        }
                    }, 800);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        if (this.manager != null) {
            savedState.putParcelableArrayList("notes", manager.getNotesList());
            super.onSaveInstanceState(savedState);
        }
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

        this.setSupportActionBar(act_main_toolbar);
    }

    private void setBackgroundToNoNotes() {
        RelativeLayout.LayoutParams myParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        myParams.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
        mainLayout.addView(noNotesMessage, myParams);
    }

    public void hideOrShowNoNotesMessage() {
        AlphaAnimation fadeIn = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.fade_in);
        AlphaAnimation fadeOut = (AlphaAnimation) AnimationUtils.loadAnimation(this, R.anim.fade_out);


        //if items arraylist has items in it still
        /********************************************/
        if (manager.getNotesList().isEmpty() == false) {

            if (act_main_lv_Items.isShown() == false) {
                noNotesMessage.startAnimation(fadeOut);
                act_main_lv_Items.startAnimation(fadeIn);
                noNotesMessage.setVisibility(View.INVISIBLE);
                act_main_lv_Items.setVisibility(View.VISIBLE);
                noNotesMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noNotesMessage.setVisibility(View.INVISIBLE);
                        act_main_lv_Items.setVisibility(View.VISIBLE);
                    }
                }, 800);
            }
            /********************************************/


            //else if items ArrayList is actually empty
            /********************************************/
        } else if (manager.getNotesList().isEmpty() == true) {

            if (act_main_lv_Items.isShown() == true) {
                noNotesMessage.startAnimation(fadeIn);
                act_main_lv_Items.startAnimation(fadeOut);
                noNotesMessage.setVisibility(View.VISIBLE);
                act_main_lv_Items.setVisibility(View.INVISIBLE);
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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        switch (reqCode) {
            case REQUEST_CODE_EDIT_NOTE:
                if (data != null) {
                    String title, description;
                    int index;

                    title = data.getStringExtra("NOTE_TITLE");
                    description = data.getStringExtra("NOTE_DESCRIPTION");
                    index = data.getIntExtra("NOTE_INDEX", -1);

                    manager.setNoteTitle(index, title);
                    manager.setNoteDescription(index, description);
                    manager.notifyAllObservers();
                    itemsAdapter.notifyDataSetChanged();
                    manager.getNote(index).updateParse();
                }
                break;
            case REQUEST_CODE_LOGIN_CREDENTIALS:
                Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
                if (resultCode == 0) {
                    //fail login, start activity again
                    startActivityForResult(intentLogin, REQUEST_CODE_LOGIN_CREDENTIALS);
                } else if (resultCode == 1) {
                    this.onCreate(new Bundle());
                } else {
                    startActivityForResult(intentLogin, REQUEST_CODE_LOGIN_CREDENTIALS);
                }
                break;
        }
        Util.getInstance().hideKeyboard(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.prest.simpletodo/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.prest.simpletodo/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}