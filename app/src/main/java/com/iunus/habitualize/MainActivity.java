package com.iunus.habitualize;


import android.content.Intent;

import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ListView;

import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_PICK_DATE = 0;
    public final static int REQUEST_ADD_HABIT = 1;

    public final static String filename = "meta.txt";
    public final static int BUFFER_SIZE = 100;

    final static String TAG_TASKS_FRAGMENT = "android:switcher:" + R.id.pager + ":0";
    final static String TAG_HABITS_FRAGMENT = "android:switcher:" + R.id.pager + ":1";

    TabsPagerAdapter mTabsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        setupTabView();

        generateWeeklyTasks();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }




    private void setupTabView() {
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    HabitsFragment fragment = (HabitsFragment) getSupportFragmentManager()
                            .findFragmentByTag(TAG_HABITS_FRAGMENT);
                    fragment.finishActionMode();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }



    /** Methods that generate tasks for the rest of the week **/

    private void generateWeeklyTasks() {
        Calendar c = Calendar.getInstance();
        long today = System.currentTimeMillis();
        
        c.set(Calendar.DAY_OF_WEEK, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);     // Set to first day, first hour of week
        long firstHourOfWeek = c.getTimeInMillis();

        File file = getFileStreamPath(filename);
        if (file.exists()) {
            long timeStamp = getTimeStampFromFile();

            if (timeStamp < firstHourOfWeek) {
                new DBHelper(this).incrementHabitDurations();
                addTasksForThisWeek();
                writeTimeStampToFile(today);
            }
        } else {
            writeTimeStampToFile(today);
        }
    }

    private void writeTimeStampToFile(long timeStamp) {
        try {
            FileOutputStream fileOut = openFileOutput(filename, MODE_PRIVATE);
            byte[] b = Long.toString(timeStamp).getBytes();
            fileOut.write(b);
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getTimeStampFromFile() {
        byte[] buf = new byte[BUFFER_SIZE];
        int readLength = 0;

        try {
            FileInputStream fileIn = openFileInput(filename);
            readLength = fileIn.read(buf, 0, BUFFER_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String s = new String(buf, 0, readLength);
        return Long.parseLong(s);
    }

    private void addTasksForThisWeek() {
        DBHelper dbHelper = new DBHelper(this);
        List<Habit> habits = dbHelper.getHabits();

        for (Habit h : habits) {
            List<Task> tasks = Utility.createTasksForThisWeek(h);
            Utility.insertTasksToDB(dbHelper, tasks, h.getId());
        }
    }



    /******* Appbar item handlers *********/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                AddHabit(null);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void AddHabit(View view) {
        Intent intent = new Intent(this, HabitFormActivity.class);
        intent.putExtra("requestCode", REQUEST_ADD_HABIT);
        startActivityForResult(intent, REQUEST_ADD_HABIT);
    }



    /************************ Method handling Activity Result *********************************/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_HABIT) {
            if (resultCode == RESULT_OK) {
                mTabsPagerAdapter.notifyDataSetChanged();

                Toast.makeText(this, "New Habit Added", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }



    /******** Misc *********/

    public void notifyDataSetChanged() {
        mTabsPagerAdapter.notifyDataSetChanged();
    }
}
