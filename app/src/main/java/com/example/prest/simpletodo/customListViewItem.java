package com.example.prest.simpletodo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Preston Garno on 8/8/2016.
 */
public class customListViewItem extends RelativeLayout implements NotesListObserver {

    private Context c;
    private TextView Title, Description, Date;
    private CheckBox checkBox;

    public customListViewItem(Context context) {
        super(context);
        this.c = context;
        init();
    }

    public customListViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.c = context;
    }

    public customListViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.c = context;
        init();
    }

    /**
     * Initialize and inflate the view.  Assigns the values to the instance variables
     */
    private void init() {
        inflate(getContext(), R.layout.list_view_item_layout, this);
        Title = (TextView) findViewById(R.id.textView_note_title);
        Description = (TextView) findViewById(R.id.note_description);
        Date = (TextView) findViewById(R.id.date);
        checkBox = (CheckBox) findViewById(R.id.selector);
    }

    @Override
    public void update(note n) {
        this.Title.setText(n.getTitle());
        this.Description.setText(n.getDescription());
        this.Date.setText(n.getDate().toString());
    }

    /**
     * The real reason why I rewrote the entire app unfortunately :(
     * @return checkbox state
     */
    public boolean isCheckBoxSelected() {
        return checkBox.isSelected();
    }

}
