// GoalNotFoundException.java
package com.fitter.exception;

import org.springframework.http.HttpStatus;

public class GoalNotFoundException extends CustomException {
  public GoalNotFoundException(String message) {
    super(message, "GOAL_NOT_FOUND", HttpStatus.NOT_FOUND);
  }
}