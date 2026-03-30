package com.taskmanager;

import java.util.ArrayList;

public class OCLConstraints {

    public static void checkMaxSubtasks(Task task) {
        if (task.getSubtasks().size() > 20) {
            throw new IllegalStateException("A task cannot have more than 20 subtasks.");
        }
    }

    public static void checkMaxOpenTasksWithoutDueDate(TaskManager manager) {
        int count = 0;
        for (Task t : manager.getAllTasks()) {
            if (t.getStatus() == TaskStatus.OPEN && t.getDueDate() == null) {
                count++;
            }
        }
        if (count > 50) {
            throw new IllegalStateException(
                "The number of open tasks without a due date cannot exceed 50."
            );
        }
    }

    public static void checkPositiveLimit(CollaboratorCategory category) {
        if (category.getMaxOpenTasks() <= 0) {
            throw new IllegalStateException("Collaborator category limit must be positive.");
        }
    }

    public static void checkNotOverloaded(Collaborator collaborator) {
        if (collaborator.getOpenTaskCount() > collaborator.getCategory().getMaxOpenTasks()) {
            throw new IllegalStateException(
                "Collaborator " + collaborator.getName() + " is overloaded."
            );
        }
    }

    public static ArrayList<Collaborator> findOverloadedCollaborators(TaskManager manager) {
        ArrayList<Collaborator> overloaded = new ArrayList<Collaborator>();

        for (Project project : manager.getAllProjects()) {
            for (Collaborator c : project.getCollaborators()) {
                if (c.getOpenTaskCount() >= c.getCategory().getMaxOpenTasks() && !overloaded.contains(c)) {
                    overloaded.add(c);
                }
            }
        }
        return overloaded;
    }
}