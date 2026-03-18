package com.taskmanager;

import java.time.LocalDate;

public class ActivityEntry {

    private LocalDate date;
    private String description;

    public ActivityEntry(String description) {
        this.date = LocalDate.now();
        this.description = description;
    }

    public LocalDate getTimestamp() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "[" + date + "] " + description;
    }
}
