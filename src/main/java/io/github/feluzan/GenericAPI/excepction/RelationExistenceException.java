package io.github.feluzan.GenericAPI.excepction;

public class RelationExistenceException extends RuntimeException {

    public RelationExistenceException() {
        this("There was a problem with CheckRelationExistence");
    }

    public RelationExistenceException(String message) {
        this(message, null);
    }

    public RelationExistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
