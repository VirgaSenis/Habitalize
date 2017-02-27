package com.iunus.habitualize;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.content.pm.ActivityInfo;

import android.os.Bundle;
import android.os.CountDownTimer;

import android.support.annotation.Nullable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Iunus on 31/01/2017.
 */

public class TimerDialogFragment extends DialogFragment {

    public static final int MILLIS_IN_SECOND = 1000;
    public static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;

    private CountDownTimer timer;
    private EditText hoursField;
    private EditText minutesField;
    private TextView secondsText;
    private long duration;
    private int initialHours;
    private int initialMinutes;
    private Button nButton;
    private Button pButton;
    private View mView;

    Alarm alarm;

    private boolean vibrateOn;
    private boolean soundOn;

    public static TimerDialogFragment newInstance() {
        TimerDialogFragment fragment = new TimerDialogFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alarm = new Alarm();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_timer_dialog, container, false);
        
        getDialog().setTitle("Set Timer");
        getDialog().setCanceledOnTouchOutside(false);

        getActivity().setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        vibrateOn = false;
        soundOn = false;
        
        setupTimeFields();

        setupCheckBoxes();

        return mView;
    }

    private void setupCheckBoxes() {
        CheckBox soundCheckBox = (CheckBox) mView.findViewById(R.id.checkbox_sound);
        soundCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                soundOn = isChecked;
            }
        });

        CheckBox vibrateCheckBox = (CheckBox) mView.findViewById(R.id.checkbox_vibrate);
        vibrateCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrateOn = isChecked;
            }
        });
    }

    private void setupTimeFields() {
        hoursField = (EditText) mView.findViewById(R.id.field_hour);
        hoursField.addTextChangedListener(textWatcher);
        hoursField.setOnClickListener(listener);

        minutesField = (EditText) mView.findViewById(R.id.field_minute);
        minutesField.addTextChangedListener(textWatcher);
        minutesField.setOnClickListener(listener);

        secondsText = (TextView) mView.findViewById(R.id.view_second);

        nButton = (Button) mView.findViewById(R.id.button_negative);
        nButton.setText(R.string.cancel);
        nButton.setOnClickListener(nClickListener);

        pButton = (Button) mView.findViewById(R.id.button_positive);
        pButton.setText(R.string.start);
        pButton.setOnClickListener(pClickListener);
    }

    EditText.OnClickListener listener = new EditText.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((EditText)v).setSelection(2);
        }
    };

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            EditText field;
            int maxValue;
            if (s == minutesField.getEditableText()) {
                field = minutesField;
                maxValue = 59;
            } else {
                field = hoursField;
                maxValue = 23;
            }

            field.removeTextChangedListener(textWatcher);
            if (s.length() == 1) {
                field.setText(String.format(Locale.ENGLISH, "0%s", s.toString()));
            } else if (s.length() == 3) {
                int value = Integer.parseInt(s.toString());
                if (value > 100) {  // Text before had two non-zero digits
                    field.setText(String.format(Locale.ENGLISH, "0%s", s.toString().substring(2)));
                } else if (value > maxValue) {
                    field.setText(R.string.value_minutes_max);
                } else {
                    field.setText(s.toString().substring(1));
                }
            }
            field.addTextChangedListener(textWatcher);

            field.setSelection(2);  // place cursor position to 2
        }
    };

    Button.OnClickListener pClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String text = pButton.getText().toString();
            switch (text) {
                case "Start" :
                    if (!isTimeSet()) {
                        Toast.makeText(getActivity(), "Please Set Time!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Utility.hideSoftKeyboard(getActivity(), v);
                    initialHours = Utility.editTextToInt(hoursField);
                    initialMinutes = Utility.editTextToInt(minutesField);
                    duration = getDurationInMillis();
                    pButton.setText(R.string.pause);
                    nButton.setText(R.string.reset);
                    toggleFieldFocusable(false);

                    runTimer();
                    break;
                case "Pause" :
                    pButton.setText(R.string.resume);
                    stopTimer();
                    toggleFieldFocusable(true);
                    break;
                case "Resume" :
                    pButton.setText(R.string.pause);
                    runTimer();
                    toggleFieldFocusable(false);
                default:
                    break;
            }
        }
    };

    private boolean isTimeSet() {
        return getDurationInMillis() != 0;
    }

    Button.OnClickListener nClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String text = nButton.getText().toString();
            switch (text) {
                case "Cancel" :
                    dismiss();
                    break;
                case "Reset" :
                    stopTimer();
                    resetTimeViews();
                    toggleFieldFocusable(true);
                    pButton.setText(R.string.start);
                    nButton.setText(R.string.cancel);
                    break;
            }
        }
    };

    private void runTimer() {
        long duration = getDurationInMillis();

        timer = new CountDownTimer(duration, MILLIS_IN_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                int hours = (int) millisUntilFinished / (MILLIS_IN_HOUR);
                hoursField.setText(String.format(Locale.ENGLISH, "%02d", hours));

                int minutes = (int) (millisUntilFinished / (MILLIS_IN_MINUTE)) % 60;
                minutesField.setText(String.format(Locale.ENGLISH, "%02d", minutes));

                int seconds = (int) (millisUntilFinished / MILLIS_IN_SECOND) % 60;
                secondsText.setText(String.format(Locale.ENGLISH, "%02d", seconds));
            }

            @Override
            public void onFinish() {
                secondsText.setText(R.string.value_time_min);
                alertUser();
                updateTaskProgress();
                resetTimeViews();
                toggleFieldFocusable(true);
                pButton.setText(R.string.start);
                nButton.setText(R.string.cancel);
            }
        };

        timer.start();
    }

    private void stopTimer() {
        timer.cancel();
    }

    private void alertUser() {
        if (soundOn) {
            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.ringtone);
            mp.start();
        }

        if (vibrateOn) {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(new long[]{0, 1000, 1000, 1000}, 3);
        }
    }

    private void toggleFieldFocusable(boolean isFocusable) {
        hoursField.setFocusableInTouchMode(isFocusable);
        hoursField.setFocusable(isFocusable);
        minutesField.setFocusable(isFocusable);
        minutesField.setFocusableInTouchMode(isFocusable);
    }


    /*************** *******************/

    private void resetTimeViews() {
        hoursField.setText(String.format(Locale.ENGLISH, "%02d", initialHours));
        minutesField.setText(String.format(Locale.ENGLISH, "%02d", initialMinutes));
        secondsText.setText(R.string.value_time_min);
    }

    private void updateTaskProgress() {
        TasksFragment fragment = (TasksFragment) getTargetFragment();
        fragment.onTimerDismissed(duration);
    }

    private long getDurationInMillis() {
        return  Utility.editTextToInt(hoursField) * MILLIS_IN_HOUR +
                Utility.editTextToInt(minutesField) * MILLIS_IN_MINUTE +
                Utility.textViewToInt(secondsText) * MILLIS_IN_SECOND;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

}
