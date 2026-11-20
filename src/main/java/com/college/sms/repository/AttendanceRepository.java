package com.college.sms.repository;

import com.college.sms.entity.Attendance;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByStudentAndSubjectAndAttendanceDate(Student student, Subject subject, LocalDate date);

    List<Attendance> findBySubjectAndAttendanceDate(Subject subject, LocalDate date);

    List<Attendance> findByStudentOrderByAttendanceDateDesc(Student student);

    long countByStudent(Student student);

    long countByStudentAndPresentTrue(Student student);

    long countByStudentAndSubject(Student student, Subject subject);

    long countByStudentAndSubjectAndPresentTrue(Student student, Subject subject);

    long countByPresentTrue();

    void deleteByStudent(Student student);
}
