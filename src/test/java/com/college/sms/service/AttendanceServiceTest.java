package com.college.sms.service;

import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AttendanceServiceTest {

    @Test
    void percentageIsRoundedToOneDecimal() {
        assertThat(AttendanceService.percentage(17, 20)).isEqualTo(85.0);
        assertThat(AttendanceService.percentage(2, 3)).isEqualTo(66.7);
        assertThat(AttendanceService.percentage(5, 5)).isEqualTo(100.0);
    }

    @Test
    void percentageIsZeroWhenNoClassesHeld() {
        assertThat(AttendanceService.percentage(0, 0)).isEqualTo(0.0);
    }

    @Test
    void attendanceCannotBeMarkedForFutureDate() {
        AttendanceService service = new AttendanceService(
                mock(AttendanceRepository.class), mock(StudentRepository.class), mock(SubjectRepository.class));

        assertThatThrownBy(() -> service.saveAttendance(1L, LocalDate.now().plusDays(1), Map.of(1L, true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }
}
