package io.github.feluzan.GenericAPI.excepction;

public class InvalidFilterException  extends RuntimeException {

    public InvalidFilterException() {
        this("There was a problem with Filter");
    }

    public InvalidFilterException(String message) {
        this(message, null);
    }

    public InvalidFilterException(String message, Throwable cause) {
        super(message, cause);
    }

}