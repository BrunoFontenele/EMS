package com.brunofontenele.ems.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
	private static Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

	@ExceptionHandler(EmsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public EmsExceptionDto EmsException(EmsException e) {
		return new EmsExceptionDto(e);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public EmsExceptionDto authenticationException(AuthenticationException e) {
		return new EmsExceptionDto(new EmsException(ErrorMessage.INVALID_CREDENTIALS));
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public EmsExceptionDto accessDenied(AccessDeniedException e) {
		return new EmsExceptionDto(new EmsException(ErrorMessage.ACCESS_DENIED));
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public EmsExceptionDto unexpectedException(Exception e) {
		logger.error(e.getMessage(), e);
		return new EmsExceptionDto(e);
	}
}
