package com.iunus.habitualize;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HabitFormActivity extends AppCompatActivity {
    
    public final static int DAYS_IN_WEEK = 7;

    public final static int DAY_UNSELECTED = -1;
    public final static int DAY_SELECTED = 1;

    private DBHelper dbHelper;
    private List<View> daysSelector;
    private Habit habit;
    private int requestCode;

    private EditText nameField;
    private EditText durationField;
    private String incrementType;
    private EditText incrementField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_habit_form);
        setSupportActionBar(toolbar);

        dbHelper = new DBHelper(this);

        setUpDaysSelector();

        nameField = (EditText) findViewById(R.id.field_name);
        durationField = (EditText) findViewById(R.id.field_duration);
        incrementField = (EditText) findViewById(R.id.field_increment);



        Intent intent = getIntent();
        requestCode = intent.getIntExtra("requestCode", MainActivity.REQUEST_ADD_HABIT);
        if (requestCode == MainActivity.REQUEST_ADD_HABIT) {
            setTitle("New Habit");
            incrementType = getResources().getString(R.string.percent_string);
            ((RadioButton) findViewById(R.id.radio_percent)).setChecked(true);
        } else {
            habit = (Habit) intent.getSerializableExtra("habit");
            setTitle(String.format("Edit %s", habit.getName()));

            fillInForm();

            Button button = (Button) findViewById(R.id.button_positive);
            button.setText(R.string.update);
        }

        setUpTextViewButtons();
    }

    private void fillInForm() {
        nameField.setText(habit.getName());
        durationField.setText(String.format(Locale.ENGLISH, "%d", habit.getDuration()));
        incrementField.setText(String.format(Locale.ENGLISH, "%d", habit.getIncrementValue()));
        incrementType = habit.getIncrementType();
        if (incrementType.equals(getResources().getString(R.string.percent_string))) {
            ((RadioButton) findViewById(R.id.radio_percent)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radio_minutes)).setChecked(true);
        }

        String workingDays = habit.getWorkingDays();
        for (int i=0; i<habit.getNumberOfWorkingDays(); i++) {
            int pos = workingDays.charAt(i) - '0';
            daysSelector.get(pos).setSelected(true);
        }
    }

    /******** listener for clear fields *********/
    
    private void setUpTextViewButtons() {
        TextView textView = (TextView) findViewById(R.id.button_clear);
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((TextView) v).setText(R.string.clear_ul);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((TextView) v).setText(R.string.clear);
                    clearFields();
                }

                return true;
            }
        });

        TextView toggleButton = (TextView) findViewById(R.id.button_toggle);
        toggleButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((TextView) v).setText(R.string.toggle_all_ul);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((TextView) v).setText(R.string.toggle_all);

                    toggleAllDays(null);
                }

                return true;
            }
        });
    }

    public void toggleAllDays(View view) {
        for (View v : daysSelector) {
            v.setSelected(!v.isSelected());
        }
    }



    public void clearFields() {
        nameField.setText("");

        EditText taskDurationField = (EditText) findViewById(R.id.field_duration);
        taskDurationField.setText("");

        EditText incrementField = (EditText) findViewById(R.id.field_increment);
        incrementField.setText("");

        for (View v : daysSelector) {
            v.setSelected(false);
        }
    }

    /****** Spinner methods********/

    public void toggleDaySelection(View view) {
        view.setSelected(!view.isSelected());
    }


    public void onRadioClick(View view) {
        switch (view.getId()) {
            case R.id.radio_percent :
                incrementType = getResources().getString(R.string.percent_string);
                break;
            case R.id.radio_minutes :
                incrementType = getResources().getString(R.string.minutes);
                break;
        }
    }

    /********** Methods handling button clicks ****************/

    public void cancelActivity(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }



    public void doPositiveAction(View view) {
        switch (requestCode) {
            case MainActivity.REQUEST_ADD_HABIT :
                addHabit();
                break;
            case HabitsFragment.REQUEST_EDIT_HABIT :
                updateHabit();
                break;
        }
    }

    public void addHabit() {
        Habit habit = createHabit();
        if (habit == null) {
            return;
        }

        // Need  task inserted first, so that we can get its ID generated by DB
        insertHabit(habit);
        List<Task> tasks = Utility.createTasksForThisWeek(habit);
        Utility.insertTasksToDB(dbHelper, tasks, habit.getId());

        setResult(RESULT_OK);
        dbHelper.close();
        finish();
    }

    private Habit createHabit() {
        String name = nameField.getText().toString();

        EditText durationField = (EditText) findViewById(R.id.field_duration);
        String durationStr = durationField.getText().toString();

        String workingDays = "";
        for (int i=0; i<DAYS_IN_WEEK; i++) {
            if (daysSelector.get(i).isSelected()) {
                workingDays += i;
            }
        }

        if (!isFormFilled(name, durationStr, workingDays)) {
            return null;
        }

        // Cannot parse to int until we make sure the string is non-empty.
        int duration = Integer.parseInt(durationStr);

        String startDate = Utility.calendarToString(Calendar.getInstance());

        int increment = 0;
        String incrementStr = incrementField.getText().toString();
        if (!Utility.isStringEmpty(incrementStr)) {
            increment = Integer.parseInt(incrementStr);
        }

        return new Habit(Habit.ID_UNSET, name, duration, 0, startDate, workingDays, incrementType, increment);
    }

    private boolean isFormFilled(String name, String duration, String workingDays) {
        if (Utility.isStringEmpty(name)) {
            Toast.makeText(this, "Name Required!", Toast.LENGTH_LONG).show();
            return false;
        } else if (Utility.isStringEmpty(duration)) {
            Toast.makeText(this, "Duration Required!", Toast.LENGTH_LONG).show();
            return false;
        } else if (Utility.isStringEmpty(workingDays)) {
            Toast.makeText(this, "Select Working Days!", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void insertHabit(Habit habit) {
        dbHelper.insertHabit(habit);
    }



    /** Methods for updating task **/

    private void updateHabit() {
        String newName = nameField.getText().toString();
        int newDuration = Utility.editTextToInt(durationField);
        String newWorkingDays = getSelectedDays();
        int newIncrementValue = Utility.editTextToInt(incrementField);

        if (!habit.getName().equals(newName)) {
            dbHelper.updateHabitName(habit.getId(), newName);
        }

        if ( habit.getDuration() != newDuration ) {
            dbHelper.updateHabitDuration(habit.getId(), newDuration);
            dbHelper.updateDurationOfThisWeekTasks(habit.getId(), newDuration);
        }

        dbHelper.updateHabitDuration(habit.getId(), newDuration);

        if (!habit.getWorkingDays().equals(newWorkingDays)) {
            updateWorkingDays(newWorkingDays);
        }

        if ( habit.getIncrementType() != incrementType ) {
            dbHelper.updateIncrementType(habit.getId(), incrementType);
        }

        if ( habit.getIncrementValue() != newIncrementValue) {
            dbHelper.updateIncrementValue(habit.getId(), newIncrementValue);
        }

        finish();
    }

    private void updateWorkingDays(String newWorkingDays) {
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_MONTH);
        int lastDayOfMonth = Utility.getLastDayOfMonth(cal.get(Calendar.MONTH));

        // Add and delete tasks of this week, depending on the selection of days
        int[] daysArr = habit.getWorkingDaysComparisonArray(newWorkingDays);

        for (int i=today; i<=lastDayOfMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;

            if (daysArr[dayOfWeek] == DAY_UNSELECTED) {
                dbHelper.deleteTask(habit.getId(), Utility.calendarToString(cal));
            } else if (daysArr[dayOfWeek] == DAY_SELECTED && i >= today) {
                String date = Utility.calendarToString(cal);

                Task task = Utility.createTask(habit, date);
                dbHelper.insertTask(task, habit.getId());
            }
        }

        dbHelper.updateHabitWorkingDays(habit.getId(), newWorkingDays);
    }

    private String getSelectedDays() {
        String s = "";
        for (int i=0; i<7; i++) {
            if (daysSelector.get(i).isSelected()) {
                s += i;
            }
        }

        return s;
    }

    private void setUpDaysSelector() {
        daysSelector = new ArrayList<>();
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_days_selection);
        for (int i=0; i<layout.getChildCount(); i++) {
            daysSelector.add(layout.getChildAt(i));
        }
    }

}
