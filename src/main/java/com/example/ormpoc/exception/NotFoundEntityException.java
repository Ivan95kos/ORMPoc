package com.example.ormpoc.exception;

public class NotFoundEntityException extends RuntimeException {

    public NotFoundEntityException(String message, Throwable e) {
        super(message, e);
    }

}
