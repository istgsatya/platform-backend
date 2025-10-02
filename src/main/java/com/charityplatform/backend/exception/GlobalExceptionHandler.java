package com.charityplatform.backend.exception;

import com.charityplatform.backend.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice // This annotation makes it a global exception handler for all controllers
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // This method will handle any unexpected exception that we didn't explicitly catch
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        // Log the full error stack trace for debugging
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        // Return a clear 500 Internal Server Error response to the client
        MessageResponse errorDetails = new MessageResponse(false, "An internal server error occurred: " + ex.getMessage());        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // We can add more specific exception handlers here later if needed
}