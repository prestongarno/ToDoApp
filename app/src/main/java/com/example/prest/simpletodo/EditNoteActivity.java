package com.example.prest.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditNoteActivity extends AppCompatActivity {

    private Button saveButton;
    private TextView title;
    private EditText titleEditor, descriptionEditor;
    private Button backButton, saveTitleButton;
    private String NoteTitle, NoteDescription;
    private int noteIndex;
    private String originalNoteDescription, originalNoteTitle;
    private static String[] results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        //access the buttons
        title = (TextView) this.findViewById(R.id.edit_note_title_bar);
        titleEditor = (EditText) this.findViewById(R.id.edit_note_edit_title);
        backButton = (Button) this.findViewById(R.id.edit_note_back_button);
        saveTitleButton = (Button) this.findViewById(R.id.edit_note_save_title);
        descriptionEditor = (EditText) this.findViewById(R.id.edit_description);

        NoteTitle = getIntent().getStringExtra("NOTE_TITLE");
        NoteDescription = getIntent().getStringExtra("NOTE_DESCRIPTION");
        originalNoteDescription = NoteDescription;
        originalNoteTitle = NoteTitle;

        noteIndex = getIntent().getIntExtra("NOTE_INDEX", -1);

        title.setText(NoteTitle);
        descriptionEditor.setText(NoteDescription);
        this.addActionListeners();
    }

    private void addActionListeners(){
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                backButton.setVisibility(View.INVISIBLE);
                backButton.setEnabled(false);
                saveTitleButton.setVisibility(View.VISIBLE);
                saveTitleButton.setEnabled(true);
                title.setVisibility(View.INVISIBLE);
                titleEditor.setText(title.getText());
                titleEditor.setVisibility(View.VISIBLE);
                titleEditor.requestFocus();
                titleEditor.setSelection(0, titleEditor.length());
                Util.getInstance().showKeyboard(EditNoteActivity.this);
                return true;
            }
        });
    }

    public void onSave(View view) {
        originalNoteTitle = title.getText().toString();
        originalNoteDescription = descriptionEditor.getText().toString();
    }

    public void onBackButton(View view) {
        //kill the activity here, give an option to save the note if needed
        Intent i = new Intent();
        i.putExtra("NOTE_TITLE", originalNoteTitle);
        i.putExtra("NOTE_DESCRIPTION", originalNoteDescription);
        i.putExtra("NOTE_INDEX", noteIndex);
        this.setResult(RESULT_OK, i);
        this.finish();
    }

    public void onSaveTitle(View view) {
        //save the title
        title.setText(titleEditor.getText());
        title.setVisibility(View.VISIBLE);
        titleEditor.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.VISIBLE);
        backButton.setEnabled(true);
        saveTitleButton.setVisibility(View.INVISIBLE);
        saveTitleButton.setEnabled(false);
        this.originalNoteTitle = titleEditor.getText().toString();
    }
}
