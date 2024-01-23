package com.example.demo.exceptionHandler;

@SuppressWarnings("serial")
public class GlobalExceptionHandler extends RuntimeException {

    public GlobalExceptionHandler(String msg) {

        super(msg);

    }
}
