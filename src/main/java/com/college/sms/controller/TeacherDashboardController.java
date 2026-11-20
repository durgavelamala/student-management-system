package com.college.sms.controller;

import com.college.sms.dto.AttendanceSummary;
import com.college.sms.dto.SubjectPerformance;
import com.college.sms.service.AnnouncementService;
import com.college.sms.service.AttendanceService;
import com.college.sms.service.DashboardService;
import com.college.sms.service.MarksService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {

    private final DashboardService dashboardService;
    private final MarksService marksService;
    private final AttendanceService attendanceService;
    private final AnnouncementService announcementService;

    public TeacherDashboardController(DashboardService dashboardService,
                                      MarksService marksService,
                                      AttendanceService attendanceService,
                                      AnnouncementService announcementService) {
        this.dashboardService = dashboardService;
        this.marksService = marksService;
        this.attendanceService = attendanceService;
        this.announcementService = announcementService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Data for the "average marks per subject" bar chart
        List<SubjectPerformance> subjectAverages = marksService.subjectAverages();
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartValues = new ArrayList<>();
        for (SubjectPerformance sp : subjectAverages) {
            chartLabels.add(sp.getSubjectCode());
            chartValues.add(sp.getMarks());
        }

        // Students below 75% attendance
        List<AttendanceSummary> lowAttendance = new ArrayList<>();
        for (AttendanceSummary summary : attendanceService.summariesForAllStudents()) {
            if (summary.isLow()) {
                lowAttendance.add(summary);
            }
        }

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("stats", dashboardService.teacherStats());
        model.addAttribute("subjectAverages", subjectAverages);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);
        model.addAttribute("presentCount", attendanceService.totalPresent());
        model.addAttribute("absentCount", attendanceService.totalAbsent());
        model.addAttribute("lowAttendance", lowAttendance);
        model.addAttribute("recentAnnouncements", announcementService.findRecent());
        return "teacher/dashboard";
    }
}
