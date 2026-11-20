package com.college.sms.entity;

/**
 * The two kinds of users in the system.
 * Spring Security turns these into the authorities ROLE_TEACHER and ROLE_STUDENT.
 */
public enum Role {
    TEACHER,
    STUDENT
}
