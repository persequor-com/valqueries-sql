package com.valqueries.automapper;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ValqueriesGroupQuery<T> {
	GroupNumericResult count(Function<T, ?> field);
	GroupNumericResult count(BiConsumer<T, ?> field);

	GroupNumericResult sum(Function<T, ?> field);
	GroupNumericResult sum(BiConsumer<T, ?> field);

	GroupNumericResult max(Function<T, ?> field);
	GroupNumericResult max(BiConsumer<T, ?> field);

	GroupNumericResult min(Function<T, ?> field);
	GroupNumericResult min(BiConsumer<T, ?> field);

	GroupStringResult concat(Function<T, ?> field, String separator);
	GroupStringResult concat(BiConsumer<T, ?> field, String separator);
}
