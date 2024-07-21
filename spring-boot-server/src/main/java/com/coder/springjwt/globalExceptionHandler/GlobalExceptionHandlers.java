package com.coder.springjwt.globalExceptionHandler;

import com.coder.springjwt.exception.customerException.InvalidMobileNumberException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandlers {

    @ExceptionHandler(InvalidMobileNumberException.class)
    public ResponseEntity<?> invalidMobileNumberException(InvalidMobileNumberException invalidMobileNumberException)
    {
        String message = invalidMobileNumberException.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(message);
        exceptionResponse.setDateTime(LocalDateTime.now());
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
        return responseEntity;
    }
}
