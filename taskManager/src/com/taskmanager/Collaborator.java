package com.taskmanager;

import java.util.ArrayList;

public class Collaborator {

    private static int nextId = 1;

    private int id;
    private String name;
    private CollaboratorCategory category;
    private ArrayList<Subtask> assignedSubtasks;

    public Collaborator(String name, CollaboratorCategory category) {
        this.id = nextId++;
        this.name = name;
        this.category = category;
        this.assignedSubtasks = new ArrayList<Subtask>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public CollaboratorCategory getCategory() { return category; }
    public ArrayList<Subtask> getAssignedSubtasks() { return assignedSubtasks; }

    public void setName(String name) { this.name = name; }
    public void setCategory(CollaboratorCategory category) { this.category = category; }

    public void addSubtask(Subtask subtask) {
        assignedSubtasks.add(subtask);
    }

    public int getOpenTaskCount() {
        int count = 0;
        for (Subtask s : assignedSubtasks) {
            if (!s.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public boolean canAcceptTask() {
        return getOpenTaskCount() < category.getMaxOpenTasks();
    }

    public boolean isOverloaded() {
        return getOpenTaskCount() > category.getMaxOpenTasks();
    }

    public String toString() {
        return name + " [" + category + "] open tasks: "
                + getOpenTaskCount() + "/" + category.getMaxOpenTasks();
    }
}
