package com.college.sms.service;

import com.college.sms.entity.Role;
import com.college.sms.entity.Student;
import com.college.sms.entity.User;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.MarksRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for student records (used by the teacher's Students pages).
 */
@Service
public class StudentService {

    /** Every student added by a teacher gets this starting password. */
    public static final String DEFAULT_STUDENT_PASSWORD = "student123";

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final MarksRepository marksRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          MarksRepository marksRepository,
                          AttendanceRepository attendanceRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.marksRepository = marksRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Student> findAll() {
        return studentRepository.findAllByOrderByStudentIdAsc();
    }

    /** Returns all students when the keyword is empty, otherwise the matching ones. */
    public List<Student> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return studentRepository.search(keyword.trim());
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id " + id));
    }

    public Optional<Student> findByUsername(String username) {
        return studentRepository.findByUserUsername(username);
    }

    public long count() {
        return studentRepository.count();
    }

    /**
     * Saves a new student and creates their login account.
     * Login username = student ID in lower case, password = DEFAULT_STUDENT_PASSWORD.
     */
    @Transactional
    public Student create(Student student) {
        String studentId = student.getStudentId().trim().toUpperCase();
        String email = student.getEmail().trim().toLowerCase();

        if (studentRepository.existsByStudentIdIgnoreCase(studentId)) {
            throw new IllegalArgumentException("Student ID " + studentId + " already exists");
        }
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email " + email + " is already used by another student");
        }
        String username = studentId.toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("A login with username " + username + " already exists");
        }

        User user = userRepository.save(
                new User(username, passwordEncoder.encode(DEFAULT_STUDENT_PASSWORD), Role.STUDENT));

        student.setId(null);
        student.setStudentId(studentId);
        student.setEmail(email);
        student.setFullName(student.getFullName().trim());
        student.setUser(user);
        return studentRepository.save(student);
    }

    /** Updates the editable fields. Student ID and login account are not changed. */
    @Transactional
    public Student update(Long id, Student form) {
        Student existing = findById(id);
        String email = form.getEmail().trim().toLowerCase();

        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new IllegalArgumentException("Email " + email + " is already used by another student");
        }

        existing.setFullName(form.getFullName().trim());
        existing.setEmail(email);
        existing.setDepartment(form.getDepartment());
        existing.setYear(form.getYear());
        existing.setPhone(form.getPhone());
        return studentRepository.save(existing);
    }

    /** Deletes the student together with their marks, attendance and login account. */
    @Transactional
    public void delete(Long id) {
        Student student = findById(id);
        User user = student.getUser();
        marksRepository.deleteByStudent(student);
        attendanceRepository.deleteByStudent(student);
        studentRepository.delete(student);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
