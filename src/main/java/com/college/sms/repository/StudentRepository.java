package com.college.sms.repository;

import com.college.sms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findAllByOrderByStudentIdAsc();

    Optional<Student> findByUserUsername(String username);

    boolean existsByStudentIdIgnoreCase(String studentId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /** Search by name, student ID, email or department (case-insensitive). */
    @Query("SELECT s FROM Student s WHERE "
            + "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(s.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(s.department) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "ORDER BY s.studentId")
    List<Student> search(@Param("keyword") String keyword);
}
