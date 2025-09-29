package com.airfranceklm.fasttrack.assignment.exception;


import org.springframework.http.HttpStatus;


public class BusinessRuleViolationException extends RuntimeException {
    private final HttpStatus status;


    public BusinessRuleViolationException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }


    public BusinessRuleViolationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public HttpStatus getStatus() { return status; }
}