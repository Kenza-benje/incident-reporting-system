package com.example.ifraneguard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) // 422
public class InvalidWorkflowTransitionException extends RuntimeException {
    public InvalidWorkflowTransitionException(String message) {
        super(message);
    }
}