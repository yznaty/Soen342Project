package com.taskmanager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static TaskManager manager = new TaskManager();
    static CSVHandler csvHandler = new CSVHandler();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Personal Task Management System ===");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                createTask();
            } else if (choice.equals("2")) {
                searchTasks();
            } else if (choice.equals("3")) {
                viewTaskDetails();
            } else if (choice.equals("4")) {
                updateTask();
            } else if (choice.equals("5")) {
                createProject();
            } else if (choice.equals("6")) {
                assignTaskToProject();
            } else if (choice.equals("7")) {
                addCollaborator();
            } else if (choice.equals("8")) {
                linkCollaboratorToTask();
            } else if (choice.equals("9")) {
                importCSV();
            } else if (choice.equals("10")) {
                exportCSV();
            } else if (choice.equals("11")) {
                viewAllProjects();
            } else if (choice.equals("12")) {
                viewActivityHistory();
            } else if (choice.equals("0")) {
                running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }

        System.out.println("Goodbye!");
    }

    static void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1.  Create Task");
        System.out.println("2.  Search Tasks");
        System.out.println("3.  View Task Details");
        System.out.println("4.  Update Task");
        System.out.println("5.  Create Project");
        System.out.println("6.  Assign Task to Project");
        System.out.println("7.  Add Collaborator to Project");
        System.out.println("8.  Link Collaborator to Task");
        System.out.println("9.  Import from CSV");
        System.out.println("10. Export to CSV");
        System.out.println("11. View All Projects");
        System.out.println("12. View Task Activity History");
        System.out.println("0.  Exit");
        System.out.print("Choice: ");
    }

    // -------------------------------------------------------------------------

    static void createTask() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        System.out.print("Description (leave blank to skip): ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) description = null;

        Priority priority = readPriority();

        System.out.print("Due date (yyyy-MM-dd, leave blank to skip): ");
        LocalDate dueDate = readDate();

        System.out.print("Tags (comma separated, leave blank to skip): ");
        String tagsInput = scanner.nextLine().trim();

        System.out.print("Is this a recurring task? (y/n): ");
        String recurring = scanner.nextLine().trim();

        Task task;
        if (recurring.equalsIgnoreCase("y")) {
            RecurrencePattern pattern = buildRecurrencePattern();
            if (pattern == null) return;
            task = manager.createRecurringTask(title, description, priority, pattern);
            System.out.println("Recurring task created with occurrences.");
        } else {
            task = manager.createTask(title, description, priority, dueDate);
        }

        // add tags
        if (!tagsInput.isEmpty()) {
            String[] tagParts = tagsInput.split(",");
            for (String t : tagParts) {
                manager.addTagToTask(task, t.trim());
            }
        }

        System.out.println("Task created: " + task);
    }

    static RecurrencePattern buildRecurrencePattern() {
        System.out.println("Recurrence types: DAILY, WEEKLY, MONTHLY, CUSTOM");
        System.out.print("Type: ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        RecurrenceType type;
        try {
            type = RecurrenceType.valueOf(typeStr);
        } catch (Exception e) {
            System.out.println("Invalid type.");
            return null;
        }

        System.out.print("Start date (yyyy-MM-dd): ");
        LocalDate start = readDate();
        System.out.print("End date (yyyy-MM-dd): ");
        LocalDate end = readDate();

        if (start == null || end == null) {
            System.out.println("Start and end dates are required.");
            return null;
        }

        RecurrencePattern pattern = new RecurrencePattern(type, start, end);

        if (type == RecurrenceType.WEEKLY) {
            System.out.print("Days of week (e.g. MONDAY,WEDNESDAY): ");
            String daysInput = scanner.nextLine().trim();
            ArrayList<String> days = new ArrayList<String>();
            for (String d : daysInput.split(",")) {
                days.add(d.trim().toUpperCase());
            }
            pattern.setWeekdays(days);
        } else if (type == RecurrenceType.MONTHLY) {
            System.out.print("Day of month (1-31): ");
            int dom = readInt(1);
            pattern.setDayOfMonth(dom);
        } else if (type == RecurrenceType.CUSTOM) {
            System.out.print("Interval in days: ");
            int interval = readInt(1);
            pattern.setInterval(interval);
        }

        return pattern;
    }

    // -------------------------------------------------------------------------

    static void searchTasks() {
        System.out.println("\n-- Search Tasks (leave blank to skip any field) --");
        SearchCriteria criteria = new SearchCriteria();

        System.out.print("Keyword (title/description): ");
        String kw = scanner.nextLine().trim();
        if (!kw.isEmpty()) criteria.keyword = kw;

        System.out.print("Status (OPEN/COMPLETED/CANCELLED): ");
        String statusStr = scanner.nextLine().trim();
        if (!statusStr.isEmpty()) {
            try { criteria.status = TaskStatus.valueOf(statusStr.toUpperCase()); }
            catch (Exception e) { System.out.println("Invalid status, ignoring."); }
        }

        System.out.print("Priority (LOW/MEDIUM/HIGH): ");
        String priStr = scanner.nextLine().trim();
        if (!priStr.isEmpty()) {
            try { criteria.priority = Priority.valueOf(priStr.toUpperCase()); }
            catch (Exception e) { System.out.println("Invalid priority, ignoring."); }
        }

        System.out.print("Due date from (yyyy-MM-dd): ");
        criteria.startDate = readDate();

        System.out.print("Due date to (yyyy-MM-dd): ");
        criteria.endDate = readDate();

        System.out.print("Day of week (e.g. MONDAY): ");
        String dow = scanner.nextLine().trim();
        if (!dow.isEmpty()) criteria.dayOfWeek = dow.toUpperCase();

        System.out.print("Project name: ");
        String proj = scanner.nextLine().trim();
        if (!proj.isEmpty()) criteria.projectName = proj;

        System.out.print("Tag: ");
        String tag = scanner.nextLine().trim();
        if (!tag.isEmpty()) criteria.tag = tag;

        ArrayList<Task> results = manager.search(criteria);
        System.out.println("\nFound " + results.size() + " task(s):");
        for (Task t : results) {
            System.out.println("  " + t);
        }
    }

    // -------------------------------------------------------------------------

    static void viewTaskDetails() {
        Task task = pickTask();
        if (task == null) return;

        System.out.println("\n-- Task Details --");
        System.out.println("ID:          " + task.getId());
        System.out.println("Title:       " + task.getTitle());
        System.out.println("Description: " + (task.getDescription() != null ? task.getDescription() : "-"));
        System.out.println("Status:      " + task.getStatus());
        System.out.println("Priority:    " + task.getPriority());
        System.out.println("Created:     " + task.getCreationDate());
        System.out.println("Due Date:    " + (task.getDueDate() != null ? task.getDueDate() : "-"));
        System.out.println("Project:     " + (task.getProject() != null ? task.getProject().getName() : "-"));
        System.out.println("Recurring:   " + (task.getRecurrencePattern() != null ? task.getRecurrencePattern() : "No"));

        if (!task.getTags().isEmpty()) {
            System.out.print("Tags:        ");
            for (Tag t : task.getTags()) {
                System.out.print(t.getName() + " ");
            }
            System.out.println();
        }

        if (!task.getSubtasks().isEmpty()) {
            System.out.println("Subtasks (" + task.getSubtaskProgress() + "% done):");
            for (Subtask s : task.getSubtasks()) {
                System.out.println("  " + s);
            }
        }
    }

    // -------------------------------------------------------------------------

    static void updateTask() {
        Task task = pickTask();
        if (task == null) return;

        System.out.println("What to update?");
        System.out.println("1. Title");
        System.out.println("2. Description");
        System.out.println("3. Priority");
        System.out.println("4. Due Date");
        System.out.println("5. Status");
        System.out.println("6. Add Tag");
        System.out.println("7. Add Subtask");
        System.out.println("8. Complete a Subtask");
        System.out.println("9. Remove from Project");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("New title: ");
            String title = scanner.nextLine().trim();
            if (!title.isEmpty()) task.setTitle(title);

        } else if (choice.equals("2")) {
            System.out.print("New description: ");
            task.setDescription(scanner.nextLine().trim());

        } else if (choice.equals("3")) {
            task.setPriority(readPriority());

        } else if (choice.equals("4")) {
            System.out.print("New due date (yyyy-MM-dd): ");
            task.setDueDate(readDate());

        } else if (choice.equals("5")) {
            System.out.print("New status (OPEN/COMPLETED/CANCELLED): ");
            String s = scanner.nextLine().trim();
            try {
                task.setStatus(TaskStatus.valueOf(s.toUpperCase()));
                // check if any collaborators are now overloaded
                for (Subtask sub : task.getSubtasks()) {
                    if (sub.getCollaborator() != null && sub.getCollaborator().isOverloaded()) {
                        System.out.println("Warning: " + sub.getCollaborator().getName()
                                + " is now overloaded!");
                    }
                }
            } catch (Exception e) {
                System.out.println("Invalid status.");
            }

        } else if (choice.equals("6")) {
            System.out.print("Tag name: ");
            manager.addTagToTask(task, scanner.nextLine().trim());

        } else if (choice.equals("7")) {
            System.out.print("Subtask title: ");
            String st = scanner.nextLine().trim();
            if (!st.isEmpty()) task.addSubtask(st);

        } else if (choice.equals("8")) {
            ArrayList<Subtask> open = new ArrayList<Subtask>();
            for (Subtask s : task.getSubtasks()) {
                if (!s.isCompleted()) open.add(s);
            }
            if (open.isEmpty()) {
                System.out.println("No open subtasks.");
                return;
            }
            for (int i = 0; i < open.size(); i++) {
                System.out.println((i + 1) + ". " + open.get(i).getTitle());
            }
            System.out.print("Select: ");
            int idx = readInt(0) - 1;
            if (idx >= 0 && idx < open.size()) {
                open.get(idx).setCompleted(true);
                System.out.println("Subtask completed. Progress: " + task.getSubtaskProgress() + "%");
            }

        } else if (choice.equals("9")) {
            manager.removeTaskFromProject(task);
            System.out.println("Removed from project.");
        }

        System.out.println("Done.");
    }

    // -------------------------------------------------------------------------

    static void createProject() {
        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name required."); return; }

        System.out.print("Description (optional): ");
        String desc = scanner.nextLine().trim();

        Project p = manager.createProject(name, desc.isEmpty() ? null : desc);
        if (p != null) System.out.println("Project created: " + p);
    }

    static void assignTaskToProject() {
        Task task = pickTask();
        if (task == null) return;

        Project project = pickProject();
        if (project == null) return;

        manager.assignTaskToProject(task, project);
        System.out.println("Task assigned to project.");
    }

    static void addCollaborator() {
        Project project = pickProject();
        if (project == null) return;

        System.out.print("Collaborator name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name required."); return; }

        System.out.print("Category (JUNIOR/INTERMEDIATE/SENIOR): ");
        String catStr = scanner.nextLine().trim();
        CollaboratorCategory cat;
        try {
            cat = CollaboratorCategory.valueOf(catStr.toUpperCase());
        } catch (Exception e) {
            System.out.println("Invalid category.");
            return;
        }

        Collaborator c = manager.getOrCreateCollaborator(name, cat, project);
        System.out.println("Collaborator added: " + c);
    }

    static void linkCollaboratorToTask() {
        Project project = pickProject();
        if (project == null) return;

        if (project.getCollaborators().isEmpty()) {
            System.out.println("No collaborators in this project.");
            return;
        }

        System.out.println("Collaborators:");
        ArrayList<Collaborator> collabs = project.getCollaborators();
        for (int i = 0; i < collabs.size(); i++) {
            System.out.println((i + 1) + ". " + collabs.get(i));
        }
        System.out.print("Select collaborator: ");
        int ci = readInt(0) - 1;
        if (ci < 0 || ci >= collabs.size()) { System.out.println("Invalid."); return; }

        Task task = pickTask();
        if (task == null) return;

        try {
            Subtask st = manager.linkCollaboratorToTask(task, collabs.get(ci));
            System.out.println("Subtask created: " + st);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------

    static void importCSV() {
        System.out.print("File path: ");
        String path = scanner.nextLine().trim();
        try {
            int count = csvHandler.importFromCSV(path, manager);
            System.out.println("Imported " + count + " rows.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void exportCSV() {
        System.out.print("Output file [tasks_export.csv]: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "tasks_export.csv";
        try {
            csvHandler.exportToCSV(manager, path);
            System.out.println("Exported " + manager.getAllTasks().size() + " tasks to " + path);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewAllProjects() {
        ArrayList<Project> projects = manager.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects.");
            return;
        }
        System.out.println("\n-- All Projects --");
        for (Project p : projects) {
            System.out.println(p);
            for (Collaborator c : p.getCollaborators()) {
                System.out.println("   " + c);
            }
        }
    }

    static void viewActivityHistory() {
        Task task = pickTask();
        if (task == null) return;

        System.out.println("\n-- Activity History: " + task.getTitle() + " --");
        ArrayList<ActivityEntry> history = task.getActivityHistory();
        if (history.isEmpty()) {
            System.out.println("No activity yet.");
        } else {
            for (ActivityEntry e : history) {
                System.out.println("  " + e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    static Task pickTask() {
        ArrayList<Task> all = manager.getAllTasks();
        if (all.isEmpty()) {
            System.out.println("No tasks in the system.");
            return null;
        }
        System.out.println("Tasks:");
        for (int i = 0; i < all.size(); i++) {
            System.out.println((i + 1) + ". " + all.get(i));
        }
        System.out.print("Select task number: ");
        int idx = readInt(0) - 1;
        if (idx < 0 || idx >= all.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        return all.get(idx);
    }

    static Project pickProject() {
        ArrayList<Project> all = manager.getAllProjects();
        if (all.isEmpty()) {
            System.out.println("No projects in the system.");
            return null;
        }
        System.out.println("Projects:");
        for (int i = 0; i < all.size(); i++) {
            System.out.println((i + 1) + ". " + all.get(i).getName());
        }
        System.out.print("Select project: ");
        int idx = readInt(0) - 1;
        if (idx < 0 || idx >= all.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        return all.get(idx);
    }

    static Priority readPriority() {
        System.out.print("Priority (LOW/MEDIUM/HIGH): ");
        String s = scanner.nextLine().trim().toUpperCase();
        try {
            return s.isEmpty() ? Priority.MEDIUM : Priority.valueOf(s);
        } catch (Exception e) {
            System.out.println("Invalid, defaulting to MEDIUM.");
            return Priority.MEDIUM;
        }
    }

    static LocalDate readDate() {
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            System.out.println("Invalid date, skipping.");
            return null;
        }
    }

    static int readInt(int defaultVal) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
