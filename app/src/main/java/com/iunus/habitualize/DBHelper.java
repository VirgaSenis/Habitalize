package com.iunus.habitualize;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.iunus.habitualize.DBContract.habitEntry;
import static com.iunus.habitualize.DBContract.taskEntry;

/**
 * Created by Iunus on 03/01/2017.
 */

 class DBHelper extends SQLiteOpenHelper {

     private static final int DB_VERSION = 1;
     private static final String DB_NAME = "WeeklyPlanner.db";

     DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.CREATE_HABITS_TABLE);
        db.execSQL(DBContract.CREATE_TASKS_TABLE);
        db.execSQL(DBContract.TRIGGER_UPDATE_PROGRESS_TOTAL);
        db.execSQL(DBContract.TRIGGER_DELETE_HABIT);
        db.execSQL(DBContract.CREATE_TASKS_VIEW);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }



    /******************* Insert Methods ****************/

     void insertTask(Task task, int habitId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
         values.put(taskEntry.COLUMN_DURATION, task.getDuration());
        values.put(taskEntry.COLUMN_PROGRESS, 0);
        values.put(taskEntry.COLUMN_DATE, task.getDate());
        values.put(taskEntry.COLUMN_HABIT_ID, habitId);

        long id = db.insert(taskEntry.TABLE_NAME, null, values);
        db.close();
    }

     void insertHabit(Habit habit) {
        ContentValues values = new ContentValues();
        values.put(habitEntry.COLUMN_NAME, habit.getName());
        values.put(habitEntry.COLUMN_DURATION, habit.getDuration());
        values.put(habitEntry.COLUMN_PROGRESS_TOTAL, 0);
        values.put(habitEntry.COLUMN_START_DATE, habit.getStartDate());
        values.put(habitEntry.COLUMN_WORKING_DAYS, habit.getWorkingDays());
         values.put(habitEntry.COLUMN_INCREMENT_TYPE, habit.getIncrementType());
         values.put(habitEntry.COLUMN_INCREMENT_VALUE, habit.getIncrementValue());

        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(habitEntry.TABLE_NAME, null, values);
        habit.setId((int) id);

        db.close();
    }



    /**********************/

    void incrementHabitDurations() {
        String stmt =
           "UPDATE " + habitEntry.TABLE_NAME +
                " SET " + habitEntry.COLUMN_DURATION + " = " +
                   "CASE " +
                    "WHEN " + habitEntry.COLUMN_INCREMENT_TYPE + " = '" + habitEntry.VALUE_MINUTES + "' " +
                        "THEN " + habitEntry.COLUMN_DURATION + " + " + habitEntry.COLUMN_INCREMENT_VALUE +
                   " ELSE " + habitEntry.COLUMN_DURATION + " + MAX(1, (" +
                    habitEntry.COLUMN_DURATION + " * " + habitEntry.COLUMN_INCREMENT_VALUE + " / 100)) " +
                "END WHERE " + habitEntry.COLUMN_INCREMENT_VALUE + " != 0";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(stmt);
        db.close();
    }

     void updateTaskProgress(int id, int progress) {
        ContentValues values = new ContentValues();
        values.put(taskEntry.COLUMN_PROGRESS, progress);

        String whereClause = taskEntry._ID + " = ?";
        String[] args = { Integer.toString(id) };

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(taskEntry.TABLE_NAME, values, whereClause, args);
        db.close();
    }


     void updateHabitName(int id, String name) {
        String tableName = habitEntry.TABLE_NAME;

        ContentValues values = new ContentValues();
        values.put(habitEntry.COLUMN_NAME, name);

        String whereClause = "_id = ?";
        String[] args = { Integer.toString(id) };

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(tableName, values, whereClause, args);
        db.close();
    }

     void updateHabitDuration(int id, int duration) {
        ContentValues values = new ContentValues();
        values.put(habitEntry.COLUMN_DURATION, duration);

        String whereClause = habitEntry._ID + " = ?";
        String[] args = { Integer.toString(id) };

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(habitEntry.TABLE_NAME, values, whereClause, args);
        db.close();
    }

     void updateHabitWorkingDays(int id, String workingDays) {
        ContentValues values = new ContentValues();
        values.put(habitEntry.COLUMN_WORKING_DAYS, workingDays);

        String whereClause = habitEntry._ID + " = ?";
        String[] args = { Integer.toString(id) };

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(habitEntry.TABLE_NAME, values, whereClause, args);
        db.close();
    }

     void deleteTask(int habitId, String date) {
        String tableName = taskEntry.TABLE_NAME;

        String whereClause = taskEntry.COLUMN_HABIT_ID + " = ? AND " + taskEntry.COLUMN_DATE + " = ?";
        String[] args = { Integer.toString(habitId), date };

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, whereClause, args);
        db.close();
    }

     void deleteHabit(int id) {
        String tableName = habitEntry.TABLE_NAME;

        String whereClause = habitEntry._ID + " = ?";
        String[] args = { Integer.toString(id) };

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, whereClause, args);
        db.close();
    }

    /*************************/

     List<Task> getTasksOfDate(String date) {
        String whereClause = taskEntry.COLUMN_DATE + " = ?";
        String[] args = { date };

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.query(taskEntry.VIEW_NAME, null, whereClause, args, null, null, null);

        List<Task> tasks = getTaskListFromCursor(result);

        result.close();
        db.close();

        return tasks;
    }

     List<Habit> getHabits() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.query(habitEntry.TABLE_NAME, null, null, null, null, null, null);

        List<Habit> taskEntry = new ArrayList<>();
        int idIdx = result.getColumnIndex(habitEntry._ID);
        int nameIdx = result.getColumnIndex(habitEntry.COLUMN_NAME);
        int durationIdx = result.getColumnIndex(habitEntry.COLUMN_DURATION);
        int progressIdx = result.getColumnIndex(habitEntry.COLUMN_PROGRESS_TOTAL);
        int startDateIdx = result.getColumnIndex(habitEntry.COLUMN_START_DATE);
         int workingDaysIdx = result.getColumnIndex(habitEntry.COLUMN_WORKING_DAYS);
         int incrementValueIdx = result.getColumnIndex(habitEntry.COLUMN_INCREMENT_VALUE);
         int incrementTypeIdx = result.getColumnIndex(habitEntry.COLUMN_INCREMENT_TYPE);

        while(result.moveToNext()) {
            int id = result.getInt(idIdx);
            String taskName = result.getString(nameIdx);
            int duration = result.getInt(durationIdx);
            int progress = result.getInt(progressIdx);
            String startDate = result.getString(startDateIdx);
            String workingDays = result.getString(workingDaysIdx);
            String incrementType = result.getString(incrementTypeIdx);
            int incrementValue = result.getInt(incrementValueIdx);

            taskEntry.add(new Habit(id, taskName, duration, progress, startDate,
                    workingDays, incrementType, incrementValue));
        }

        result.close();
        db.close();
        return taskEntry;
    }

     List<Task> getTasksOfHabit(int habitId) {
        String whereClause = taskEntry.COLUMN_HABIT_ID + " = ?";
        String[] args = { Integer.toString(habitId) };
        String orderClause = taskEntry.COLUMN_DATE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.query(taskEntry.VIEW_NAME, null, whereClause, args, null, null, orderClause);

        List<Task> tasks = getTaskListFromCursor(result);

        result.close();
        db.close();

        return tasks;
    }

    private List<Task> getTaskListFromCursor(Cursor result) {
        List<Task> tasks = new ArrayList<>();
        int idIdx = result.getColumnIndex(taskEntry._ID);
        int nameIdx = result.getColumnIndex(habitEntry.COLUMN_NAME);
        int dateIdx = result.getColumnIndex(taskEntry.COLUMN_DATE);
        int durationIdx = result.getColumnIndex(habitEntry.COLUMN_DURATION);
        int progressIdx = result.getColumnIndex(taskEntry.COLUMN_PROGRESS);

        while(result.moveToNext()) {
            int id = result.getInt(idIdx);
            String taskName = result.getString(nameIdx);
            String date = result.getString(dateIdx);
            int duration = result.getInt(durationIdx);
            int progress = result.getInt(progressIdx);

            tasks.add(new Task(id, taskName, duration, progress, date));
        }

        return tasks;
    }

    public void updateDurationOfThisWeekTasks(int habitId, int duration) {
        ContentValues values = new ContentValues();
        values.put(taskEntry.COLUMN_DURATION, duration);

        String date = Utility.calendarToString(Calendar.getInstance());
        String whereClause = taskEntry.COLUMN_HABIT_ID + " = ? AND " + taskEntry.COLUMN_DATE + " >= ?";
        String[] args = { Integer.toString(habitId), date };

        SQLiteDatabase db = getWritableDatabase();
        db.update(taskEntry.TABLE_NAME, values, whereClause, args);
        db.close();
    }

    public void updateIncrementType(int id, String incrementType) {
        ContentValues values = new ContentValues();
        values.put(habitEntry.COLUMN_INCREMENT_TYPE, incrementType);

        String whereClause = habitEntry._ID + " = ?";
        String[] args = { Integer.toString(id) };

        SQLiteDatabase db = getReadableDatabase();
        db.update(habitEntry.TABLE_NAME, values, whereClause, args);
        db.close();
    }

    public void updateIncrementValue(int id, int incrementValue) {
        ContentValues values = new ContentValues();
        values.put(habitEntry.COLUMN_INCREMENT_VALUE, incrementValue);

        String whereClause = habitEntry._ID + " = ?";
        String[] args = { Integer.toString(id) };

        SQLiteDatabase db = getReadableDatabase();
        db.update(habitEntry.TABLE_NAME, values, whereClause, args);
        db.close();
    }
}


