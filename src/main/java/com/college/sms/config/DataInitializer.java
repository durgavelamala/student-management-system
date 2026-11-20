package com.college.sms.config;

import com.college.sms.entity.Announcement;
import com.college.sms.entity.Attendance;
import com.college.sms.entity.Marks;
import com.college.sms.entity.Role;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.entity.User;
import com.college.sms.repository.AnnouncementRepository;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.MarksRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import com.college.sms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Inserts demo data the first time the application starts (when the users table is empty),
 * so the dashboards are not empty. On later restarts nothing is inserted and existing data is kept.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final MarksRepository marksRepository;
    private final AttendanceRepository attendanceRepository;
    private final AnnouncementRepository announcementRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           SubjectRepository subjectRepository,
                           MarksRepository marksRepository,
                           AttendanceRepository attendanceRepository,
                           AnnouncementRepository announcementRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.marksRepository = marksRepository;
        this.attendanceRepository = attendanceRepository;
        this.announcementRepository = announcementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Demo data already present - skipping data initialisation");
            return;
        }
        log.info("Empty database detected - inserting demo data");

        // ---- Teacher account ----
        userRepository.save(new User("teacher", passwordEncoder.encode("teacher123"), Role.TEACHER));

        // ---- Subjects ----
        List<Subject> subjects = subjectRepository.saveAll(List.of(
                new Subject("CS301", "Data Structures"),
                new Subject("CS302", "Database Management Systems"),
                new Subject("CS303", "Operating Systems"),
                new Subject("CS304", "Computer Networks"),
                new Subject("CS305", "DevOps Fundamentals")));

        // ---- Students (the first one uses the demo login "student") ----
        List<Student> students = new ArrayList<>();
        students.add(createStudent("student", new Student("STU001", "Rahul Sharma", "rahul.sharma@college.edu", "Computer Science", 3, "9876543210")));
        students.add(createStudent("stu002", new Student("STU002", "Priya Reddy", "priya.reddy@college.edu", "Computer Science", 3, "9876543211")));
        students.add(createStudent("stu003", new Student("STU003", "Arjun Kumar", "arjun.kumar@college.edu", "Electronics", 2, "9876543212")));
        students.add(createStudent("stu004", new Student("STU004", "Sneha Patel", "sneha.patel@college.edu", "Information Technology", 4, "9876543213")));
        students.add(createStudent("stu005", new Student("STU005", "Kiran Rao", "kiran.rao@college.edu", "Mechanical", 1, "9876543214")));
        students.add(createStudent("stu006", new Student("STU006", "Ananya Iyer", "ananya.iyer@college.edu", "Computer Science", 2, "9876543215")));

        // Fixed seed so the demo data is the same every time
        Random random = new Random(42);

        // ---- Marks ----
        for (Student student : students) {
            for (Subject subject : subjects) {
                int marks = 55 + random.nextInt(44); // 55 - 98
                marksRepository.save(new Marks(student, subject, marks));
            }
        }

        // ---- Attendance for the last 10 weekdays ----
        List<LocalDate> days = lastWeekdays(10);
        for (LocalDate day : days) {
            for (Subject subject : subjects) {
                for (Student student : students) {
                    boolean present = random.nextInt(100) < 85; // about 85% present
                    attendanceRepository.save(new Attendance(student, subject, day, present));
                }
            }
        }

        // ---- Announcements ----
        LocalDateTime now = LocalDateTime.now();
        announcementRepository.save(new Announcement("Welcome to the new semester",
                "Classes for all years begin on Monday. Please check your timetable on the notice board.",
                "teacher", now.minusDays(6)));
        announcementRepository.save(new Announcement("Mid-term examinations",
                "Mid-term exams start next week. Marks will be updated on the portal within 7 days of each exam.",
                "teacher", now.minusDays(3)));
        announcementRepository.save(new Announcement("DevOps lab: Jenkins and Docker",
                "Bring your laptops with Docker Desktop and Jenkins installed for Friday's lab session.",
                "teacher", now.minusDays(1)));

        log.info("Demo data inserted: 1 teacher, {} students, {} subjects", students.size(), subjects.size());
    }

    private Student createStudent(String username, Student student) {
        User user = userRepository.save(new User(username, passwordEncoder.encode("student123"), Role.STUDENT));
        student.setUser(user);
        return studentRepository.save(student);
    }

    /** The last {@code count} weekdays, ending yesterday (oldest first). */
    private List<LocalDate> lastWeekdays(int count) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate day = LocalDate.now().minusDays(1);
        while (days.size() < count) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days.add(0, day);
            }
            day = day.minusDays(1);
        }
        return days;
    }
}
