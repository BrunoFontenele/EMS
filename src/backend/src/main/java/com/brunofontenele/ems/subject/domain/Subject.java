package com.brunofontenele.ems.subject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subject")
public class Subject {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "name", nullable = false)
	private String name;

    @Column(name = "code", nullable = false, unique = true)
	private String code;

    @Column(name = "active", nullable = false)
	private boolean active = true;

	protected Subject() {
	}

	public Subject(String name, String code) {
		this.name = name;
		this.code = code;
	}
}
