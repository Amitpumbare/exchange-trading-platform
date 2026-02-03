package com.example.demo.handler;

import com.example.demo.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(OrderNotFoundException.class)
    protected ResponseEntity<ApiError> handleOrderNotFound(OrderNotFoundException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Order_Not_Found",
                List.of(ex.getMessage()),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(TradeNotFoundException.class)
    protected ResponseEntity<ApiError> handleTradeNotFound(TradeNotFoundException ex) {
        ApiError apiError = new ApiError(
          HttpStatus.NOT_FOUND.value(),
          "Trade_Not_Found",
          List.of(ex.getMessage()),
          Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(OrderAlreadyFilledException.class)
    protected ResponseEntity<ApiError> handleFilled(OrderAlreadyFilledException ex) {
        ApiError apiError = new ApiError(
          HttpStatus.CONFLICT.value(),
          "Order_Already_Filled",
          List.of(ex.getMessage()),
          Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(OrderAlreadyCancelledException.class)
    protected ResponseEntity<ApiError> handleCancelled(OrderAlreadyCancelledException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Order_Already_Cancelled",
                List.of(ex.getMessage()),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(InvalidOrderRequestException.class)
    protected ResponseEntity<ApiError> handleInvalid(InvalidOrderRequestException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Order_Details_Invalid",
                List.of(ex.getMessage()),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation_Failed",
                errors,
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    protected ResponseEntity<ApiError> handleEmailExistsException(EmailAlreadyExistsException ex){
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Email already Exists",
                List.of(ex.getMessage()),
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    protected ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex){
        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid Credentials",
                List.of(ex.getMessage()),
                Instant.now()
        );

        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex){
        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                List.of(ex.getMessage()),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

}

