package com.brunofontenele.ems.auth.dto;

public record LoginResponse(
		String token,
		long expiresInMs,
		AuthUserDto user) {
}
