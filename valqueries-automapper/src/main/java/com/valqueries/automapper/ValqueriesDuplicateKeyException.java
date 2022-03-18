package com.valqueries.automapper;

public class ValqueriesDuplicateKeyException extends Exception{
	public ValqueriesDuplicateKeyException(Exception e) {
		super(e);
	}

	public ValqueriesDuplicateKeyException(String message) {
		super(message);
	}
}
