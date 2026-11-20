package com.college.sms.controller;

import com.college.sms.entity.Marks;
import com.college.sms.entity.Subject;
import com.college.sms.service.MarksService;
import com.college.sms.service.StudentService;
import com.college.sms.service.SubjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Teacher's marks page: pick a subject, view current marks and add or update them.
 */
@Controller
@RequestMapping("/teacher/marks")
public class TeacherMarksController {

    private static final String MARKS_PREFIX = "marks_";

    private final MarksService marksService;
    private final StudentService studentService;
    private final SubjectService subjectService;

    public TeacherMarksController(MarksService marksService,
                                  StudentService studentService,
                                  SubjectService subjectService) {
        this.marksService = marksService;
        this.studentService = studentService;
        this.subjectService = subjectService;
    }

    @GetMapping
    public String marksPage(@RequestParam(value = "subjectId", required = false) Long subjectId, Model model) {
        Subject subject = subjectService.findOrFirst(subjectId);

        model.addAttribute("activePage", "marks");
        model.addAttribute("subjects", subjectService.findAll());
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("marksMap", subject == null ? new HashMap<Long, Marks>() : marksService.marksBySubject(subject));
        model.addAttribute("subjectAverage", subject == null ? 0.0 : marksService.averageForSubject(subject));
        model.addAttribute("subjectAverages", marksService.subjectAverages());
        return "teacher/marks";
    }

    /** Form fields look like marks_5=78 (5 is the student id). Empty boxes are skipped. */
    @PostMapping("/save")
    public String save(@RequestParam("subjectId") Long subjectId,
                       @RequestParam Map<String, String> params,
                       RedirectAttributes redirectAttributes) {
        Map<Long, Integer> marksByStudentId = new HashMap<>();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith(MARKS_PREFIX)) {
                    Long studentId = Long.valueOf(entry.getKey().substring(MARKS_PREFIX.length()));
                    String value = entry.getValue() == null ? "" : entry.getValue().trim();
                    marksByStudentId.put(studentId, value.isEmpty() ? null : Integer.valueOf(value));
                }
            }
            int saved = marksService.saveMarks(subjectId, marksByStudentId);
            redirectAttributes.addFlashAttribute("success", "Marks saved for " + saved + " students");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Marks must be whole numbers between 0 and 100");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addAttribute("subjectId", subjectId);
        return "redirect:/teacher/marks";
    }
}
