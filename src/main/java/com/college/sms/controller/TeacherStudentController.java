package com.college.sms.controller;

import com.college.sms.entity.Student;
import com.college.sms.service.AttendanceService;
import com.college.sms.service.DashboardService;
import com.college.sms.service.MarksService;
import com.college.sms.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Teacher's student management: list, search, add, view, edit, delete.
 */
@Controller
@RequestMapping("/teacher/students")
public class TeacherStudentController {

    static final List<String> DEPARTMENTS = List.of(
            "Computer Science", "Information Technology", "Electronics", "Electrical", "Mechanical", "Civil");

    private final StudentService studentService;
    private final MarksService marksService;
    private final AttendanceService attendanceService;
    private final DashboardService dashboardService;

    public TeacherStudentController(StudentService studentService,
                                    MarksService marksService,
                                    AttendanceService attendanceService,
                                    DashboardService dashboardService) {
        this.studentService = studentService;
        this.marksService = marksService;
        this.attendanceService = attendanceService;
        this.dashboardService = dashboardService;
    }

    /** Never let a form submission set the database id or the linked login account. */
    @InitBinder("student")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("id", "user", "user.*");
    }

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("activePage", "students");
        model.addAttribute("departments", DEPARTMENTS);
    }

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("students", studentService.search(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "teacher/students";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("editMode", false);
        return "teacher/student-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("student") Student student,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", false);
            return "teacher/student-form";
        }
        try {
            Student saved = studentService.create(student);
            redirectAttributes.addFlashAttribute("success",
                    "Student " + saved.getFullName() + " added. Login: " + saved.getUser().getUsername()
                            + " / " + StudentService.DEFAULT_STUDENT_PASSWORD);
            return "redirect:/teacher/students";
        } catch (IllegalArgumentException e) {
            model.addAttribute("editMode", false);
            model.addAttribute("error", e.getMessage());
            return "teacher/student-form";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Student student = studentService.findById(id);
        model.addAttribute("student", student);
        model.addAttribute("attendance", attendanceService.summaryFor(student));
        model.addAttribute("averageMarks", marksService.averageForStudent(student));
        model.addAttribute("performance", dashboardService.performanceFor(student));
        return "teacher/student-view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        model.addAttribute("studentDbId", id);
        model.addAttribute("editMode", true);
        return "teacher/student-form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("studentDbId", id);
            model.addAttribute("editMode", true);
            return "teacher/student-form";
        }
        try {
            Student updated = studentService.update(id, student);
            redirectAttributes.addFlashAttribute("success", "Student " + updated.getFullName() + " updated successfully");
            return "redirect:/teacher/students";
        } catch (IllegalArgumentException e) {
            model.addAttribute("studentDbId", id);
            model.addAttribute("editMode", true);
            model.addAttribute("error", e.getMessage());
            return "teacher/student-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.findById(id);
            studentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Student " + student.getFullName() + " deleted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/students";
    }
}
