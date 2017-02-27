package com.iunus.habitualize;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;


import java.util.Calendar;




public class PickDateActivity extends AppCompatActivity {

    final public static int DATE_UNCHANGED = 0;
    final public static int DATE_CHANGED = 1;

    private CalendarView calView;
    private Calendar pickedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_date);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_pick_date);
        toolbar.setTitle("Pick Date");
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        pickedDate = (Calendar) intent.getSerializableExtra("pickedDate");

        calView = (CalendarView) findViewById(R.id.calendarView_main);

        setupCalendar();

        setDateText();
    }

    private void setupCalendar() {
        Calendar maxDate = Calendar.getInstance();
        setToLastDayOfWeek(maxDate);
        calView.setMaxDate(maxDate.getTimeInMillis());

        calView.setDate(pickedDate.getTimeInMillis());

        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                pickedDate.set(year, month, dayOfMonth);
                setDateText();
            }
        });
    }

    private void setToLastDayOfWeek(Calendar cal) {
        cal.set(Calendar.DAY_OF_WEEK, 1);
        cal.add(Calendar.WEEK_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
    }

    private void setDateText() {
        TextView textView = (TextView) findViewById(R.id.textView_date);
        textView.setText(Utility.calendarToDisplayableString(pickedDate));
    }

    /****** Methods handling button clicks ********/

    public void cancelActivity(View view) {
        setResult(DATE_UNCHANGED);
        finish();
    }

    public void submitDateChange(View view) {
        Intent data = new Intent();

        data.putExtra("calendar", pickedDate);
        setResult(DATE_CHANGED, data);

        finish();
    }
}
