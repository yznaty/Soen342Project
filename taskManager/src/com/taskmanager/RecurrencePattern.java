package com.taskmanager;

import java.time.LocalDate;
import java.util.ArrayList;

public class RecurrencePattern {

    private RecurrenceType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private int interval; // for CUSTOM: days between occurrences
    private int dayOfMonth; // for MONTHLY
    private ArrayList<String> weekdays; // for WEEKLY

    public RecurrencePattern(RecurrenceType type, LocalDate startDate, LocalDate endDate) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.interval = 1;
        this.dayOfMonth = 1;
        this.weekdays = new ArrayList<String>();
    }

    public RecurrenceType getType() { return type; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getInterval() { return interval; }
    public int getDayOfMonth() { return dayOfMonth; }
    public ArrayList<String> getWeekdays() { return weekdays; }

    public void setInterval(int interval) { this.interval = interval; }
    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public void setWeekdays(ArrayList<String> weekdays) { this.weekdays = weekdays; }

    public ArrayList<LocalDate> generateOccurrences() {
        ArrayList<LocalDate> occurrences = new ArrayList<LocalDate>();

        if (startDate == null || endDate == null) {
            return occurrences;
        }

        if (type == RecurrenceType.DAILY) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                occurrences.add(current);
                current = current.plusDays(1);
            }

        } else if (type == RecurrenceType.WEEKLY) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                String dayName = current.getDayOfWeek().toString();
                if (weekdays.contains(dayName)) {
                    occurrences.add(current);
                }
                current = current.plusDays(1);
            }

        } else if (type == RecurrenceType.MONTHLY) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                if (current.getDayOfMonth() == dayOfMonth) {
                    occurrences.add(current);
                }
                current = current.plusDays(1);
            }

        } else if (type == RecurrenceType.CUSTOM) {
            int step = interval;
            if (step < 1) step = 1;
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                occurrences.add(current);
                current = current.plusDays(step);
            }
        }

        return occurrences;
    }

    public String toString() {
        return type + " from " + startDate + " to " + endDate;
    }
}
