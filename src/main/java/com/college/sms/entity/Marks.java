package com.college.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Marks (out of 100) of one student in one subject.
 * A student can have only one marks row per subject.
 */
@Entity
@Table(name = "marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id"}))
public class Marks {

    public static final int MAX_MARKS = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer marksObtained;

    public Marks() {
    }

    public Marks(Student student, Subject subject, Integer marksObtained) {
        this.student = student;
        this.subject = subject;
        this.marksObtained = marksObtained;
    }

    /** Converts marks into a letter grade. */
    public static String gradeFor(int marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 50) return "D";
        return "F";
    }

    /** Grade of this marks row, e.g. "A". Used directly in Thymeleaf as ${m.grade}. */
    public String getGrade() {
        return marksObtained == null ? "-" : gradeFor(marksObtained);
    }

    public boolean isPassed() {
        return marksObtained != null && marksObtained >= 50;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Integer getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Integer marksObtained) {
        this.marksObtained = marksObtained;
    }
}
