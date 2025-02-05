package org.demo.document_operations.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;

@Getter
@Setter
@StandardException
public class CustomerException extends Exception {
    private static final long serialVersionUID = 1L;
    private String message;

    public CustomerException(String message) {
        super(message);
        this.message = message;
    }

}
