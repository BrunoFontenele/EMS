package com.brunofontenele.ems.correction.domain;

import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.question.domain.Question;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "correction")
public class Correction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Person professor;

    private Integer score = null;

	protected Correction() {
	}

	public Correction(Question question, Person professor, Integer score) {
        this.question = question;
        this.professor = professor;
        this.score = score;
	}
}
