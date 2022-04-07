package com.valqueries.automapper;

import java.util.List;

public interface GroupStringResult {
	int size();
	String get(Object... groupValues);
	List<List<Object>> keys();
}
