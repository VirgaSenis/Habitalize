package com.iunus.habitualize;


import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;

import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Iunus on 01/02/2017.
 */

public class ProgressDialogFragment extends DialogFragment {

    int max;
    int progress;
    String name;
    View dialogView;
    private SeekBar seekBar;
    private EditText progressField;

    static ProgressDialogFragment newInstance(String name, int max, int progress) {
        ProgressDialogFragment f = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("max", max);
        args.putInt("progress", progress);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        name = args.getString("name");
        max = args.getInt("max");
        progress = args.getInt("progress");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialogView = inflater.inflate(R.layout.fragment_progress_dialog, container, false);
        getDialog().setTitle(name);
        getDialog().setCanceledOnTouchOutside(false);

        seekBar = (SeekBar) dialogView.findViewById(R.id.dialog_seekbar);
        progressField = (EditText) dialogView.findViewById(R.id.field_progress);

        setUpView();
        setUpButtons(dialogView);

        return dialogView;
    }

    private void setUpView() {

        TextView maxView = (TextView) dialogView.findViewById(R.id.view_max);
        maxView.setText(String.format("(MAX: %d)", max));

        seekBar.setMax(max);
        progressField.setFilters(new InputFilter[]{new InputFilterMinMax(0, max)});

        progressField.setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressField.setSelection(progressField.getText().length());
            }
        });

        progressField.addTextChangedListener(watcher);


        setValues();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setProgress(progress);
                setValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int sLength = s.length();
            int selectionPos = sLength;

            if (sLength == 0) {  // "" -> "0"
                progressField.removeTextChangedListener(watcher);
                progressField.setText("0");
                progressField.addTextChangedListener(watcher);
                selectionPos++;
            } else if (sLength == 2 && s.toString().startsWith("0")) {   // "0" -> "02" -> "2"
                progressField.removeTextChangedListener(watcher);
                progressField.setText(s.toString().substring(1));
                progressField.addTextChangedListener(watcher);
                selectionPos--;
            }

            seekBar.setProgress(Integer.parseInt(progressField.getText().toString()));
            progressField.setSelection(selectionPos);
        }
    };

    private void setProgress(int progress) {
        this.progress = progress;
    }

    private void setValues() {
        SeekBar seekBar = (SeekBar) dialogView.findViewById(R.id.dialog_seekbar);
        seekBar.setProgress(progress);


        progressField.setText(Integer.toString(progress));

    }

    private void setUpButtons(View v) {
        Button setButton = (Button) v.findViewById(R.id.button_set);
        setButton.setOnClickListener(setButtonListener);

        Button maxButton = (Button) v.findViewById(R.id.button_max);
        maxButton.setOnClickListener(maxButtonListener);

        Button cancelButton = (Button) v.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(cancelButtonListener);
    }

    Button.OnClickListener setButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText progressField = (EditText) dialogView.findViewById(R.id.field_progress);
            int progress = Integer.parseInt(progressField.getText().toString());
            TasksFragment fragment = (TasksFragment) getTargetFragment();
            fragment.onProgressSet(progress);
            dismiss();
        }
    };

    Button.OnClickListener maxButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            setProgress(max);
            setValues();
        }
    };

    Button.OnClickListener cancelButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
}

