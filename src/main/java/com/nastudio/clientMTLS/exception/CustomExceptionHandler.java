package com.nastudio.clientMTLS.exception;

import com.nastudio.clientMTLS.dto.responses.DataApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.apache.hc.core5.http.StreamClosedException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.UnknownHostException;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DataApiResponse> returnPostGresException(DataIntegrityViolationException e) {
        String message = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        var response = DataApiResponse.builder().success(false).data(message).message(HttpStatus.INTERNAL_SERVER_ERROR.name()).statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        return ResponseEntity.internalServerError().body(response);
    }


    @ExceptionHandler(CertificateNotExistException.class)
    public ResponseEntity<DataApiResponse> certificateNotExistException(CertificateNotExistException e) {
        var response = DataApiResponse.builder().success(false).data(e.getMessage()).message(HttpStatus.BAD_REQUEST.name()).statusCode(HttpStatus.BAD_REQUEST.value()).build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<DataApiResponse> badRequestException(BadRequestException e) {
        var response = DataApiResponse.builder().success(false).data(e.getMessage()).message(HttpStatus.BAD_REQUEST.name()).statusCode(HttpStatus.BAD_REQUEST.value()).build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(StreamClosedException.class)
    public ResponseEntity<DataApiResponse> streamClosedException(StreamClosedException e) {
        var response = DataApiResponse.builder().success(false).data(e.getMessage()).message(HttpStatus.BAD_REQUEST.name()).statusCode(HttpStatus.BAD_REQUEST.value()).build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<DataApiResponse> nullPointerException(NullPointerException e) {
        var response = DataApiResponse.builder().success(false).data(e.getMessage()).message(HttpStatus.BAD_REQUEST.name()).statusCode(HttpStatus.BAD_REQUEST.value()).build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<DataApiResponse> unknownHostException(UnknownHostException e) {
        log.error("[EXCEPTION] :: [CAUSE] : {} && [MESSAGE] : {}", e.getCause(), e.getMessage(),e);
        var response = DataApiResponse.builder().success(false).data("Failed To Process An Action Internally.").message(HttpStatus.INTERNAL_SERVER_ERROR.name()).statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        return ResponseEntity.internalServerError().body(response);
    }


}
