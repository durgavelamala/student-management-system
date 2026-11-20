package com.college.sms.dto;

import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;

/**
 * Attendance totals for a student: overall (subject is null) or for one subject.
 */
public class AttendanceSummary {

    private final Student student;
    private final Subject subject;
    private final long totalClasses;
    private final long presentClasses;
    private final double percentage;

    public AttendanceSummary(Student student, Subject subject, long totalClasses, long presentClasses, double percentage) {
        this.student = student;
        this.subject = subject;
        this.totalClasses = totalClasses;
        this.presentClasses = presentClasses;
        this.percentage = percentage;
    }

    public Student getStudent() {
        return student;
    }

    public Subject getSubject() {
        return subject;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public long getPresentClasses() {
        return presentClasses;
    }

    public long getAbsentClasses() {
        return totalClasses - presentClasses;
    }

    public double getPercentage() {
        return percentage;
    }

    /** Below 75% is usually treated as an attendance shortage in colleges. */
    public boolean isLow() {
        return totalClasses > 0 && percentage < 75.0;
    }
}
