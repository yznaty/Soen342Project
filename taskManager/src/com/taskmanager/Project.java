package com.taskmanager;

import java.util.ArrayList;

public class Project {

    private static int nextId = 1;

    private int id;
    private String name;
    private String description;
    private ArrayList<Task> tasks;
    private ArrayList<Collaborator> collaborators;

    public Project(String name, String description) {
        this.id = nextId++;
        this.name = name;
        this.description = description;
        this.tasks = new ArrayList<Task>();
        this.collaborators = new ArrayList<Collaborator>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ArrayList<Task> getTasks() { return tasks; }
    public ArrayList<Collaborator> getCollaborators() { return collaborators; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }

    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }

    public void addCollaborator(Collaborator collaborator) {
        collaborators.add(collaborator);
    }

    // returns null if not found
    public Collaborator findCollaborator(String name) {
        for (Collaborator c : collaborators) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public String toString() {
        return "[" + id + "] " + name + " (" + tasks.size() + " tasks)";
    }
}
