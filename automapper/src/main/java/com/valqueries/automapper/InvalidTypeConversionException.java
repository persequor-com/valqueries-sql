package com.valqueries.automapper;

public class InvalidTypeConversionException extends RuntimeException {
	public InvalidTypeConversionException(String sqlType, String type) {
		super("Invalid type conversion from: "+sqlType+" to "+type);
	}
}
