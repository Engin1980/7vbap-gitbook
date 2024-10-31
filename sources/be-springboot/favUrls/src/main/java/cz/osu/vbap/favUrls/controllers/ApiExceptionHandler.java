package cz.osu.vbap.favUrls.controllers;

import cz.osu.vbap.favUrls.controllers.dto.ErrorView;
import cz.osu.vbap.favUrls.controllers.exceptions.ForbiddenException;
import cz.osu.vbap.favUrls.services.exceptions.BadDataException;
import cz.osu.vbap.favUrls.services.exceptions.BadRequestException;
import cz.osu.vbap.favUrls.services.exceptions.InternalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorView> badRequestException(BadRequestException e, WebRequest request) {
    ResponseEntity<ErrorView> ret = new ResponseEntity<>(
            new ErrorView(e.getMessage()),
            HttpStatus.BAD_REQUEST);
    return ret;
  }

  @ExceptionHandler(BadDataException.class)
  public ResponseEntity<ErrorView> badDataException(BadDataException e, WebRequest request) {
    ResponseEntity<ErrorView> ret = new ResponseEntity<>(
            new ErrorView(e.getMessage()),
            HttpStatus.BAD_REQUEST);
    return ret;
  }

  @ExceptionHandler(InternalException.class)
  public ResponseEntity<Error> internalServerException(InternalException e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Internal service error."),
            HttpStatus.INTERNAL_SERVER_ERROR
    );
    return ret;
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Error> exception(NoResourceFoundException e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Invalid request - path.", null),
            HttpStatus.NOT_FOUND
    );
    return ret;
  }

  @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Error> exception(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Invalid request - data.", null),
            HttpStatus.NOT_FOUND
    );
    return ret;
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> exception(Exception e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Internal server error", null),
            HttpStatus.INTERNAL_SERVER_ERROR
    );
    return ret;
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<Error> exception(ForbiddenException e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Forbidden", null),
            HttpStatus.FORBIDDEN
    );
    return ret;
  }
}
