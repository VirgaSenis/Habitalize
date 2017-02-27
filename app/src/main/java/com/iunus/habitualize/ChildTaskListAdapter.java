package com.iunus.habitualize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Iunus on 07/02/2017.
 */

public class ChildTaskListAdapter extends BaseAdapter {
    private List<? extends Task> tasks;
    private Context context;
    private static LayoutInflater inflater = null;

    public ChildTaskListAdapter(Context context, List<? extends Task> tasks) {
        this.context = context;
        this.tasks = tasks;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = (Task) getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_task_item, parent, false);
        }

        String title = Utility.calendarToDisplayableString(Utility.toCalendar(task.getDate()));

        TextView titleView = (TextView) convertView.findViewById(R.id.title_view);
        titleView.setText(title);

        TextView taskMinutes = (TextView) convertView.findViewById(R.id.task_minutes);
        String minutesString = String.format(Locale.CANADA, "%d/%d %s",
                task.getProgress(), task.getDuration(), context.getString(R.string.measure_common));
        taskMinutes.setText(minutesString);

        ProgressBar taskProgressBar = (ProgressBar) convertView.findViewById(R.id.task_progress_bar);
        taskProgressBar.setMax(task.getDuration());
        taskProgressBar.setProgress(task.getProgress());

        return convertView;
    }
}
