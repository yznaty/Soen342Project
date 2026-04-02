package com.taskmanager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {

    private ICalGateway icalGateway;
    private ArrayList<Task> tasks;
    private ArrayList<Project> projects;
    private ArrayList<Tag> tags;
    

    public TaskManager() {
        tasks = new ArrayList<Task>();
        projects = new ArrayList<Project>();
        tags = new ArrayList<Tag>();
        icalGateway = new ICalGateway();
    }

    public Task createTask(String title, String description, Priority priority, LocalDate dueDate) {
        if (dueDate == null && countOpenTasksWithoutDueDate() >= 50) {
            throw new IllegalStateException(
                "The number of open tasks without a due date cannot exceed 50."
            );
        }

        Task task = new Task(title, description, priority, dueDate);
        tasks.add(task);
        return task;
    }

    private int countOpenTasksWithoutDueDate() {
        int count = 0;
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.OPEN && t.getDueDate() == null) {
                count++;
            }
        }
        return count;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public Task createRecurringTask(String title, String description,
                                    Priority priority, RecurrencePattern pattern) {
        Task template = new Task(title, description, priority, null);
        template.setRecurrencePattern(pattern);
        tasks.add(template);

        // generate one task per occurrence date
        ArrayList<LocalDate> dates = pattern.generateOccurrences();
        for (LocalDate date : dates) {
            // skip if same title & date already exists
            if (findTaskByTitleAndDate(title, date) == null) {
                Task occurrence = new Task(title, description, priority, date);
                tasks.add(occurrence);
            }
        }

        return template;
    }

    public ArrayList<Task> getAllTasks() {
        return tasks;
    }

    // returns null if not found
    public Task findTaskById(int id) {
        for (Task t : tasks) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    // returns null if not found
    public Task findTaskByTitleAndDate(String title, LocalDate date) {
        for (Task t : tasks) {
            if (t.getTitle().equals(title) && t.getDueDate() != null
                    && t.getDueDate().equals(date)) {
                return t;
            }
        }
        return null;
    }

    public Project createProject(String name, String description) {
        // project names must be unique
        if (findProjectByName(name) != null) {
            System.out.println("Error: project '" + name + "' already exists.");
            return null;
        }
        Project project = new Project(name, description);
        projects.add(project);
        return project;
    }

    // finds project or creates it if it doesn't exist (used in CSV import)
    public Project getOrCreateProject(String name, String description) {
        Project existing = findProjectByName(name);
        if (existing != null) return existing;
        return createProject(name, description);
    }

    public ArrayList<Project> getAllProjects() {
        return projects;
    }

    // returns null if not found
    public Project findProjectByName(String name) {
        for (Project p : projects) {
            if (p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    public void assignTaskToProject(Task task, Project project) {
        // remove from old project first
        if (task.getProject() != null) {
            task.getProject().removeTask(task);
        }
        project.addTask(task);
    }

    public void removeTaskFromProject(Task task) {
        if (task.getProject() != null) {
            task.getProject().removeTask(task);
        }
    }

    public Collaborator getOrCreateCollaborator(String name, CollaboratorCategory category,
                                                Project project) {
        Collaborator existing = project.findCollaborator(name);
        if (existing != null) return existing;

        Collaborator c = new Collaborator(name, category);
        project.addCollaborator(c);
        return c;
    }

    // links a collaborator to a task by creating a subtask
    // throws exception if collaborator is at their limit
    public Subtask linkCollaboratorToTask(Task task, Collaborator collaborator) {
        if (!collaborator.canAcceptTask()) {
            throw new IllegalStateException(
                "Collaborator " + collaborator.getName() + " has reached their limit of "
                + collaborator.getCategory().getMaxOpenTasks() + " open tasks.");
        }
        Subtask subtask = task.addSubtask("[" + collaborator.getName() + "] " + task.getTitle());
        subtask.setCollaborator(collaborator);
        collaborator.addSubtask(subtask);
        return subtask;
    }

    public Tag getOrCreateTag(String name) {
        String lower = name.toLowerCase().trim();
        for (Tag t : tags) {
            if (t.getName().equals(lower)) return t;
        }
        Tag newTag = new Tag(lower);
        tags.add(newTag);
        return newTag;
    }

    public void addTagToTask(Task task, String tagName) {
        Tag tag = getOrCreateTag(tagName);
        task.addTag(tag);
    }

    // if no criteria set, returns all open tasks sorted by due date
    public ArrayList<Task> search(SearchCriteria criteria) {
        ArrayList<Task> results = new ArrayList<Task>();

        for (Task task : tasks) {

            // if no criteria at all, only show OPEN tasks
            boolean hasCriteria = criteria.keyword != null || criteria.status != null
                    || criteria.priority != null || criteria.startDate != null
                    || criteria.endDate != null || criteria.projectName != null
                    || criteria.tag != null || criteria.dayOfWeek != null;

            if (!hasCriteria && task.getStatus() != TaskStatus.OPEN) {
                continue;
            }

            if (criteria.keyword != null && !criteria.keyword.isEmpty()) {
                String kw = criteria.keyword.toLowerCase();
                boolean inTitle = task.getTitle().toLowerCase().contains(kw);
                boolean inDesc = task.getDescription() != null
                        && task.getDescription().toLowerCase().contains(kw);
                if (!inTitle && !inDesc) continue;
            }

            if (criteria.status != null && task.getStatus() != criteria.status) {
                continue;
            }

            if (criteria.priority != null && task.getPriority() != criteria.priority) {
                continue;
            }

            if (criteria.startDate != null) {
                if (task.getDueDate() == null) continue;
                if (task.getDueDate().isBefore(criteria.startDate)) continue;
            }
            if (criteria.endDate != null) {
                if (task.getDueDate() == null) continue;
                if (task.getDueDate().isAfter(criteria.endDate)) continue;
            }

            if (criteria.dayOfWeek != null) {
                if (task.getDueDate() == null) continue;
                String taskDay = task.getDueDate().getDayOfWeek().toString();
                if (!taskDay.equalsIgnoreCase(criteria.dayOfWeek)) continue;
            }

            if (criteria.projectName != null && !criteria.projectName.isEmpty()) {
                if (task.getProject() == null) continue;
                if (!task.getProject().getName().equalsIgnoreCase(criteria.projectName)) continue;
            }

            if (criteria.tag != null && !criteria.tag.isEmpty()) {
                boolean found = false;
                for (Tag t : task.getTags()) {
                    if (t.getName().equalsIgnoreCase(criteria.tag)) {
                        found = true;
                        break;
                    }
                }
                if (!found) continue;
            }

            results.add(task);
        }

        results.sort((a, b) -> {
            if (a.getDueDate() == null && b.getDueDate() == null) return 0;
            if (a.getDueDate() == null) return 1;
            if (b.getDueDate() == null) return -1;
            return a.getDueDate().compareTo(b.getDueDate());
        });

        return results;
    }
    public void exportTaskToICal(Task task, String filePath) throws IOException {
        icalGateway.exportTask(task, filePath);
}

    public void exportProjectToICal(Project project, String filePath) throws IOException {
        icalGateway.exportProject(project, filePath);
    }
    public void exportFilteredToICal(List<Task> tasks, String filePath) throws IOException {
        icalGateway.exportTasks(tasks, filePath);
}
    public ArrayList<Task> getOpenTasksDueThisWeek() {
    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
    LocalDate weekEnd   = today.with(java.time.DayOfWeek.SUNDAY);
    ArrayList<Task> result = new ArrayList<Task>();
    for (Task t : tasks) {
        if (t.getStatus() == TaskStatus.OPEN && t.getDueDate() != null
                && !t.getDueDate().isBefore(weekStart)
                && !t.getDueDate().isAfter(weekEnd)) {
            result.add(t);
        }
    }
    return result;
    }
}
