package com.taskmanager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class ICalGateway {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public void exportTask(Task task, String filePath) throws IOException {
        ArrayList<Task> singleTask = new ArrayList<Task>();
        if (task != null) {
            singleTask.add(task);
        }
        writeCalendar(singleTask, filePath);
    }

    public void exportProject(Project project, String filePath) throws IOException {
        ArrayList<Task> projectTasks = new ArrayList<Task>();
        if (project != null) {
            projectTasks.addAll(project.getTasks());
        }
        writeCalendar(projectTasks, filePath);
    }

    public void exportTasks(List<Task> tasks, String filePath) throws IOException {
        writeCalendar(tasks, filePath);
    }

    private void writeCalendar(List<Task> tasks, String filePath) throws IOException {
        StringBuilder calendar = new StringBuilder();
        calendar.append("BEGIN:VCALENDAR\r\n");
        calendar.append("VERSION:2.0\r\n");
        calendar.append("PRODID:-//TaskManager//iCal Export//EN\r\n");
        calendar.append("CALSCALE:GREGORIAN\r\n");
        calendar.append("METHOD:PUBLISH\r\n");

        if (tasks != null) {
            for (Task task : tasks) {
                if (task == null || task.getDueDate() == null) {
                    continue;
                }
                appendEvent(calendar, task);
            }
        }

        calendar.append("END:VCALENDAR\r\n");
        Files.write(Path.of(filePath), calendar.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void appendEvent(StringBuilder calendar, Task task) {
        String dueDate = DATE_FORMAT.format(task.getDueDate());
        String nextDay = DATE_FORMAT.format(task.getDueDate().plusDays(1));
        String stamp = DATE_TIME_FORMAT.format(ZonedDateTime.now(ZoneOffset.UTC));

        calendar.append("BEGIN:VEVENT\r\n");
        calendar.append("UID:task-").append(task.getId()).append("@taskmanager\r\n");
        calendar.append("DTSTAMP:").append(stamp).append("\r\n");
        calendar.append("DTSTART;VALUE=DATE:").append(dueDate).append("\r\n");
        calendar.append("DTEND;VALUE=DATE:").append(nextDay).append("\r\n");
        calendar.append("SUMMARY:").append(escape(task.getTitle())).append("\r\n");
        calendar.append("DESCRIPTION:").append(escape(buildDescription(task))).append("\r\n");
        calendar.append("PRIORITY:").append(mapPriority(task.getPriority())).append("\r\n");
        calendar.append("X-TASK-STATUS:").append(escape(task.getStatus().toString())).append("\r\n");

        if (task.getProject() != null) {
            calendar.append("X-PROJECT-NAME:")
                    .append(escape(task.getProject().getName()))
                    .append("\r\n");
        }

        if (task.getStatus() == TaskStatus.CANCELLED) {
            calendar.append("STATUS:CANCELLED\r\n");
        } else {
            calendar.append("STATUS:CONFIRMED\r\n");
        }

        calendar.append("END:VEVENT\r\n");
    }

    private String buildDescription(Task task) {
        StringBuilder description = new StringBuilder();

        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            description.append(task.getDescription()).append("\n");
        }

        description.append("Status: ").append(task.getStatus()).append("\n");
        description.append("Priority: ").append(task.getPriority()).append("\n");
        description.append("Due Date: ").append(task.getDueDate()).append("\n");

        if (task.getProject() != null) {
            description.append("Project: ").append(task.getProject().getName()).append("\n");
        }

        if (!task.getSubtasks().isEmpty()) {
            description.append("Subtasks:\n");
            for (Subtask subtask : task.getSubtasks()) {
                description.append("- ").append(subtask.getTitle());
                if (subtask.isCompleted()) {
                    description.append(" [completed]");
                }
                if (subtask.getCollaborator() != null) {
                    description.append(" (assigned to: ")
                            .append(subtask.getCollaborator().getName())
                            .append(")");
                }
                description.append("\n");
            }
        }

        return description.toString().trim();
    }

    private int mapPriority(Priority priority) {
        if (priority == Priority.HIGH) {
            return 1;
        }
        if (priority == Priority.MEDIUM) {
            return 5;
        }
        return 9;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}
