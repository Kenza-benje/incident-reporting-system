package com.example.ifraneguard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested incident doesn't exist in the database.
 * @ResponseStatus makes Spring automatically return 404 if this is thrown from a controller.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class IncidentNotFoundException extends RuntimeException {
  public IncidentNotFoundException(String message) {
    super(message);
  }
}