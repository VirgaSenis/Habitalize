package com.iunus.habitualize;

import java.io.Serializable;

/**
 * Created by Iunus on 08/02/2017.
 */

class Habit implements Serializable {
    final static int ID_UNSET = -1;


    private int id;
    private String name;
    private int duration;
    private int progressTotal;
    private String startDate;
    private String workingDays;
    private int incrementValue;
    private String incrementType;

    public Habit(int id, String name, int duration, int progressTotal, String startDate,
                 String workingDays, String incrementType, int incrementValue) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.progressTotal = progressTotal;
        this.startDate = startDate;
        this.workingDays = workingDays;
        this.incrementType = incrementType;
        this.incrementValue = incrementValue;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }


    public String getStartDate() {
        return startDate;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumberOfWorkingDays() {
        return workingDays.length();
    }

    public int getId() {
        return id;
    }

    public int[] getWorkingDaysComparisonArray(String newWorkingDays) {
        int[] arr = new int[7];
        String oldWorkingDays = getWorkingDays();

        for (int i=0; i<oldWorkingDays.length(); i++) {
            int j = oldWorkingDays.charAt(i) - '0';
            arr[j]--;
        }

        for (int i=0; i<newWorkingDays.length(); i++) {
            int j = newWorkingDays.charAt(i) - '0';
            arr[j]++;
        }

        return arr;
    }

    public int getProgressTotal() {
        return progressTotal;
    }

    public String getIncrementType() {
        return incrementType;
    }

    public int getIncrementValue() {
        return incrementValue;
    }
}
