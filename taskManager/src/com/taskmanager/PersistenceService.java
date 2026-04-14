package com.taskmanager;

import java.sql.SQLException;

public interface PersistenceService {
    void save(TaskManager manager) throws SQLException;
    TaskManager load() throws SQLException;
}