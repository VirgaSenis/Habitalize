package com.iunus.habitualize;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Iunus on 08/02/2017.
 */

public class TasksFragment extends Fragment {

    private static final long MILLIS_IN_MIN = 1000 * 60;
    private Context context;
    private View mainView;
    private List<Task> tasks;
    private Calendar dateOfTasks;
    private DBHelper dbHelper;
    private int clickedItemIdx;

    public static TasksFragment newInstance() {
        Bundle args = new Bundle();
        
        TasksFragment fragment = new TasksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        dateOfTasks = Calendar.getInstance();
        dbHelper = new DBHelper(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_tasks, container, false);

        setupDateBarButtons();
        setUpListItemClick();
        setViews();

        return mainView;
    }

    private void setViews() {
        setDateView();
        setListView();
    }

    private void setDateView() {
        TextView dateView = (TextView) mainView.findViewById(R.id.view_date);
        String dateText = Utility.calendarToDisplayableString(dateOfTasks);
        dateView.setText(dateText);
    }

    protected void setListView() {
        tasks = dbHelper.getTasksOfDate(Utility.calendarToString(dateOfTasks));

        ListView listView = (ListView) mainView.findViewById(R.id.listView_tasks);
        TaskListAdapter adapter = new TaskListAdapter(context, tasks);
        listView.setAdapter(adapter);
    }

    private void setupDateBarButtons() {
        ImageView leftArrow = (ImageView) mainView.findViewById(R.id.button_previous_date);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.setToPreviousDate(dateOfTasks);
                setViews();
            }
        });

        ImageView rightArrow = (ImageView) mainView.findViewById(R.id.button_next_date);
        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.setToNextDate(dateOfTasks);
                setViews();
            }
        });

        ImageView calendar = (ImageView) mainView.findViewById(R.id.button_pickDate);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PickDateActivity.class);

                intent.putExtra("pickedDate", dateOfTasks);
                startActivityForResult(intent, MainActivity.REQUEST_PICK_DATE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.REQUEST_PICK_DATE) {
            if (resultCode == PickDateActivity.DATE_CHANGED) {
                dateOfTasks = (Calendar) data.getSerializableExtra("calendar");
                setViews();

                Toast.makeText(getContext(), "Date Changed", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void setUpListItemClick() {
        ListView listView = (ListView) mainView.findViewById(R.id.listView_tasks);
        listView.setOnItemClickListener(itemClickListener);
    }

    ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clickedItemIdx = position;
            showPickActionDialog();
        }
    };


    private void showPickActionDialog() {
        PickActionDialogFragment fragment = PickActionDialogFragment.newInstance();
       fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), "dialog_pick_action");
    }

    public void onActionSelected(int action) {
        if (action == PickActionDialogFragment.ACTION_TIMER) {
            showTimerDialog();
        } else if (action == PickActionDialogFragment.ACTION_PROGRESS) {
            showProgressDialog();
        }
    }

    private void showTimerDialog() {
        TimerDialogFragment fragment = TimerDialogFragment.newInstance();
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), "fragment_timer");
    }

    protected void onTimerDismissed(long duration, boolean soundOn, boolean vibrateOn) {
        alertUser(soundOn, vibrateOn);
        int progress = (int) (duration / MILLIS_IN_MIN);
        Task task = tasks.get(clickedItemIdx);
        int oldProgress = task.getProgress();
        task.addProgress(progress);

        updateListViewRow(task, oldProgress);
        updateTaskProgress(task);
        notifyChangeToSibling();

        Toast.makeText(context, "Progress Saved", Toast.LENGTH_LONG).show();
    }

    private void alertUser(boolean soundOn, boolean vibrateOn) {
        if (soundOn) {
            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.ringtone);
            mp.start();
        }

        if (vibrateOn) {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(new long[]{0, 1000, 1000, 1000}, 3);  // {delay, sound, delay, sound}
        }
    }

    private void notifyChangeToSibling() {
        HabitsFragment fragment = (HabitsFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":1");
        fragment.setListView();
    }

    private void showProgressDialog() {
        Task task = tasks.get(clickedItemIdx);
        String name = task.getName();
        int max = task.getDuration();
        int progress = task.getProgress();

        ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(name, max, progress);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), "fragment_progress");
    }

    protected void onProgressSet(int progress) {
        Task task = tasks.get(clickedItemIdx);
        int oldProgress = task.getProgress();
        task.setProgress(progress);

        updateListViewRow(task, oldProgress);
        updateTaskProgress(task);
        notifyChangeToSibling();

        Toast.makeText(context, "Progress Saved", Toast.LENGTH_LONG).show();
    }

    private void updateTaskProgress(Task task) {
        dbHelper.updateTaskProgress(task.getId(), task.getProgress());
    }

    private void updateListViewRow(Task task, int oldProgress) {
        ListView listView = (ListView) mainView.findViewById(R.id.listView_tasks);
        View v = getListViewItemByIndex(clickedItemIdx, listView);
        Utility.setTaskRowValues(v, task, oldProgress);
    }

    private View getListViewItemByIndex(int idx, ListView listView) {
        final int firstItemPosition = listView.getFirstVisiblePosition();
        final int lastItemPosition = firstItemPosition + listView.getChildCount() - 1;

        if (idx < firstItemPosition || idx > lastItemPosition) {
            return listView.getAdapter().getView(idx, null, listView);
        } else {
            final int childIdx = idx - firstItemPosition;
            return listView.getChildAt(childIdx);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        long currentTimeInMillis = System.currentTimeMillis();
        long currentDate = currentTimeInMillis / Utility.MILLIS_IN_DAY;
        long viewDate = dateOfTasks.getTimeInMillis() / Utility.MILLIS_IN_DAY;
        if (currentDate > viewDate) {
            dateOfTasks.setTimeInMillis(currentTimeInMillis);
            setViews();
            notifyChangeToSibling();
        }
    }
}
