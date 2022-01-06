package com.valqueries.automapper;

import java.util.List;
import java.util.Set;

public interface GroupNumericResult {
	int size();
	int get(Object... groupValues);

	List<List<Object>> keys();
}
