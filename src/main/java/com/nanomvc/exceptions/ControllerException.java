package com.nanomvc.exceptions;

public class ControllerException extends RuntimeException {

    public ControllerException() {
    }

    public ControllerException(String msg) {
        super(msg);
    }
}