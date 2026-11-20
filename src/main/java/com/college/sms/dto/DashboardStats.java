package com.college.sms.dto;

/**
 * Numbers shown on the teacher dashboard cards.
 */
public class DashboardStats {

    private final long totalStudents;
    private final long totalSubjects;
    private final double averageAttendance;
    private final double averageMarks;

    public DashboardStats(long totalStudents, long totalSubjects, double averageAttendance, double averageMarks) {
        this.totalStudents = totalStudents;
        this.totalSubjects = totalSubjects;
        this.averageAttendance = averageAttendance;
        this.averageMarks = averageMarks;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public double getAverageAttendance() {
        return averageAttendance;
    }

    public double getAverageMarks() {
        return averageMarks;
    }
}
