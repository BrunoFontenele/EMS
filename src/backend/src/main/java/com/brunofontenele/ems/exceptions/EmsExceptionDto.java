package com.brunofontenele.ems.exceptions;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class EmsExceptionDto {
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp;
	private String message;
	private String debugMessage;
	private int code;

	public EmsExceptionDto(EmsException e) {
		this.timestamp = LocalDateTime.now();
		this.message = e.getMessage();
		this.code = e.getCode();
	}

	public EmsExceptionDto(Throwable e) {
		this.timestamp = LocalDateTime.now();
		this.message = "Unexpected error";
		this.debugMessage = e.getLocalizedMessage();
	}

	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public String getMessage() {
		return this.message;
	}

	public String getDebugMessage() {
		return this.debugMessage;
	}

	public int getCode() {
		return this.code;
	}

}
