package com.brunofontenele.ems.review.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Data;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.question.domain.Question;

@Data
@Entity
@Table(name = "review")
public class Review {
    public enum ReviewStatus {
        PENDING,  
        IN_REVIEW,
        REVIEWED  
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(length = 1000, nullable = false)
    private String studentJustification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "old_score", nullable = false)
    private Integer oldScore;

    @Column(name = "new_score")
    private Integer newScore = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id")
    private Person assignedTeacher = null;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Review() {}

    public Review(Question question, String studentJustification, Integer oldScore) {
        this.question = question;
        this.studentJustification = studentJustification;
        this.oldScore = oldScore;
    }
}