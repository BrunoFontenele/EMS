package com.brunofontenele.ems.exam.domain;

import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.school.domain.School;
import com.brunofontenele.ems.subject.domain.Subject;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exam")
public class Exam {

    public enum ExamStatus {
        UPLOAD_IN_PROGRESS,
        IN_REVIEW,
        CLOSED,
        RELEASED;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "finalScore", nullable = true)
    private Integer finalScore;

    @Column(name = "viewRequested", nullable = false)
    private boolean viewRequested = false;

    @Column(name = "releaseDate", nullable = true)
    private LocalDateTime releaseDate;

    @Column(name = "filePath", nullable = false)
	private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExamStatus status = ExamStatus.UPLOAD_IN_PROGRESS;

    @ManyToOne(fetch = FetchType.LAZY)  
    @JoinColumn(name = "student_id", nullable = false)
    private Person student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

	protected Exam() {
	}

	public Exam(String name, Person student, School school, Subject subject) {
        this.filePath = name;
        this.student = student;
        this.school = school;
        this.subject = subject;
	}
}
