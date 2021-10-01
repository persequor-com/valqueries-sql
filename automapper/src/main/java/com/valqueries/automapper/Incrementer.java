package com.valqueries.automapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Incrementer {
	int i = 0;
	Set<Integer> alreadySaved = new HashSet<>();

	public int increment(Object object, int by) {
		int identity = System.identityHashCode(object);
		alreadySaved.add(identity);
		return i += by;
	}

	public int  value() {
		return i;
	}

	public boolean isAlreadySaved(Object t) {
		int identity = System.identityHashCode(t);
		return alreadySaved.contains(identity);
	}
}
