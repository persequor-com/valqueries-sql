package com.valqueries.automapper;

public class ValqueriesInsertFailedException extends ValqueriesException {
	public ValqueriesInsertFailedException(Exception e) {
		super(e);
	}

	public ValqueriesInsertFailedException(String message) {
		super(message);
	}
}
