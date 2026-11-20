package com.college.sms.service;

import com.college.sms.entity.Announcement;
import com.college.sms.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<Announcement> findAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Announcement> findRecent() {
        return announcementRepository.findTop3ByOrderByCreatedAtDesc();
    }

    public Announcement create(Announcement announcement, String postedBy) {
        announcement.setId(null);
        announcement.setTitle(announcement.getTitle().trim());
        announcement.setMessage(announcement.getMessage().trim());
        announcement.setPostedBy(postedBy);
        announcement.setCreatedAt(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }

    public void delete(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new IllegalArgumentException("Announcement not found");
        }
        announcementRepository.deleteById(id);
    }
}
