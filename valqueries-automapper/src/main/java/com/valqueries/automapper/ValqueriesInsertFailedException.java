package com.valqueries.automapper;

public class ValqueriesInsertFailedException extends Exception{
	public ValqueriesInsertFailedException(Exception e) {
		super(e);
	}

	public ValqueriesInsertFailedException(String message) {
		super(message);
	}
}
