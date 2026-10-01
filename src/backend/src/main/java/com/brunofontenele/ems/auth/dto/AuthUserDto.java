package com.brunofontenele.ems.auth.dto;

import java.util.List;

public record AuthUserDto(
		long id,
		String name,
		String email,
		String role,
		List<String> permissions,
		boolean impersonating,
		String impersonatorEmail) {
}
