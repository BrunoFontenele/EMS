package com.brunofontenele.ems.person.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import com.brunofontenele.ems.person.domain.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PersonDto(
		Long id,

		@NotBlank(message = "O nome é obrigatório")
		String name,

		@NotBlank(message = "O email é obrigatório") 
		@Email(message = "O email deve ser válido") 
		String email,

		@JsonProperty(access = Access.WRITE_ONLY)
		String password,

		@NotNull(message = "O tipo é obrigatório") 
		Person.PersonType type,

		String schoolCode,
		
		String subjectCode,

		Boolean active
		) {

	public PersonDto(Person person) {
		this(person.getId(), person.getName(), person.getEmail(), null, person.getType(),
				person.getSchool() != null ? person.getSchool().getCode() : null,
				person.getSubject() != null ? person.getSubject().getCode() : null,
				person.isActive());
	}
}
