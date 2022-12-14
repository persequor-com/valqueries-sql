package com.valqueries.automapper.elements;

public enum Operator {
	IS_NULL,
	IS_NOT_NULL,
	EQUALS,
	LESS_THAN,
	LESS_THAN_OR_EQUALS,
	MORE_THAN,
	MORE_THAN_OR_EQUALS,
	LIKE;

	public boolean isUnary() {
		switch (this) {
			case IS_NULL:
			case IS_NOT_NULL:
				return true;
		}
		return false;
	}
}