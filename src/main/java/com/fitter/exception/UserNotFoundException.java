// UserNotFoundException.java
package com.fitter.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomException {
  public UserNotFoundException(String message) {
    super(message, "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
  }
}