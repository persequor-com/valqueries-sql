
package com.valqueries;

public class OrmException extends RuntimeException {
	public OrmException(Exception e) {
		super(e);
	}

	public OrmException(String message) {
		super(message);
	}

	public static class MoreThanOneRowFound extends OrmException {
		public MoreThanOneRowFound(String message) {
			super(message);
		}
	}
}
