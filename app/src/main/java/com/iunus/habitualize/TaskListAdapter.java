package com.iunus.habitualize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Iunus on 01/01/2017.
 */

public class TaskListAdapter extends BaseAdapter {
    private List<Task> tasks;
    private static LayoutInflater inflater = null;

    public TaskListAdapter(Context context, List<Task> tasks) {
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
            convertView = inflater.inflate(R.layout.item_task, parent, false);
        }

        Utility.setTaskRowValues(convertView, task, 0);

        return convertView;
    }


}
