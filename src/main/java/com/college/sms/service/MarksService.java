package com.college.sms.service;

import com.college.sms.dto.SubjectPerformance;
import com.college.sms.entity.Marks;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.repository.MarksRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarksService {

    private final MarksRepository marksRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public MarksService(MarksRepository marksRepository,
                        StudentRepository studentRepository,
                        SubjectRepository subjectRepository) {
        this.marksRepository = marksRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    /** Map of student database id -> Marks for one subject (used to pre-fill the marks form). */
    public Map<Long, Marks> marksBySubject(Subject subject) {
        Map<Long, Marks> map = new HashMap<>();
        for (Marks m : marksRepository.findBySubject(subject)) {
            map.put(m.getStudent().getId(), m);
        }
        return map;
    }

    public List<Marks> findByStudent(Student student) {
        return marksRepository.findByStudentOrderBySubjectCodeAsc(student);
    }

    /**
     * Adds new marks or updates existing marks for one subject.
     * @param marksByStudentId student database id -> marks (null means "not entered", skipped)
     * @return how many rows were saved
     */
    @Transactional
    public int saveMarks(Long subjectId, Map<Long, Integer> marksByStudentId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
        int saved = 0;
        for (Map.Entry<Long, Integer> entry : marksByStudentId.entrySet()) {
            Integer value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value < 0 || value > Marks.MAX_MARKS) {
                throw new IllegalArgumentException("Marks must be between 0 and " + Marks.MAX_MARKS);
            }
            Student student = studentRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            Marks marks = marksRepository.findByStudentAndSubject(student, subject)
                    .orElse(new Marks(student, subject, value));
            marks.setMarksObtained(value);
            marksRepository.save(marks);
            saved++;
        }
        return saved;
    }

    public double overallAverage() {
        return round(marksRepository.findOverallAverage());
    }

    public double averageForStudent(Student student) {
        List<Marks> list = findByStudent(student);
        if (list.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Marks m : list) {
            sum += m.getMarksObtained();
        }
        return round(sum / list.size());
    }

    public double averageForSubject(Subject subject) {
        return round(marksRepository.findAverageBySubject(subject));
    }

    /** Class average per subject, for the teacher dashboard chart. */
    public List<SubjectPerformance> subjectAverages() {
        List<SubjectPerformance> result = new ArrayList<>();
        for (Subject subject : subjectRepository.findAllByOrderByCodeAsc()) {
            double avg = averageForSubject(subject);
            result.add(new SubjectPerformance(subject.getCode(), subject.getName(), avg,
                    Marks.gradeFor((int) Math.round(avg)), 0.0));
        }
        return result;
    }

    static double round(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 10.0) / 10.0;
    }
}
