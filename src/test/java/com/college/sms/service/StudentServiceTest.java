package com.college.sms.service;

import com.college.sms.entity.Role;
import com.college.sms.entity.Student;
import com.college.sms.entity.User;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.MarksRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StudentService. Repositories are replaced by Mockito mocks,
 * so these tests run in milliseconds without a database.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MarksRepository marksRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private Student newStudent() {
        return new Student("stu100", "New Student", "New@College.edu", "Computer Science", 1, "9999999999");
    }

    @Test
    void createRejectsDuplicateStudentId() {
        when(studentRepository.existsByStudentIdIgnoreCase("STU100")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(newStudent()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createSavesStudentAndCreatesLoginAccount() {
        when(studentRepository.existsByStudentIdIgnoreCase("STU100")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("new@college.edu")).thenReturn(false);
        when(userRepository.existsByUsername("stu100")).thenReturn(false);
        when(passwordEncoder.encode(StudentService.DEFAULT_STUDENT_PASSWORD)).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student saved = studentService.create(newStudent());

        assertThat(saved.getStudentId()).isEqualTo("STU100");
        assertThat(saved.getEmail()).isEqualTo("new@college.edu");
        assertThat(saved.getUser().getUsername()).isEqualTo("stu100");
        assertThat(saved.getUser().getPassword()).isEqualTo("ENCODED");
        assertThat(saved.getUser().getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void blankSearchReturnsAllStudents() {
        List<Student> all = List.of(newStudent());
        when(studentRepository.findAllByOrderByStudentIdAsc()).thenReturn(all);

        assertThat(studentService.search("   ")).isEqualTo(all);
        verify(studentRepository, never()).search(any());
    }
}
