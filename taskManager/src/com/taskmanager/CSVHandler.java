package com.taskmanager;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class CSVHandler {

    // column order: TaskName, Description, Subtask, Status, Priority,
    //               DueDate, ProjectName, ProjectDescription, Collaborator, CollaboratorCategory
    private static final String HEADER =
            "TaskName,Description,Subtask,Status,Priority," +
            "DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory";

    public void exportToCSV(TaskManager manager, String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write(HEADER + "\n");

        for (Task task : manager.getAllTasks()) {
            if (task.getSubtasks().isEmpty()) {
                fw.write(buildRow(task, null) + "\n");
            } else {
                for (Subtask subtask : task.getSubtasks()) {
                    fw.write(buildRow(task, subtask) + "\n");
                }
            }
        }

        fw.close();
    }

    private String buildRow(Task task, Subtask subtask) {
        String taskName    = escape(task.getTitle());
        String desc        = escape(task.getDescription());
        String subtaskCol  = subtask != null ? escape(subtask.getTitle()) : "";
        String status      = task.getStatus().toString();
        String priority    = task.getPriority().toString();
        String dueDate     = task.getDueDate() != null ? task.getDueDate().toString() : "";
        String projName    = task.getProject() != null ? escape(task.getProject().getName()) : "";
        String projDesc    = task.getProject() != null ? escape(task.getProject().getDescription()) : "";
        String collabName  = "";
        String collabCat   = "";

        if (subtask != null && subtask.getCollaborator() != null) {
            collabName = escape(subtask.getCollaborator().getName());
            collabCat  = subtask.getCollaborator().getCategory().toString();
        }

        return taskName + "," + desc + "," + subtaskCol + "," + status + ","
                + priority + "," + dueDate + "," + projName + "," + projDesc + ","
                + collabName + "," + collabCat;
    }

    public int importFromCSV(String filename, TaskManager manager) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int count = 0;

        String line = reader.readLine(); // skip header
        if (line == null) {
            reader.close();
            return 0;
        }

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] fields = splitCSVLine(line);

            String[] f = new String[10];
            for (int i = 0; i < 10; i++) {
                f[i] = i < fields.length ? fields[i].trim() : "";
            }

            String taskName    = f[0];
            String description = f[1];
            String subtaskTitle= f[2];
            String statusStr   = f[3];
            String priorityStr = f[4];
            String dueDateStr  = f[5];
            String projectName = f[6];
            String projectDesc = f[7];
            String collabName  = f[8];
            String collabCatStr= f[9];

            if (taskName.isEmpty()) {
                System.out.println("Skipping row - TaskName is empty");
                continue;
            }

            Priority priority = Priority.MEDIUM;
            if (!priorityStr.isEmpty()) {
                try { priority = Priority.valueOf(priorityStr.toUpperCase()); }
                catch (Exception e) { /* keep default */ }
            }

            TaskStatus status = TaskStatus.OPEN;
            if (!statusStr.isEmpty()) {
                try { status = TaskStatus.valueOf(statusStr.toUpperCase()); }
                catch (Exception e) { /* keep default */ }
            }

            LocalDate dueDate = null;
            if (!dueDateStr.isEmpty()) {
                try { dueDate = LocalDate.parse(dueDateStr); }
                catch (Exception e) {
                    System.out.println("Warning: bad date '" + dueDateStr + "' - skipped");
                }
            }

            Task task = findExistingTask(manager, taskName, dueDate);
            if (task == null) {
                task = manager.createTask(taskName,
                        description.isEmpty() ? null : description, priority, dueDate);
                if (status != TaskStatus.OPEN) {
                    task.setStatus(status);
                }
            }

            Project project = null;
            if (!projectName.isEmpty()) {
                project = manager.getOrCreateProject(projectName,
                        projectDesc.isEmpty() ? null : projectDesc);
                if (task.getProject() == null) {
                    manager.assignTaskToProject(task, project);
                }
            }

            Collaborator collaborator = null;
            if (!collabName.isEmpty() && project != null) {
                CollaboratorCategory cat = CollaboratorCategory.JUNIOR;
                if (!collabCatStr.isEmpty()) {
                    try { cat = CollaboratorCategory.valueOf(collabCatStr.toUpperCase()); }
                    catch (Exception e) { /* keep default */ }
                }
                collaborator = manager.getOrCreateCollaborator(collabName, cat, project);
            }

            if (!subtaskTitle.isEmpty()) {
                boolean exists = false;
                for (Subtask s : task.getSubtasks()) {
                    if (s.getTitle().equals(subtaskTitle)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    Subtask s = task.addSubtask(subtaskTitle);
                    if (collaborator != null) {
                        s.setCollaborator(collaborator);
                        collaborator.addSubtask(s);
                    }
                }
            } else if (collaborator != null) {
                try {
                    manager.linkCollaboratorToTask(task, collaborator);
                } catch (IllegalStateException e) {
                    System.out.println("Warning: " + e.getMessage());
                }
            }

            count++;
        }

        reader.close();
        return count;
    }

    // finds a task with the same title and due date - returns null if not found
    private Task findExistingTask(TaskManager manager, String title, LocalDate dueDate) {
        for (Task t : manager.getAllTasks()) {
            if (t.getTitle().equals(title)) {
                if (dueDate == null && t.getDueDate() == null) return t;
                if (dueDate != null && dueDate.equals(t.getDueDate())) return t;
            }
        }
        return null;
    }

    // simple CSV line split - handles quoted fields
    private String[] splitCSVLine(String line) {
        ArrayList<String> fields = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

    // wraps value in quotes if it contains a comma
    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",")) return "\"" + value + "\"";
        return value;
    }
}
