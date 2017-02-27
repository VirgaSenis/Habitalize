package com.iunus.habitualize;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Iunus on 25/01/2017.
 */

 final class Utility {

    static final int VIEW_TEXT_EMPTY = -1;
    static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

    static int getLastDayOfMonth(int month) {   // January == 0; February == 1
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, month + 1);
        c.set(Calendar.DAY_OF_MONTH, 0);    // 0 sets Calendar to last day of previous month
        return c.get(Calendar.DAY_OF_MONTH);
    }

     static String calendarToString(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
    }

    static Calendar toCalendar(String date)  {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cal;
    }

     static String calendarToDisplayableString(Calendar c1) {
        Calendar c2 = Calendar.getInstance();

         switch (compareDate(c1, c2)) {
             case 0 :
                 return "Today";
             case 1 :
                 return "Tomorrow";
             case -1 :
                 return "Yesterday";
             default :
                 return new SimpleDateFormat("EEEE, yyyy-MM-dd", Locale.ENGLISH).format(c1.getTime());
         }
    }

    // Returns number of differing days. (i.e. if return value is -1, then c1 is a day ahead of c2)
     static int compareDate(Calendar c1, Calendar c2) {
         return (int) ((c1.getTimeInMillis() / MILLIS_IN_DAY) - (c2.getTimeInMillis() / MILLIS_IN_DAY));
    }

     static void setTaskRowValues(View view, Task task, int oldProgress) {
        TextView nameView = (TextView) view.findViewById(R.id.view_name);
        nameView.setText(task.getName());

        TextView numericProgressView = (TextView) view.findViewById(R.id.view_numeric_progress);
        String progressString = String.format(Locale.ENGLISH, "%d/%d %s",
                task.getProgress(), task.getDuration(), view.getContext().getString(R.string.measure_common));
        numericProgressView.setText(progressString);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setMax(task.getDuration());
        Utility.animateProgressBar(progressBar, oldProgress, task.getProgress());

        int color;
        if (task.getDuration() <= task.getProgress()) {
            color = Color.parseColor("#21B510");
        } else {
            color = Color.parseColor("#C11515");
        }

        Drawable background = new ColorDrawable(Color.parseColor("#CEB7B7"));
        Drawable secondaryProgress = new ColorDrawable(Color.TRANSPARENT);
        Drawable progress = new ScaleDrawable(new ColorDrawable(color), Gravity.LEFT, 1, -1);
        LayerDrawable result = new LayerDrawable(new Drawable[] { background, secondaryProgress, progress });

        result.setId(0, android.R.id.background);
        result.setId(1, android.R.id.secondaryProgress);
        result.setId(2, android.R.id.progress);

        progressBar.setProgressDrawable(result);
    }


    private static void animateProgressBar(ProgressBar bar, int start, int end) {
        final int MAGNITUDE = 500;

        int max = bar.getMax();
        int progress = bar.getProgress();
        bar.setMax(max * MAGNITUDE);
        bar.setProgress(progress * MAGNITUDE);
        ProgressBarAnimation animation = new ProgressBarAnimation(bar, start * MAGNITUDE, end * MAGNITUDE);
        animation.setDuration(1000);
        bar.startAnimation(animation);
    }

    static void hideSoftKeyboard(Activity activity, View v) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    static boolean isStringEmpty(String s) {
        return s.trim().length() == 0;
    }

    static int editTextToInt(EditText et) {
        String s = et.getText().toString();
        return (isStringEmpty(s)) ? VIEW_TEXT_EMPTY : Integer.parseInt(s);
    }

    static int textViewToInt(TextView tv) {
        String s = tv.getText().toString();
        return (isStringEmpty(s)) ? VIEW_TEXT_EMPTY : Integer.parseInt(s);
    }

    static void setToPreviousDate(Calendar c) {
        c.add(Calendar.DAY_OF_MONTH, -1);

    }

    static void setToNextDate(Calendar c) {
        c.add(Calendar.DAY_OF_MONTH, 1);
    }


    static List<Task> createTasksForThisWeek(Habit habit) {
        List<Task> tasks = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        long today = cal.getTimeInMillis();
        
        cal.set(Calendar.DAY_OF_WEEK, 7);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        long lastMinuteOfWeek = cal.getTimeInMillis();

        String workingDays = habit.getWorkingDays();

        for (long i=today; i<=lastMinuteOfWeek; i+=MILLIS_IN_DAY) {
            cal.setTimeInMillis(i);
            // Calendar starts with 1, but workingDays starts with 0; so subtract 1 from cal's DOW
            String dayOfWeek = Integer.toString( cal.get(Calendar.DAY_OF_WEEK)-1 );

            if (workingDays.contains(dayOfWeek)) {
                String date = calendarToString(cal);

                tasks.add(createTask(habit, date));
            }
        }

        return tasks;
    }

    static Task createTask(Habit habit, String date) {
        String name = habit.getName();
        int duration = habit.getDuration();

        return new Task(Task.ID_UNSET, name, duration, 0, date);
    }

    static void insertTasksToDB(DBHelper helper, List<Task> tasks, int habitId) {
        for (Task t : tasks) {
            helper.insertTask(t, habitId);
        }
    }
}
