package com.smartlab.erp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 权限不足异常
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
