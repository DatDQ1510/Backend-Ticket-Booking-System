package com.example.demo.exception;

import com.example.demo.constants.ErrorCode;
import lombok.Getter;

/**
 * Base exception for token-related errors
 */
@Getter
public class TokenException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public TokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public TokenException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public TokenException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
