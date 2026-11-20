package com.college.sms.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit tests for the grade calculation (no Spring, no database).
 */
class MarksTest {

    @Test
    void gradeBoundariesAreCorrect() {
        assertThat(Marks.gradeFor(95)).isEqualTo("A+");
        assertThat(Marks.gradeFor(90)).isEqualTo("A+");
        assertThat(Marks.gradeFor(89)).isEqualTo("A");
        assertThat(Marks.gradeFor(75)).isEqualTo("B");
        assertThat(Marks.gradeFor(60)).isEqualTo("C");
        assertThat(Marks.gradeFor(50)).isEqualTo("D");
        assertThat(Marks.gradeFor(49)).isEqualTo("F");
        assertThat(Marks.gradeFor(0)).isEqualTo("F");
    }

    @Test
    void passMarkIsFifty() {
        assertThat(new Marks(null, null, 50).isPassed()).isTrue();
        assertThat(new Marks(null, null, 49).isPassed()).isFalse();
    }

    @Test
    void studentInitialsAreBuiltFromFirstAndLastName() {
        Student student = new Student("STU1", "rahul kumar sharma", "r@x.com", "CSE", 1, "9999999999");
        assertThat(student.getInitials()).isEqualTo("RS");
    }
}
