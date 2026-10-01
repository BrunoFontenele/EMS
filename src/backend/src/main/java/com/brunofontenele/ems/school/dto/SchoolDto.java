package com.brunofontenele.ems.school.dto;

import com.brunofontenele.ems.school.domain.School;

import jakarta.validation.constraints.NotBlank;

public record SchoolDto(
		long id,

		@NotBlank(message = "O nome é obrigatório")
		String name,

		@NotBlank(message = "O código é obrigatório")
		String code,

		@NotBlank(message = "A região é obrigatória")
		String region,

		boolean active) {
	public SchoolDto(School school) {
		this(school.getId(), school.getName(), school.getCode(), school.getRegion(), school.isActive());
	}
}
