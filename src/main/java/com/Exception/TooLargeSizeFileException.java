package com.Exception;

public class TooLargeSizeFileException extends RuntimeException {

    public TooLargeSizeFileException() {
    }

    public TooLargeSizeFileException(String message) {
        super(message);
    }
}
