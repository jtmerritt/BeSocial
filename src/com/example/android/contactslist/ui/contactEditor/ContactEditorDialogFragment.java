package com.example.android.contactslist.ui.contactEditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.contactslist.FloatingActionButton.FloatingActionButton2;
import com.example.android.contactslist.R;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.util.ImageLoader;

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

public class ContactEditorDialogFragment extends DialogFragment {

    // Use this instance of the interface to deliver action events
    ContactEditorDialogListener mListener = null;

    private final static String CONTACT_NAME = "contactName";
    private final static String GROUP_NAME = "groupName";

    private final static String EVENT_INTERVAL_LIMIT = "eventIntervalLimit";
    private final static String DECAY_RATE = "decayRate";
    private final static String PRIMARY_GROUP_MEMBERSHIP = "primeGroupMembership";
    private final static String PRIMARY_GROUP_BEHAVIOR = "primeGroupBehavior";

    private final static int MAX_SEEK_BAR_DECAY_RATE = 100;

    private float mDecayRate = 0;
    private EditText mDecayRateEditText;

    private SeekBar mDecayRateSeekBar;




    public static ContactEditorDialogFragment newInstance(ContactInfo contactInfo,
                                                          ContactInfo groupInfo,
                                                        int id) {
        ContactEditorDialogFragment dialog = new ContactEditorDialogFragment();
        Bundle args = new Bundle();
        args.putString(CONTACT_NAME, contactInfo.getName());
        args.putString(GROUP_NAME, groupInfo.getName());
        args.putInt(EVENT_INTERVAL_LIMIT, contactInfo.getEventIntervalLimit());
        args.putFloat(DECAY_RATE, contactInfo.getDecay_rate());
        args.putInt(PRIMARY_GROUP_BEHAVIOR, groupInfo.getBehavior());

        args.putInt("id", id);
        dialog.setArguments(args);

        return dialog;
    }


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ContactEditorDialogListener {
        public void onContactEditorDialogPositiveClick(Float newDecayRate);
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers

        // set the listener to the fragment as set in setTargetFragment()
        mListener = (ContactEditorDialogListener) getTargetFragment();

        final String contactName = getArguments().getString(CONTACT_NAME);
        mDecayRate = getArguments().getFloat(DECAY_RATE);
        final int eventIntervalLimit = getArguments().getInt(EVENT_INTERVAL_LIMIT);
        final int primary_behavior = getArguments().getInt(PRIMARY_GROUP_BEHAVIOR);
        final long primary_group_membership = getArguments().getLong(PRIMARY_GROUP_MEMBERSHIP);
        final String mGroupName = getArguments().getString(GROUP_NAME);


        if(savedInstanceState != null){
            mDecayRate = savedInstanceState.getFloat(DECAY_RATE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mGroupName);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        View detailView = inflater.inflate(R.layout.contact_editor_fragment, null);
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(detailView);



        // Set up constituent views
        final ImageView groupTypeIcon = (ImageView) detailView.findViewById(R.id.group_function_image);
        //set group function icon
        switch (primary_behavior){
            default:
            case ContactInfo.IGNORED:
                groupTypeIcon.setImageResource(
                        R.drawable.ic_visibility_off_black_48dp);
                break;
            case ContactInfo.PASSIVE_BEHAVIOR:
                groupTypeIcon.setImageResource(
                        R.drawable.ic_visibility_black_48dp);
                break;
            case ContactInfo.COUNTDOWN_BEHAVIOR:
                groupTypeIcon.setImageResource(
                        R.drawable.ic_timelapse_black_48dp);
                break;
            case ContactInfo.AUTOMATIC_BEHAVIOR:
                groupTypeIcon.setImageResource(
                        R.drawable.ic_track_changes_black_48dp);
                break;
            case ContactInfo.RANDOM_BEHAVIOR:
                groupTypeIcon.setImageResource(
                        R.drawable.ic_shuffle_black_48dp);
                break;
            case ContactInfo.EXPONENTIAL_BEHAVIOR:
                groupTypeIcon.setImageResource(
                        R.drawable.ic_search_black_48dp);
                break;
        }


        //Interval view
        final TextView mGroupIntervalLimitView = (TextView) detailView
                .findViewById(R.id.group_interval_limit_text_view);
        mGroupIntervalLimitView.setText(Integer.toString(eventIntervalLimit) + " " +
                getResources().getString(R.string.Days));
        // if there is no countdown behavior, hide the text view
        if(primary_behavior != ContactInfo.COUNTDOWN_BEHAVIOR) {
            mGroupIntervalLimitView.setVisibility(View.INVISIBLE);
        }


        // Set the prime behavior view
        final String groupBehaviorString = getResources()
                .getStringArray(R.array.array_of_group_behaviors)[primary_behavior];
        final TextView mGroupPrimaryBehaviorView = (TextView) detailView
                .findViewById(R.id.group_primary_behavior_text_view);
        mGroupPrimaryBehaviorView.setText(groupBehaviorString);


        mDecayRateEditText = (EditText) detailView.findViewById(R.id.decay_rate_button);
        mDecayRateEditText.setText(Float.toString(mDecayRate));

        mDecayRateSeekBar = (SeekBar) detailView.findViewById(R.id.decay_factor_seek_bar);
        mDecayRateSeekBar.setMax(MAX_SEEK_BAR_DECAY_RATE);
        mDecayRateSeekBar.setProgress((int)(mDecayRate*(float)MAX_SEEK_BAR_DECAY_RATE));
        mDecayRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // using onStopTrackingTouch because the progressChange would update every time
            // the text was manually updated, causing an annoying jump in the text cursor
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDecayRate = (float)seekBar.getProgress()/(float)MAX_SEEK_BAR_DECAY_RATE;
                mDecayRateEditText.setText(Float.toString(mDecayRate));
            }
        });

        mDecayRateEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                //  Do things when the text changes
                try {
                    mDecayRate = Float.parseFloat(mDecayRateEditText.getText().toString());

                    // limit the range to [0:1]
                    if(mDecayRate > 1){
                        mDecayRate = 1;
                        mDecayRateEditText.setText(Float.toString(mDecayRate));
                    }
                    if(mDecayRate <0){
                        mDecayRate = 0;
                        mDecayRateEditText.setText(Float.toString(mDecayRate));
                    }

                    mDecayRateSeekBar.setProgress((int)(mDecayRate*(float)MAX_SEEK_BAR_DECAY_RATE));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });



        // disable the decay rate interface for behavior modes that don't need it
        switch (primary_behavior){
            default:
            case ContactInfo.IGNORED:
            case ContactInfo.PASSIVE_BEHAVIOR:
            case ContactInfo.COUNTDOWN_BEHAVIOR:
            case ContactInfo.RANDOM_BEHAVIOR:
                mDecayRateSeekBar.setEnabled(false);
                mDecayRateEditText.setEnabled(false);
                break;
            case ContactInfo.AUTOMATIC_BEHAVIOR:
            case ContactInfo.EXPONENTIAL_BEHAVIOR:

                break;
        }

        /*
        // Set up the buttons
        builder.setNeutralButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        */
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do something with the new text


                    // Send the positive button event back to the host activity
                    mListener.onContactEditorDialogPositiveClick(mDecayRate);

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
        outState.putFloat(DECAY_RATE, mDecayRate);
    }

}



