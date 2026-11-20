package com.college.sms.service;

import com.college.sms.entity.Subject;
import com.college.sms.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> findAll() {
        return subjectRepository.findAllByOrderByCodeAsc();
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id " + id));
    }

    /** Returns the requested subject, or the first subject when no id is given. Null if none exist. */
    public Subject findOrFirst(Long id) {
        if (id != null) {
            return findById(id);
        }
        List<Subject> all = findAll();
        return all.isEmpty() ? null : all.get(0);
    }

    public long count() {
        return subjectRepository.count();
    }
}
