package com.valqueries.automapper.elements;

import com.valqueries.Setter;

public interface Element extends Setter {
	String queryString();

	default String fromString() {
		return null;
	}
}
