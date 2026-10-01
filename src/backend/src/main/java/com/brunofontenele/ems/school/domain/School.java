package com.brunofontenele.ems.school.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "school")
public class School {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "name", nullable = false)
	private String name;

    @Column(name = "code", nullable = false, unique = true)
	private String code;

    @Column(name = "region", nullable = false)
	private String region;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	protected School() {
	}

	public School(String name, String code, String region) {
		this.name = name;
		this.code = code;
		this.region = region;
	}
}
