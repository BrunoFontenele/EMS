package com.brunofontenele.ems.question.domain;

import com.brunofontenele.ems.exam.domain.Exam;
import com.brunofontenele.ems.subject.domain.Subject;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "question")
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "questionNumber", nullable = false)
	private Integer questionNumber;

    @Column(name = "maxScore", nullable = false)
    private Integer maxScore;

    @Column(name = "filePath", nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

	protected Question() {
	}

	public Question(Integer questionNumber, Integer maxScore, String filePath, Exam exam, Subject subject) {
        this.questionNumber = questionNumber;
        this.maxScore = maxScore;
        this.filePath = filePath;
        this.exam = exam;
        this.subject = subject;
	}
}
