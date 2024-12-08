// InvalidRequestException.java
package com.fitter.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends CustomException {
  public InvalidRequestException(String message) {
    super(message, "INVALID_REQUEST", HttpStatus.BAD_REQUEST);
  }
}