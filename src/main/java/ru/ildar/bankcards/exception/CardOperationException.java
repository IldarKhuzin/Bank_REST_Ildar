package ru.ildar.bankcards.exception;

public class CardOperationException extends RuntimeException {
    public CardOperationException() {
        super();
    }

    public CardOperationException(String message) {
        super(message);
    }

    public CardOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
