package com.college.sms.service;

import com.college.sms.dto.AttendanceSummary;
import com.college.sms.dto.DashboardStats;
import com.college.sms.dto.SubjectPerformance;
import com.college.sms.entity.Marks;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the numbers shown on the two dashboards.
 */
@Service
public class DashboardService {

    private final StudentService studentService;
    private final SubjectService subjectService;
    private final MarksService marksService;
    private final AttendanceService attendanceService;

    public DashboardService(StudentService studentService,
                            SubjectService subjectService,
                            MarksService marksService,
                            AttendanceService attendanceService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.marksService = marksService;
        this.attendanceService = attendanceService;
    }

    public DashboardStats teacherStats() {
        return new DashboardStats(
                studentService.count(),
                subjectService.count(),
                attendanceService.overallAverage(),
                marksService.overallAverage());
    }

    /** For a student: marks, grade and attendance in every subject. */
    public List<SubjectPerformance> performanceFor(Student student) {
        List<Marks> marksList = marksService.findByStudent(student);
        List<AttendanceSummary> attendanceList = attendanceService.subjectWiseFor(student);

        List<SubjectPerformance> result = new ArrayList<>();
        for (AttendanceSummary att : attendanceList) {
            Subject subject = att.getSubject();
            Marks marks = null;
            for (Marks m : marksList) {
                if (m.getSubject().getId().equals(subject.getId())) {
                    marks = m;
                    break;
                }
            }
            Double value = marks == null ? null : marks.getMarksObtained().doubleValue();
            String grade = marks == null ? "-" : marks.getGrade();
            result.add(new SubjectPerformance(subject.getCode(), subject.getName(), value, grade,
                    att.getPercentage()));
        }
        return result;
    }
}
