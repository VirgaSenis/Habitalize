package com.iunus.habitualize;

import java.io.Serializable;

/**
 * Created by Iunus on 02/01/2017.
 */

public class Task implements Serializable {


    public final static int ID_UNSET = -1;

    private int id;
    private String name;
    private int duration;
    private int progress;
    private String date;

    public Task(int id, String name, int duration, int progress, String date) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.progress = progress;
        this.date = date;
    }

    public int getId() { return id; }

    public void setId(int id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void addProgress(int progress) {
        this.progress += progress;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }
}
