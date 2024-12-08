// ExerciseNotFoundException.java
package com.fitter.exception;

import org.springframework.http.HttpStatus;

public class ExerciseNotFoundException extends CustomException {
  public ExerciseNotFoundException(String message) {
    super(message, "EXERCISE_NOT_FOUND", HttpStatus.NOT_FOUND);
  }
}