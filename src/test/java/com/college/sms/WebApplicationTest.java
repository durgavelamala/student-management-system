package com.college.sms;

import com.college.sms.entity.Student;
import com.college.sms.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests: start the whole application (with an in-memory H2 database and the demo data)
 * and send real HTTP requests through Spring Security and the controllers.
 * Every page is rendered, so a broken Thymeleaf template makes these tests fail.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    // ---------- Authentication ----------

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void anonymousUserIsSentToLoginPage() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void teacherLoginRedirectsToTeacherDashboard() throws Exception {
        mockMvc.perform(formLogin("/login").user("teacher").password("teacher123"))
                .andExpect(redirectedUrl("/teacher/dashboard"));
    }

    @Test
    void studentLoginRedirectsToStudentDashboard() throws Exception {
        mockMvc.perform(formLogin("/login").user("student").password("student123"))
                .andExpect(redirectedUrl("/student/dashboard"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(formLogin("/login").user("teacher").password("wrong"))
                .andExpect(redirectedUrl("/login?error"));
    }

    // ---------- Role-based access ----------

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentCannotOpenTeacherPages() throws Exception {
        mockMvc.perform(get("/teacher/students")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherCannotOpenStudentPortal() throws Exception {
        mockMvc.perform(get("/student/dashboard")).andExpect(status().isForbidden());
    }

    // ---------- Teacher pages render ----------

    @Test
    @WithUserDetails("teacher")
    void teacherPagesLoad() throws Exception {
        Long id = studentRepository.findAllByOrderByStudentIdAsc().get(0).getId();

        mockMvc.perform(get("/teacher/dashboard")).andExpect(status().isOk()).andExpect(model().attributeExists("stats"));
        mockMvc.perform(get("/teacher/students")).andExpect(status().isOk());
        mockMvc.perform(get("/teacher/students").param("keyword", "rahul")).andExpect(status().isOk());
        mockMvc.perform(get("/teacher/students/new")).andExpect(status().isOk());
        mockMvc.perform(get("/teacher/students/" + id)).andExpect(status().isOk());
        mockMvc.perform(get("/teacher/students/" + id + "/edit")).andExpect(status().isOk());
        mockMvc.perform(get("/teacher/attendance")).andExpect(status().isOk());
        mockMvc.perform(get("/teacher/marks")).andExpect(status().isOk());
        mockMvc.perform(get("/teacher/announcements")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("teacher")
    void unknownStudentShowsNotFoundPage() throws Exception {
        mockMvc.perform(get("/teacher/students/999999")).andExpect(status().isNotFound());
    }

    // ---------- Student pages render ----------

    @Test
    @WithUserDetails("student")
    void studentPagesLoad() throws Exception {
        mockMvc.perform(get("/student/dashboard")).andExpect(status().isOk()).andExpect(model().attributeExists("student"));
        mockMvc.perform(get("/student/profile")).andExpect(status().isOk());
        mockMvc.perform(get("/student/marks")).andExpect(status().isOk());
        mockMvc.perform(get("/student/attendance")).andExpect(status().isOk());
        mockMvc.perform(get("/student/announcements")).andExpect(status().isOk());
    }

    // ---------- CRUD ----------

    @Test
    @Transactional
    @WithUserDetails("teacher")
    void teacherCanAddStudent() throws Exception {
        mockMvc.perform(post("/teacher/students/save").with(csrf())
                        .param("studentId", "STU900")
                        .param("fullName", "Test Student")
                        .param("email", "test.student@college.edu")
                        .param("department", "Computer Science")
                        .param("year", "2")
                        .param("phone", "9123456789"))
                .andExpect(redirectedUrl("/teacher/students"));

        assertThat(studentRepository.existsByStudentIdIgnoreCase("STU900")).isTrue();
    }

    @Test
    @Transactional
    @WithUserDetails("teacher")
    void invalidStudentFormShowsErrors() throws Exception {
        mockMvc.perform(post("/teacher/students/save").with(csrf())
                        .param("studentId", "")
                        .param("fullName", "A")
                        .param("email", "not-an-email")
                        .param("department", "")
                        .param("year", "9")
                        .param("phone", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("student", "studentId", "fullName", "email", "phone", "year"));
    }

    @Test
    @Transactional
    @WithUserDetails("teacher")
    void teacherCanDeleteStudent() throws Exception {
        Student student = studentRepository.findAllByOrderByStudentIdAsc().get(1);

        mockMvc.perform(post("/teacher/students/" + student.getId() + "/delete").with(csrf()))
                .andExpect(redirectedUrl("/teacher/students"));

        assertThat(studentRepository.findById(student.getId())).isEmpty();
    }
}
