package com.taskmanager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SQLitePersistenceService implements PersistenceService {

    private final String dbUrl;

    public SQLitePersistenceService(String dbFileName) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found.", e);
        }

        this.dbUrl = "jdbc:sqlite:" + dbFileName;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS projects (" +
            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " name TEXT NOT NULL UNIQUE," +
            " description TEXT" +
            ")"
        );

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS collaborators (" +
            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " name TEXT NOT NULL," +
            " category TEXT NOT NULL," +
            " project_name TEXT NOT NULL," +
            " FOREIGN KEY(project_name) REFERENCES projects(name)" +
            ")"
        );

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS tasks (" +
            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " title TEXT NOT NULL," +
            " description TEXT," +
            " priority TEXT NOT NULL," +
            " status TEXT NOT NULL," +
            " due_date TEXT," +
            " project_name TEXT," +
            " FOREIGN KEY(project_name) REFERENCES projects(name)" +
            ")"
        );

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS subtasks (" +
            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " title TEXT NOT NULL," +
            " completed INTEGER NOT NULL," +
            " task_title TEXT NOT NULL," +
            " task_due_date TEXT," +
            " collaborator_name TEXT," +
            " project_name TEXT" +
            ")"
        );

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS task_tags (" +
            " task_title TEXT NOT NULL," +
            " task_due_date TEXT," +
            " tag_name TEXT NOT NULL" +
            ")"
        );

        stmt.close();
    }

    private void clearDatabase(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM task_tags");
        stmt.executeUpdate("DELETE FROM subtasks");
        stmt.executeUpdate("DELETE FROM tasks");
        stmt.executeUpdate("DELETE FROM collaborators");
        stmt.executeUpdate("DELETE FROM projects");
        stmt.close();
    }

    @Override
    public void save(TaskManager manager) throws SQLException {
        Connection conn = connect();
        try {
            conn.setAutoCommit(false);
            createTables(conn);
            clearDatabase(conn);

            saveProjects(conn, manager);
            saveCollaborators(conn, manager);
            saveTasks(conn, manager);
            saveSubtasks(conn, manager);
            saveTags(conn, manager);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    private void saveProjects(Connection conn, TaskManager manager) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO projects(name, description) VALUES(?, ?)"
        );

        for (Project project : manager.getAllProjects()) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.executeUpdate();
        }

        ps.close();
    }

    private void saveCollaborators(Connection conn, TaskManager manager) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO collaborators(name, category, project_name) VALUES(?, ?, ?)"
        );

        for (Project project : manager.getAllProjects()) {
            for (Collaborator c : project.getCollaborators()) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getCategory().toString());
                ps.setString(3, project.getName());
                ps.executeUpdate();
            }
        }

        ps.close();
    }

    private void saveTasks(Connection conn, TaskManager manager) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO tasks(title, description, priority, status, due_date, project_name) " +
            "VALUES(?, ?, ?, ?, ?, ?)"
        );

        for (Task task : manager.getAllTasks()) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority().toString());
            ps.setString(4, task.getStatus().toString());
            ps.setString(5, task.getDueDate() != null ? task.getDueDate().toString() : null);
            ps.setString(6, task.getProject() != null ? task.getProject().getName() : null);
            ps.executeUpdate();
        }

        ps.close();
    }

    private void saveSubtasks(Connection conn, TaskManager manager) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO subtasks(title, completed, task_title, task_due_date, collaborator_name, project_name) " +
            "VALUES(?, ?, ?, ?, ?, ?)"
        );

        for (Task task : manager.getAllTasks()) {
            for (Subtask subtask : task.getSubtasks()) {
                ps.setString(1, subtask.getTitle());
                ps.setInt(2, subtask.isCompleted() ? 1 : 0);
                ps.setString(3, task.getTitle());
                ps.setString(4, task.getDueDate() != null ? task.getDueDate().toString() : null);
                ps.setString(5, subtask.getCollaborator() != null ? subtask.getCollaborator().getName() : null);
                ps.setString(6, task.getProject() != null ? task.getProject().getName() : null);
                ps.executeUpdate();
            }
        }

        ps.close();
    }

    private void saveTags(Connection conn, TaskManager manager) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO task_tags(task_title, task_due_date, tag_name) VALUES(?, ?, ?)"
        );

        for (Task task : manager.getAllTasks()) {
            for (Tag tag : task.getTags()) {
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDueDate() != null ? task.getDueDate().toString() : null);
                ps.setString(3, tag.getName());
                ps.executeUpdate();
            }
        }

        ps.close();
    }

    @Override
    public TaskManager load() throws SQLException {
        Connection conn = connect();
        TaskManager manager = new TaskManager();

        try {
            createTables(conn);

            Map<String, Project> projectsByName = loadProjects(conn, manager);
            Map<String, Collaborator> collaboratorsByProjectAndName = loadCollaborators(conn, manager, projectsByName);
            Map<String, Task> tasksByKey = loadTasks(conn, manager, projectsByName);

            loadSubtasks(conn, tasksByKey, collaboratorsByProjectAndName);
            loadTags(conn, manager, tasksByKey);

        } finally {
            conn.close();
        }

        return manager;
    }

    private Map<String, Project> loadProjects(Connection conn, TaskManager manager) throws SQLException {
        Map<String, Project> map = new HashMap<String, Project>();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name, description FROM projects");

        while (rs.next()) {
            String name = rs.getString("name");
            String description = rs.getString("description");
            Project project = manager.createProject(name, description);
            if (project != null) {
                map.put(name.toLowerCase(), project);
            }
        }

        rs.close();
        stmt.close();
        return map;
    }

    private Map<String, Collaborator> loadCollaborators(
            Connection conn,
            TaskManager manager,
            Map<String, Project> projectsByName) throws SQLException {

        Map<String, Collaborator> map = new HashMap<String, Collaborator>();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name, category, project_name FROM collaborators");

        while (rs.next()) {
            String name = rs.getString("name");
            String categoryStr = rs.getString("category");
            String projectName = rs.getString("project_name");

            Project project = projectsByName.get(projectName.toLowerCase());
            if (project == null) continue;

            CollaboratorCategory category = CollaboratorCategory.valueOf(categoryStr);
            Collaborator c = manager.getOrCreateCollaborator(name, category, project);

            map.put(buildCollaboratorKey(projectName, name), c);
        }

        rs.close();
        stmt.close();
        return map;
    }

    private Map<String, Task> loadTasks(
            Connection conn,
            TaskManager manager,
            Map<String, Project> projectsByName) throws SQLException {

        Map<String, Task> map = new HashMap<String, Task>();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT title, description, priority, status, due_date, project_name FROM tasks"
        );

        while (rs.next()) {
            String title = rs.getString("title");
            String description = rs.getString("description");
            Priority priority = Priority.valueOf(rs.getString("priority"));
            TaskStatus status = TaskStatus.valueOf(rs.getString("status"));

            String dueDateStr = rs.getString("due_date");
            LocalDate dueDate = dueDateStr != null ? LocalDate.parse(dueDateStr) : null;

            Task task = manager.createTask(title, description, priority, dueDate);

            if (status != TaskStatus.OPEN) {
                task.setStatus(status);
            }

            String projectName = rs.getString("project_name");
            if (projectName != null) {
                Project project = projectsByName.get(projectName.toLowerCase());
                if (project != null) {
                    manager.assignTaskToProject(task, project);
                }
            }

            map.put(buildTaskKey(title, dueDate), task);
        }

        rs.close();
        stmt.close();
        return map;
    }

    private void loadSubtasks(
            Connection conn,
            Map<String, Task> tasksByKey,
            Map<String, Collaborator> collaboratorsByProjectAndName) throws SQLException {

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT title, completed, task_title, task_due_date, collaborator_name, project_name FROM subtasks"
        );

        while (rs.next()) {
            String title = rs.getString("title");
            int completed = rs.getInt("completed");
            String taskTitle = rs.getString("task_title");
            String taskDueDateStr = rs.getString("task_due_date");
            String collaboratorName = rs.getString("collaborator_name");
            String projectName = rs.getString("project_name");

            LocalDate dueDate = taskDueDateStr != null ? LocalDate.parse(taskDueDateStr) : null;
            Task task = tasksByKey.get(buildTaskKey(taskTitle, dueDate));

            if (task == null) continue;

            Subtask subtask = task.addSubtask(title);
            subtask.setCompleted(completed == 1);

            if (collaboratorName != null && projectName != null) {
                Collaborator collaborator = collaboratorsByProjectAndName.get(
                    buildCollaboratorKey(projectName, collaboratorName)
                );
                if (collaborator != null) {
                    subtask.setCollaborator(collaborator);
                    collaborator.addSubtask(subtask);
                }
            }
        }

        rs.close();
        stmt.close();
    }

    private void loadTags(Connection conn, TaskManager manager, Map<String, Task> tasksByKey) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT task_title, task_due_date, tag_name FROM task_tags"
        );

        while (rs.next()) {
            String taskTitle = rs.getString("task_title");
            String dueDateStr = rs.getString("task_due_date");
            String tagName = rs.getString("tag_name");

            LocalDate dueDate = dueDateStr != null ? LocalDate.parse(dueDateStr) : null;
            Task task = tasksByKey.get(buildTaskKey(taskTitle, dueDate));

            if (task != null) {
                manager.addTagToTask(task, tagName);
            }
        }

        rs.close();
        stmt.close();
    }

    private String buildTaskKey(String title, LocalDate dueDate) {
        return title.toLowerCase() + "|" + (dueDate != null ? dueDate.toString() : "null");
    }

    private String buildCollaboratorKey(String projectName, String collaboratorName) {
        return projectName.toLowerCase() + "|" + collaboratorName.toLowerCase();
    }
}