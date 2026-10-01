package com.brunofontenele.ems.person.domain;

import com.brunofontenele.ems.school.domain.School;
import com.brunofontenele.ems.subject.domain.Subject;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "people")
public class Person {

	public enum PersonType {
		ADMINISTRATOR,
		SCHOOL_STAFF,
		TEACHER,
		STUDENT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	// Login identifier, unique across all accounts.
	@Column(name = "email", nullable = false, unique = true)
	private String email;

	// BCrypt hash of the password; excluded from toString so it is never logged.
	@ToString.Exclude
	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
    private PersonType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "school_id", nullable = true)
	private School school;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id", nullable = true)
	private Subject subject;

	@Column(name = "active", nullable = true)
	private boolean active = true;

	protected Person() {
	}

	public Person(String name, String email, String password, PersonType type, School school, Subject subject) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.type = type;
		this.school = school;
		this.subject = subject;
	}
}
