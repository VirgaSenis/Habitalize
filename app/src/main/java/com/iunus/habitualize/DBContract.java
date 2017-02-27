package com.iunus.habitualize;

import android.provider.BaseColumns;

/**
 * Created by Iunus on 02/01/2017.
 */

 class DBContract {
    private DBContract() {}

    static class habitEntry implements BaseColumns {
         static final String TABLE_NAME = "habits";
        
         static final String COLUMN_NAME = "name";
         static final String COLUMN_DURATION = "duration";
         static final String COLUMN_PROGRESS_TOTAL = "progress_total";
         static final String COLUMN_START_DATE = "start_date";
         static final String COLUMN_WORKING_DAYS = "working_days";
         static final String COLUMN_INCREMENT_VALUE = "increment_value";
        static final String COLUMN_INCREMENT_TYPE = "increment_type";

        static final String VALUE_PERCENT = "percent";
        static final String VALUE_MINUTES = "minutes";
    }

     static class taskEntry implements BaseColumns {
         static final String TABLE_NAME = "tasks";
         static final String VIEW_NAME = "tasks_view";

         static final String COLUMN_DURATION = "duration";
         static final String COLUMN_PROGRESS = "progress";
         static final String COLUMN_DATE = "date";
         static final String COLUMN_HABIT_ID = "habit_id";
    }


     static final String CREATE_HABITS_TABLE =
            "CREATE TABLE " + habitEntry.TABLE_NAME + " (" +
                    habitEntry._ID + " INTEGER PRIMARY KEY, " +
                    habitEntry.COLUMN_NAME + " TEXT, " +
                    habitEntry.COLUMN_DURATION + " INTEGER, " +
                    habitEntry.COLUMN_PROGRESS_TOTAL + " INTEGER, " +
                    habitEntry.COLUMN_START_DATE + " TEXT, " +
                    habitEntry.COLUMN_WORKING_DAYS + " TEXT, " +
                    habitEntry.COLUMN_INCREMENT_TYPE + " TEXT, " +
                    habitEntry.COLUMN_INCREMENT_VALUE + " INTEGER)";

     static final String CREATE_TASKS_TABLE =
            "CREATE TABLE " + taskEntry.TABLE_NAME + " (" +
                    taskEntry._ID + " INTEGER PRIMARY KEY, " +
                    taskEntry.COLUMN_DATE + " TEXT, " +
                    taskEntry.COLUMN_DURATION + " INTEGER, " +
                    taskEntry.COLUMN_PROGRESS + " INTEGER, " +
                    taskEntry.COLUMN_HABIT_ID + " INTEGER NOT NULL REFERENCES " +
                        habitEntry.TABLE_NAME + "(" + habitEntry._ID + "))";

     static final String TRIGGER_UPDATE_PROGRESS_TOTAL =
            "CREATE TRIGGER update_CUMULATIVE_progress_trigger AFTER UPDATE ON " + taskEntry.TABLE_NAME +
                    " BEGIN " +
                        "UPDATE " + habitEntry.TABLE_NAME +
                            " SET " + habitEntry.COLUMN_PROGRESS_TOTAL + " = " +
                                habitEntry.COLUMN_PROGRESS_TOTAL +
                                " - old." + taskEntry.COLUMN_PROGRESS + " + new." + taskEntry.COLUMN_PROGRESS +
                            " WHERE " + habitEntry._ID + " = " + "old." + taskEntry.COLUMN_HABIT_ID +
                    "; END";

     static final String TRIGGER_DELETE_HABIT =
            "CREATE TRIGGER delete_task_trigger AFTER DELETE ON " + habitEntry.TABLE_NAME +
                    " BEGIN " +
                        "DELETE FROM " + taskEntry.TABLE_NAME +
                        " WHERE " + taskEntry.COLUMN_HABIT_ID + " = old." + habitEntry._ID +
                    "; END";

     static final String CREATE_TASKS_VIEW =
            "CREATE VIEW " + taskEntry.VIEW_NAME + " AS " +
            "SELECT t." + taskEntry._ID + " AS " + taskEntry._ID + ", " +
                    "h." + habitEntry.COLUMN_NAME + " AS " + habitEntry.COLUMN_NAME + ", " +
                    "t." + habitEntry.COLUMN_DURATION + " AS " + habitEntry.COLUMN_DURATION + ", " +
                    "t." + taskEntry.COLUMN_PROGRESS + " AS " + taskEntry.COLUMN_PROGRESS + ", " +
                    "t." + taskEntry.COLUMN_DATE + " AS " + taskEntry.COLUMN_DATE + ", " +
                    "t." + taskEntry.COLUMN_HABIT_ID + " AS " + taskEntry.COLUMN_HABIT_ID +
            " FROM " + taskEntry.TABLE_NAME + " t JOIN " + habitEntry.TABLE_NAME + " h " +
                        "ON h." + taskEntry._ID + " = t." + taskEntry.COLUMN_HABIT_ID;

}
