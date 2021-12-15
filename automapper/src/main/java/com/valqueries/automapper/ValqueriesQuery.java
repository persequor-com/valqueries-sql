package com.valqueries.automapper;

import io.ran.CrudRepository;
import io.ran.RelationDescriber;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public interface ValqueriesQuery<T> extends CrudRepository.InlineQuery<T, ValqueriesQuery<T>> {
	<X> ValqueriesQuery<T> in(Function<T, X> field, Collection<X> value);
	<X> ValqueriesQuery<T> in(BiConsumer<T, X> field, Collection<X> value);
	<X> ValqueriesQuery<T> in(Function<T, X> field, X... value);
	<X> ValqueriesQuery<T> in(BiConsumer<T, X> field, X... value);
	ValqueriesQuery<T> like(Function<T, String> field, String value);
	ValqueriesQuery<T> like(BiConsumer<T, String> field, String value);
	ValqueriesQuery<T> isNotNull(Function<T, String> field);
	ValqueriesQuery<T> isNotNull(BiConsumer<T, String> field);
	ValqueriesQuery<T> freetext(Function<T, String> field, String value);
	ValqueriesQuery<T> freetext(BiConsumer<T, String> field, String value);
	<X> ValqueriesQuery<T> subQuery(Function<T, X> field, Consumer<ValqueriesQuery<X>> subQuery);
	<X> ValqueriesQuery<T> subQuery(BiConsumer<T, X> field, Consumer<ValqueriesQuery<X>> subQuery);
	<X> ValqueriesQuery<T> subQueryList(Function<T, List<X>> field, Consumer<ValqueriesQuery<X>> subQuery);
	<X> ValqueriesQuery<T> subQueryList(BiConsumer<T, List<X>> field, Consumer<ValqueriesQuery<X>> subQuery);

	ValqueriesGroupQuery<T> groupBy(Function<T, ?>... field);
	ValqueriesGroupQuery<T> groupBy(BiConsumer<T, ?>... field);
}
