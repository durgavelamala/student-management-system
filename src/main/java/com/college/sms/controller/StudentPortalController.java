package com.college.sms.controller;

import com.college.sms.dto.SubjectPerformance;
import com.college.sms.entity.Student;
import com.college.sms.service.AnnouncementService;
import com.college.sms.service.AttendanceService;
import com.college.sms.service.DashboardService;
import com.college.sms.service.MarksService;
import com.college.sms.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pages for logged-in students. Every page loads data ONLY for the logged-in student
 * (found from the login username), so a student can never see another student's data.
 * All pages are read-only.
 */
@Controller
@RequestMapping("/student")
public class StudentPortalController {

    private final StudentService studentService;
    private final MarksService marksService;
    private final AttendanceService attendanceService;
    private final AnnouncementService announcementService;
    private final DashboardService dashboardService;

    public StudentPortalController(StudentService studentService,
                                   MarksService marksService,
                                   AttendanceService attendanceService,
                                   AnnouncementService announcementService,
                                   DashboardService dashboardService) {
        this.studentService = studentService;
        this.marksService = marksService;
        this.attendanceService = attendanceService;
        this.announcementService = announcementService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Student student = currentStudent(principal, model, "dashboard");
        List<SubjectPerformance> performance = dashboardService.performanceFor(student);

        List<String> chartLabels = new ArrayList<>();
        List<Double> chartMarks = new ArrayList<>();
        List<Double> chartAttendance = new ArrayList<>();
        for (SubjectPerformance sp : performance) {
            chartLabels.add(sp.getSubjectCode());
            chartMarks.add(sp.getMarks() == null ? 0.0 : sp.getMarks());
            chartAttendance.add(sp.getAttendancePercentage());
        }

        model.addAttribute("attendance", attendanceService.summaryFor(student));
        model.addAttribute("averageMarks", marksService.averageForStudent(student));
        model.addAttribute("performance", performance);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartMarks", chartMarks);
        model.addAttribute("chartAttendance", chartAttendance);
        model.addAttribute("recentAnnouncements", announcementService.findRecent());
        return "student/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        currentStudent(principal, model, "profile");
        return "student/profile";
    }

    @GetMapping("/marks")
    public String marks(Principal principal, Model model) {
        Student student = currentStudent(principal, model, "marks");
        model.addAttribute("marksList", marksService.findByStudent(student));
        model.addAttribute("averageMarks", marksService.averageForStudent(student));
        return "student/marks";
    }

    @GetMapping("/attendance")
    public String attendance(Principal principal, Model model) {
        Student student = currentStudent(principal, model, "attendance");
        model.addAttribute("attendance", attendanceService.summaryFor(student));
        model.addAttribute("subjectWise", attendanceService.subjectWiseFor(student));
        model.addAttribute("records", attendanceService.recordsFor(student));
        return "student/attendance";
    }

    @GetMapping("/announcements")
    public String announcements(Principal principal, Model model) {
        currentStudent(principal, model, "announcements");
        model.addAttribute("announcements", announcementService.findAll());
        return "student/announcements";
    }

    /** Finds the Student linked to the logged-in user and adds common attributes to the model. */
    private Student currentStudent(Principal principal, Model model, String activePage) {
        Student student = studentService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No student profile is linked to the account '" + principal.getName() + "'"));
        model.addAttribute("student", student);
        model.addAttribute("activePage", activePage);
        return student;
    }
}
