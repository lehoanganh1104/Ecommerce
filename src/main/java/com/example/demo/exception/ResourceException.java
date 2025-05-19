package com.example.demo.exception;

import com.example.demo.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResourceException {
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex){
        ErrException errException = ex.getErrException();
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setCode(errException.getCode());
        apiResponse.setMessage(errException.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> checkValidation(MethodArgumentNotValidException ex){
        String message = "Validation failed";
        if (ex.getFieldError() != null && ex.getFieldError().getDefaultMessage() != null) {
            message = ex.getFieldError().getDefaultMessage();
        }
        ErrException err = ErrException.INVALID_KEY;
        try {
            err = ErrException.valueOf(message);
        } catch (IllegalArgumentException ignored) {
        }
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setCode(err.getCode());
        apiResponse.setMessage(err == ErrException.INVALID_KEY ? message : err.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
            String invalidValue = invalidFormatException.getValue().toString();
            String targetType = invalidFormatException.getTargetType().getSimpleName();

            if ("Role".equals(targetType)) {
                apiResponse.setCode(ErrException.USER_INVALID_ROLE.getCode());
                apiResponse.setMessage("Invalid value '" + invalidValue + "' for role");
            } else {
                apiResponse.setCode(ErrException.ERR_EXCEPTION.getCode());
                apiResponse.setMessage("Invalid value '" + invalidValue + "' for type " + targetType);
            }
        } else {
            apiResponse.setCode(ErrException.ERR_EXCEPTION.getCode());
            apiResponse.setMessage("Malformed JSON request");
        }
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
