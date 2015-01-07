package com.example.android.contactslist.ui.notesEditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.contactslist.R;
import com.example.android.contactslist.util.TimeStamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tyson Macdonald on 12/20/2014.
 * Based on the example in google's developer paeges
 * (http://developer.android.com/guide/topics/ui/dialogs.html)
 * However, the callback from that example only works when the dialog fragment is
 * called from a FragmentActivity (Activity) as the onAttach() method brings a reference
 * to the activity.
 *
 * This dialog fragment
 */

public class NotesEditorDialogFragment extends DialogFragment {

    // Use this instance of the interface to deliver action events
    NotesEditorDialogListener mListener = null;
    String mContactNotes = "";
    String mNewNotes = "";
    String mContactName = "";
    final static String CONTACT_NAME = "contactName";
    final static String CONTACT_NOTES = "contactNotes";
    final static String CONTACT_NEW_NOTES = "contactNewNotes";



    public static NotesEditorDialogFragment newInstance(String contactName,
                                                        String contactNotes,
                                                        int id) {
        NotesEditorDialogFragment dialog = new NotesEditorDialogFragment();
        Bundle args = new Bundle();
        args.putString(CONTACT_NAME, contactName);
        args.putString(CONTACT_NOTES, contactNotes);
        args.putInt("id", id);
        dialog.setArguments(args);

        return dialog;
    }


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NotesEditorDialogListener {
        public void onNotesEditorDialogPositiveClick(String notes);
    }



    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
/*    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NotesEditorDialogListener so we can send events to the host
            mListener = (NotesEditorDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NotesEditorDialogListener");
        }
    }
*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers

        // set the listener to the fragment as set in setTargetFragment()
        mListener = (NotesEditorDialogListener) getTargetFragment();

        mContactNotes = getArguments().getString(CONTACT_NOTES);
        mContactName = getArguments().getString(CONTACT_NAME);

        if(savedInstanceState != null){
            mContactNotes = savedInstanceState.getString(CONTACT_NOTES);
            mNewNotes = savedInstanceState.getString(CONTACT_NEW_NOTES);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.event_notes_title);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        View view = inflater.inflate(R.layout.notes_editor_fragment, null);
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);



        // Set up constituent views

        final TextView characterCountView = (TextView) view.findViewById(R.id.event_note_character_count_view);


        final EditText newNotesView = (EditText) view.findViewById(R.id.new_notes);
        newNotesView.setText(mNewNotes);
        newNotesView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                characterCountView.setText(Integer.toString((int)newNotesView.getText().length()) + "/140");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        characterCountView.setText(Integer.toString((int)newNotesView.getText().length()) + "/140");


        // Specify the type of input expected; this, for example,
        // sets the input as a password, and will mask the text
        newNotesView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);

        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.add_date_check_box);

        final TextView nameView = (TextView) view.findViewById(R.id.event_notes_title);
        nameView.setText(mContactName);

        final TextView contactNotesView = (TextView) view.findViewById(R.id.contact_notes_view);
        contactNotesView.setText(mContactNotes);



/*
        // Set up the buttons
        builder.setNeutralButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Append the new string to the existing notes
                mNewNotes = newNotesView.getText().toString();

                if(!mNewNotes.isEmpty()) {
                    // Send the positive button event back to the host activity
                    mListener.onNotesEditorDialogPositiveClick(mNewNotes);
                }
            }
        });
*/
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do something with the new text

                final String newNotes =  newNotesView.getText().toString();

                // proceed with the contact notes update only if there's content
                if (!newNotes.isEmpty()) {

                    if(checkBox.isChecked()) {
                        //Append the new string to the existing notes
                        mNewNotes = TimeStamp.getDateHeaderString(getActivity()) + newNotes;
                    }else {
                        mNewNotes = newNotes;
                    }

                    // Send the positive button event back to the host activity
                    mListener.onNotesEditorDialogPositiveClick(mNewNotes);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    /**
     * When the Fragment is being saved in order to change activity state, save the
     * currently-selected contact.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saves the contact Uri
        outState.putString(CONTACT_NOTES, mContactNotes);
        outState.putString(CONTACT_NEW_NOTES, mNewNotes);
    }

}



