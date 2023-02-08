package com.valqueries.automapper;

public class ValqueriesException extends Exception {
    public ValqueriesException(Exception e) {
        super(e);
    }

    public ValqueriesException(String message) {
        super(message);
    }
}
