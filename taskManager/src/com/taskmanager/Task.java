package com.taskmanager;

import java.time.LocalDate;
import java.util.ArrayList;

public class Task {

    private static int nextId = 1;

    private int id;
    private String title;
    private String description;
    private LocalDate creationDate;
    private Priority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private Project project;
    private ArrayList<Subtask> subtasks;
    private ArrayList<Tag> tags;
    private ArrayList<ActivityEntry> activityHistory;
    private RecurrencePattern recurrencePattern;

    public Task(String title, String description, Priority priority, LocalDate dueDate) {
        this.id = nextId++;
        this.title = title;
        this.description = description;
        this.creationDate = LocalDate.now();
        this.priority = priority;
        this.status = TaskStatus.OPEN;
        this.dueDate = dueDate;
        this.project = null;
        this.subtasks = new ArrayList<Subtask>();
        this.tags = new ArrayList<Tag>();
        this.activityHistory = new ArrayList<ActivityEntry>();
        this.recurrencePattern = null;

        activityHistory.add(new ActivityEntry("Task created: " + title));
    }

    // getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getCreationDate() { return creationDate; }
    public Priority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public Project getProject() { return project; }
    public ArrayList<Subtask> getSubtasks() { return subtasks; }
    public ArrayList<Tag> getTags() { return tags; }
    public ArrayList<ActivityEntry> getActivityHistory() { return activityHistory; }
    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }

    // setters
    public void setTitle(String title) {
        activityHistory.add(new ActivityEntry("Title changed to: " + title));
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
        activityHistory.add(new ActivityEntry("Description updated"));
    }

    public void setPriority(Priority priority) {
        activityHistory.add(new ActivityEntry("Priority changed to: " + priority));
        this.priority = priority;
    }

    public void setStatus(TaskStatus status) {
        activityHistory.add(new ActivityEntry("Status changed to: " + status));
        this.status = status;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        activityHistory.add(new ActivityEntry("Due date set to: " + dueDate));
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public Subtask addSubtask(String subtaskTitle) {
        if (subtasks.size() >= 20) {
        throw new IllegalStateException("A task cannot have more than 20 subtasks.");
        }
        Subtask subtask = new Subtask(subtaskTitle, this);
        subtasks.add(subtask);
        activityHistory.add(new ActivityEntry("Subtask added: " + subtaskTitle));
        return subtask;
    }

    public void addTag(Tag tag) {
        for (Tag t : tags) {
            if (t.equals(tag)) return;
        }
        tags.add(tag);
        activityHistory.add(new ActivityEntry("Tag added: " + tag.getName()));
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    // returns progress as percentage (0-100)
    public int getSubtaskProgress() {
        if (subtasks.size() == 0) return 0;
        int done = 0;
        for (Subtask s : subtasks) {
            if (s.isCompleted()) done++;
        }
        return (done * 100) / subtasks.size();
    }

    public String toString() {
        String due = dueDate != null ? " | due: " + dueDate : "";
        String proj = project != null ? " | project: " + project.getName() : "";
        return "[" + id + "] [" + status + "] " + title
                + " | " + priority + due + proj;
    }
}
