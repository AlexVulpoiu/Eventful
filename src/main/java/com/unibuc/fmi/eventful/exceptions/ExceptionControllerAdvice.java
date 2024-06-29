package com.unibuc.fmi.eventful.exceptions;

import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<String> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<String> handleInvalidField(MethodArgumentNotValidException ex) {
        String invalidFields = "Invalid fields: \n"
                + ex.getBindingResult().getFieldErrors().stream()
                .map(e -> "Field: " + e.getField() + ", error: " + e.getDefaultMessage() + ", value: " + e.getRejectedValue())
                .collect(Collectors.joining("\n"));
        return new ResponseEntity<>(invalidFields, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<String> handleMaxUploadSize() {
        return new ResponseEntity<>("You can't upload files larger than 10 MB! Please try again with another image.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<String> handleUnauthorizedException() {
        return new ResponseEntity<>("Unauthorized! Please log in and try again!", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<String> handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<String> handleAccessDeniedException() {
        return new ResponseEntity<>("You are not allowed to perform this action!", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({PaymentException.class, StripeException.class})
    public ResponseEntity<String> handlePaymentException() {
        return new ResponseEntity<>("An error occurred during payment processing!", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({Throwable.class})
    public ResponseEntity<String> handleException() {
        return new ResponseEntity<>("An unexpected error occurred! Please try again or contact platform admin!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
