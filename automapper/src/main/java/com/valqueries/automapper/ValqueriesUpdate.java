package com.valqueries.automapper;

import java.util.Collection;
import java.util.function.Function;

public interface ValqueriesUpdate<T> {
	<X> void set(Function<T, X> field, X value);
}
