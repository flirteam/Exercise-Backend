// InvalidJwtAuthenticationException.java
package com.fitter.exception;

import org.springframework.http.HttpStatus;

public class InvalidJwtAuthenticationException extends CustomException {
    public InvalidJwtAuthenticationException(String message) {
        super(message, "INVALID_JWT", HttpStatus.UNAUTHORIZED);
    }
}