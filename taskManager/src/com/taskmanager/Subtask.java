package com.taskmanager;

public class Subtask {

    private static int nextId = 1;

    private int id;
    private String title;
    private boolean completed;
    private Task parentTask;
    private Collaborator collaborator; // null if not linked to a collaborator

    public Subtask(String title, Task parentTask) {
        this.id = nextId++;
        this.title = title;
        this.parentTask = parentTask;
        this.completed = false;
        this.collaborator = null;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public Task getParentTask() { return parentTask; }
    public Collaborator getCollaborator() { return collaborator; }

    public void setTitle(String title) { this.title = title; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCollaborator(Collaborator collaborator) { this.collaborator = collaborator; }

    public String toString() {
        String check = completed ? "[x]" : "[ ]";
        String collab = "";
        if (collaborator != null) {
            collab = " (assigned to: " + collaborator.getName() + ")";
        }
        return check + " " + title + collab;
    }
}
