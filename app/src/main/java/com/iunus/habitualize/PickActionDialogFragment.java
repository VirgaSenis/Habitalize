package com.iunus.habitualize;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Iunus on 01/02/2017.
 */

public class PickActionDialogFragment extends DialogFragment {

    public static final int ACTION_TIMER = 1;
    public static final int ACTION_PROGRESS = 2;

    private Button clockButton;
    private Button dragButton;

    public static PickActionDialogFragment newInstance() {
        PickActionDialogFragment f = new PickActionDialogFragment();

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_action_dialog, container, false);

        getDialog().setTitle("Pick Action");

        clockButton = (Button) view.findViewById(R.id.button_clock);
        clockButton.setOnClickListener(clockButtonListener);

        dragButton = (Button) view.findViewById(R.id.button_drag);
        dragButton.setOnClickListener(dragButtonListener);

        Button okButton = (Button) view.findViewById(R.id.button_ok);
        okButton.setOnClickListener(okButtonListener);

        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(cancelButtonListener);

        return view;
    }

    Button.OnClickListener clockButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            clockButton.setSelected(true);
            dragButton.setSelected(false);
        }
    };

    Button.OnClickListener dragButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            clockButton.setSelected(false);
            dragButton.setSelected(true);
        }
    };

    Button.OnClickListener okButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            TasksFragment fragment = (TasksFragment) getTargetFragment();
            if (clockButton.isSelected()) {
                fragment.onActionSelected(ACTION_TIMER);
            } else if (dragButton.isSelected()) {
                fragment.onActionSelected(ACTION_PROGRESS);
            } else {
                return;
            }
            dismiss();
        }
    };

    Button.OnClickListener cancelButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
}
