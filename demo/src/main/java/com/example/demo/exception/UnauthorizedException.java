package com.example.demo.exception;

import com.example.demo.constants.ErrorCode;

/**
 * Exception thrown when authentication fails
 */
public class UnauthorizedException extends TokenException {
    
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
