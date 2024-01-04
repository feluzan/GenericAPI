package io.github.feluzan.GenericAPI.excepction;

public class JsonLoopResolverException extends RuntimeException {

    public JsonLoopResolverException() {
        this("There was a problem with JsonLopResolver");
    }

    public JsonLoopResolverException(String message) {
        this(message, null);
    }

    public JsonLoopResolverException(String message, Throwable cause) {
        super(message, cause);
    }

}