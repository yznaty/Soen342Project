package com.taskmanager;

import java.time.LocalDate;

public class SearchCriteria {

    public String keyword;       // matches title or description
    public TaskStatus status;
    public Priority priority;
    public LocalDate startDate;
    public LocalDate endDate;
    public String projectName;
    public String tag;
    public String dayOfWeek;     

    public SearchCriteria() {
    }
}
