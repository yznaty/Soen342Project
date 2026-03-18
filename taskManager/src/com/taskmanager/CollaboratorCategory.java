package com.taskmanager;

public enum CollaboratorCategory {
    JUNIOR, INTERMEDIATE, SENIOR;

    public int getMaxOpenTasks() {
        if (this == JUNIOR) return 10;
        if (this == INTERMEDIATE) return 5;
        return 2; // SENIOR
    }
}
