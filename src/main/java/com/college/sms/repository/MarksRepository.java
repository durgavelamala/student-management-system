package com.college.sms.repository;

import com.college.sms.entity.Marks;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarksRepository extends JpaRepository<Marks, Long> {

    Optional<Marks> findByStudentAndSubject(Student student, Subject subject);

    List<Marks> findByStudentOrderBySubjectCodeAsc(Student student);

    List<Marks> findBySubject(Subject subject);

    void deleteByStudent(Student student);

    @Query("SELECT AVG(m.marksObtained) FROM Marks m")
    Double findOverallAverage();

    @Query("SELECT AVG(m.marksObtained) FROM Marks m WHERE m.subject = :subject")
    Double findAverageBySubject(@Param("subject") Subject subject);
}
