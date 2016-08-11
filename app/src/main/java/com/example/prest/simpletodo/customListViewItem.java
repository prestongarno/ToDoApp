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

    // TODO: 8/10/2016 Possible need to get parent view and call notifyDataSetChanged() in order to display changes

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
        init();
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

    public void setCheckBoxTag(int currentPosition) {
        checkBox.setTag(currentPosition);
    }

/*    @Override
    public void setTag(Object o) {
        if (o instanceof MainActivity.NoteHolder) {
            checkBox.setTag(o);
        }
    }*/

    public CheckBox getNoteCheckBox(){return this.checkBox;}

    @Override
    public void update(note n) {
        this.Title.setText(n.getTitle());
        this.Description.setText(n.getDescription());
        this.Date.setText(n.getDate().toString());
        this.checkBox.setChecked(n.isSelected());
    }

}
