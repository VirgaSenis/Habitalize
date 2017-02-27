package com.iunus.habitualize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Iunus on 08/02/2017.
 */

public class HabitListAdapter extends BaseAdapter {

    Context context;
    List<Habit> habits;
    LayoutInflater inflater;

    public HabitListAdapter(Context context, List<Habit> habits) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.habits = habits;
        this.context = context;
    }

    @Override
    public int getCount() {
        return habits.size();
    }

    @Override
    public Object getItem(int position) {
        return habits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return habits.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Habit habit = (Habit) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_habit, parent, false);
        }

        TextView titleView = (TextView) convertView.findViewById(R.id.view_title);
        titleView.setText(habit.getName());

        Calendar startDate = Utility.toCalendar(habit.getStartDate());
        int diff = Utility.compareDate(Calendar.getInstance(), startDate);
        TextView dateView = (TextView) convertView.findViewById(R.id.view_startDate);
        dateView.setText(String.format(Locale.ENGLISH, "Began %d day(s) ago (%s)", diff, habit.getStartDate()));

        TextView progressView = (TextView) convertView.findViewById(R.id.view_progress_total);
        progressView.setText(String.format(Locale.ENGLISH, "Spent %d %s in total",
                habit.getProgressTotal(), context.getString(R.string.measure_common)));

        return convertView;
    }

}
