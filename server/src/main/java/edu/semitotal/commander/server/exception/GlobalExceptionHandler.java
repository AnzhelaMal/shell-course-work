package edu.semitotal.commander.server.exception;

import edu.semitotal.commander.server.fileops.operation.OperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchFileException.class)
    public ProblemDetail handleNoSuchFile(NoSuchFileException e) {
        log.warn("File not found: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
            "File or directory not found: " + e.getFile());
        problem.setTitle("File Not Found");
        return problem;
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ProblemDetail handleFileAlreadyExists(FileAlreadyExistsException e) {
        log.warn("File already exists: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "File or directory already exists: " + e.getFile());
        problem.setTitle("File Already Exists");
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
            "Access denied: " + e.getFile());
        problem.setTitle("Access Denied");
        return problem;
    }

    @ExceptionHandler(IOException.class)
    public ProblemDetail handleIOException(IOException e) {
        log.error("IO exception", e);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "I/O error occurred: " + e.getMessage());
        problem.setTitle("I/O Error");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid Request");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        log.error("Unhandled exception", e);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + e.getMessage());
        problem.setTitle("Internal Server Error");
        return problem;
    }

    @ExceptionHandler(OperationException.class)
    public ProblemDetail handleOperationException(OperationException e) {
        var cause = e.getCause();
        return switch (cause) {
            case AccessDeniedException accessDenied -> handleAccessDenied(accessDenied);
            case FileAlreadyExistsException fileExists -> handleFileAlreadyExists(fileExists);
            case NoSuchFileException noSuchFile -> handleNoSuchFile(noSuchFile);
            case IOException ioException -> handleIOException(ioException);
            case null, default -> {
                // Generic operation error
                log.error("Operation failed", e);
                var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage());
                problem.setTitle("Operation Failed");
                yield  problem;
            }
        };
    }

}
