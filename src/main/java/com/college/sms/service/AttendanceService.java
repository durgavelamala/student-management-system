package com.college.sms.service;

import com.college.sms.dto.AttendanceSummary;
import com.college.sms.entity.Attendance;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository,
                             SubjectRepository subjectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    /** Map of student database id -> present? for one subject and date (pre-fills the form). */
    public Map<Long, Boolean> attendanceFor(Subject subject, LocalDate date) {
        Map<Long, Boolean> map = new HashMap<>();
        for (Attendance a : attendanceRepository.findBySubjectAndAttendanceDate(subject, date)) {
            map.put(a.getStudent().getId(), a.isPresent());
        }
        return map;
    }

    /**
     * Marks students present/absent for one subject and date.
     * Existing entries for that day are updated instead of duplicated.
     */
    @Transactional
    public int saveAttendance(Long subjectId, LocalDate date, Map<Long, Boolean> presentByStudentId) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Attendance cannot be marked for a future date");
        }
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
        int saved = 0;
        for (Map.Entry<Long, Boolean> entry : presentByStudentId.entrySet()) {
            Student student = studentRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            boolean present = Boolean.TRUE.equals(entry.getValue());
            Attendance attendance = attendanceRepository
                    .findByStudentAndSubjectAndAttendanceDate(student, subject, date)
                    .orElse(new Attendance(student, subject, date, present));
            attendance.setPresent(present);
            attendanceRepository.save(attendance);
            saved++;
        }
        return saved;
    }

    /** Overall attendance of one student across all subjects. */
    public AttendanceSummary summaryFor(Student student) {
        long total = attendanceRepository.countByStudent(student);
        long present = attendanceRepository.countByStudentAndPresentTrue(student);
        return new AttendanceSummary(student, null, total, present, percentage(present, total));
    }

    /** Overall attendance of every student (teacher's attendance page). */
    public List<AttendanceSummary> summariesForAllStudents() {
        List<AttendanceSummary> list = new ArrayList<>();
        for (Student student : studentRepository.findAllByOrderByStudentIdAsc()) {
            list.add(summaryFor(student));
        }
        return list;
    }

    /** Attendance of one student in each subject. */
    public List<AttendanceSummary> subjectWiseFor(Student student) {
        List<AttendanceSummary> list = new ArrayList<>();
        for (Subject subject : subjectRepository.findAllByOrderByCodeAsc()) {
            long total = attendanceRepository.countByStudentAndSubject(student, subject);
            long present = attendanceRepository.countByStudentAndSubjectAndPresentTrue(student, subject);
            list.add(new AttendanceSummary(student, subject, total, present, percentage(present, total)));
        }
        return list;
    }

    public List<Attendance> recordsFor(Student student) {
        return attendanceRepository.findByStudentOrderByAttendanceDateDesc(student);
    }

    public double overallAverage() {
        return percentage(attendanceRepository.countByPresentTrue(), attendanceRepository.count());
    }

    public long totalPresent() {
        return attendanceRepository.countByPresentTrue();
    }

    public long totalAbsent() {
        return attendanceRepository.count() - attendanceRepository.countByPresentTrue();
    }

    /** present / total as a percentage with one decimal place; 0 when there are no classes. */
    public static double percentage(long present, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(present * 1000.0 / total) / 10.0;
    }
}
