package com.brunofontenele.ems.subject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.brunofontenele.ems.subject.domain.Subject;

public record SubjectDto(
		long id,

		@NotBlank(message = "O nome é obrigatório")
		String name,

		@NotBlank(message = "O código é obrigatório")
		String code,

		@NotNull(message = "O estado é obrigatório")
		boolean active) {
	public SubjectDto(Subject subject) {
		this(subject.getId(), subject.getName(), subject.getCode(), subject.isActive());
	}
}
