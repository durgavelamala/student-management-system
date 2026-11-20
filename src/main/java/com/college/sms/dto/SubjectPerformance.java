package com.college.sms.dto;

/**
 * Subject-wise performance: marks, grade and attendance for one subject.
 * The teacher dashboard chart reuses it with marks = class average.
 */
public class SubjectPerformance {

    private final String subjectCode;
    private final String subjectName;
    private final Double marks;
    private final String grade;
    private final double attendancePercentage;

    public SubjectPerformance(String subjectCode, String subjectName, Double marks, String grade,
                              double attendancePercentage) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.marks = marks;
        this.grade = grade;
        this.attendancePercentage = attendancePercentage;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public Double getMarks() {
        return marks;
    }

    public String getGrade() {
        return grade;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
}
