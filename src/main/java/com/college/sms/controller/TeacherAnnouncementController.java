package com.college.sms.controller;

import com.college.sms.entity.Announcement;
import com.college.sms.service.AnnouncementService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/teacher/announcements")
public class TeacherAnnouncementController {

    private final AnnouncementService announcementService;

    public TeacherAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /** Only title and message come from the form; the rest is set by the server. */
    @InitBinder("announcement")
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields("title", "message");
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activePage", "announcements");
        model.addAttribute("announcement", new Announcement());
        model.addAttribute("announcements", announcementService.findAll());
        return "teacher/announcements";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("announcement") Announcement announcement,
                         BindingResult bindingResult,
                         Principal principal,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "announcements");
            model.addAttribute("announcements", announcementService.findAll());
            return "teacher/announcements";
        }
        announcementService.create(announcement, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Announcement published");
        return "redirect:/teacher/announcements";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            announcementService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Announcement deleted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/announcements";
    }
}
