package com.valqueries.automapper;

import java.util.HashSet;
import java.util.Set;

public class ChangeMonitor {
	int changedRows = 0;
	Set<Integer> alreadySaved = new HashSet<>();

	public int increment(Object object, int changedRows) {
		int identity = System.identityHashCode(object);
		alreadySaved.add(identity);
		return this.changedRows += changedRows;
	}

	public int getNumberOfChangedRows() {
		return changedRows;
	}

	public boolean isAlreadySaved(Object t) {
		int identity = System.identityHashCode(t);
		return alreadySaved.contains(identity);
	}
}
