package org.demo.document_operations.response;

import lombok.RequiredArgsConstructor;
import org.demo.document_operations.exception.CustomerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ControllerAdvice
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleException(Exception e) {
        logger.error(e.getMessage(), e);
        ResponseDTO response = new ResponseDTO();
        response.setResponseMessage("INTERNAL SERVER ERROR");
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<ResponseDTO> handleException(CustomerException e) {
        logger.error(e.getMessage(), e);
        ResponseDTO response = new ResponseDTO();
        response.setResponseMessage(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }


}
