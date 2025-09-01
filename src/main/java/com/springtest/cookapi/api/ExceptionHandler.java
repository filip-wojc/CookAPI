package com.springtest.cookapi.api;

import com.springtest.cookapi.domain.responses.ExceptionResponse;
import com.springtest.cookapi.domain.exceptions.ForbiddenException;
import com.springtest.cookapi.domain.exceptions.NotFoundException;
import com.springtest.cookapi.domain.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex){
        var response = new ExceptionResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException ex) {
        var response = new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage().substring(ex.getMessage().lastIndexOf('[') + 1, ex.getMessage().lastIndexOf(']') - 1));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorized(UnauthorizedException ex) {
        var response = new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbidden(ForbiddenException ex) {
        var response = new ExceptionResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
