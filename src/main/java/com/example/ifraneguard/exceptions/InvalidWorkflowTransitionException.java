package com.example.ifraneguard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting an illegal status transition.
 * e.g., SUBMITTED → IN_PROGRESS (skipping UNDER_REVIEW and ASSIGNED)
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) // 422
public class InvalidWorkflowTransitionException extends RuntimeException {
    public InvalidWorkflowTransitionException(String message) {
        super(message);
    }
}