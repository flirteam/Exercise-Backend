// DuplicateResourceException.java
package com.fitter.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends CustomException {
  public DuplicateResourceException(String message) {
    super(message, "DUPLICATE_RESOURCE", HttpStatus.CONFLICT);
  }
}