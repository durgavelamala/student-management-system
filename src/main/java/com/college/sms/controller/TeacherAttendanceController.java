package com.college.sms.controller;

import com.college.sms.entity.Subject;
import com.college.sms.service.AttendanceService;
import com.college.sms.service.StudentService;
import com.college.sms.service.SubjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Teacher's attendance page: pick a subject and date, mark each student present/absent,
 * and see every student's overall attendance percentage.
 */
@Controller
@RequestMapping("/teacher/attendance")
public class TeacherAttendanceController {

    private static final String STATUS_PREFIX = "status_";

    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final SubjectService subjectService;

    public TeacherAttendanceController(AttendanceService attendanceService,
                                       StudentService studentService,
                                       SubjectService subjectService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.subjectService = subjectService;
    }

    @GetMapping
    public String attendancePage(@RequestParam(value = "subjectId", required = false) Long subjectId,
                                 @RequestParam(value = "date", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 Model model) {
        LocalDate selectedDate = (date == null) ? LocalDate.now() : date;
        Subject subject = subjectService.findOrFirst(subjectId);

        model.addAttribute("activePage", "attendance");
        model.addAttribute("subjects", subjectService.findAll());
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("attendanceMap",
                subject == null ? new HashMap<Long, Boolean>() : attendanceService.attendanceFor(subject, selectedDate));
        model.addAttribute("summaries", attendanceService.summariesForAllStudents());
        model.addAttribute("overallAverage", attendanceService.overallAverage());
        return "teacher/attendance";
    }

    /** Form fields look like status_5=PRESENT, status_6=ABSENT (5 and 6 are student ids). */
    @PostMapping("/save")
    public String save(@RequestParam("subjectId") Long subjectId,
                       @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       @RequestParam Map<String, String> params,
                       RedirectAttributes redirectAttributes) {
        Map<Long, Boolean> presentByStudentId = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith(STATUS_PREFIX)) {
                Long studentId = Long.valueOf(entry.getKey().substring(STATUS_PREFIX.length()));
                presentByStudentId.put(studentId, "PRESENT".equals(entry.getValue()));
            }
        }

        try {
            if (presentByStudentId.isEmpty()) {
                throw new IllegalArgumentException("No students to mark");
            }
            int saved = attendanceService.saveAttendance(subjectId, date, presentByStudentId);
            redirectAttributes.addFlashAttribute("success", "Attendance saved for " + saved + " students on " + date);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        redirectAttributes.addAttribute("subjectId", subjectId);
        redirectAttributes.addAttribute("date", date.toString());
        return "redirect:/teacher/attendance";
    }
}
