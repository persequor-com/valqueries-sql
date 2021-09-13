package com.valqueries.automapper;

import io.ran.CrudRepoBaseQuery;
import io.ran.CrudRepository;
import io.ran.GenericFactory;
import io.ran.Property;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseValqueriesQuery<T> extends CrudRepoBaseQuery<T, ValqueriesQuery<T>> implements ValqueriesQuery<T> {

	public BaseValqueriesQuery(Class<T> clazz, GenericFactory genericFactory) {
		super(clazz, genericFactory);
	}

	abstract ValqueriesQuery<T> in(Property.PropertyValueList propertyValues);
	abstract ValqueriesQuery<T> like(Property.PropertyValue<?> propertyValue);
	abstract ValqueriesQuery<T> freetext(Property.PropertyValue<?> propertyValue);
	abstract ValqueriesQuery<T> isNotNull(Property<?> property);

	@Override
	public <X> ValqueriesQuery<T> in(Function<T, X> field, Collection<X> value) {
		field.apply(instance);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> in(BiConsumer<T, X> field, Collection<X> value) {
		field.accept(instance, null);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> in(Function<T, X> field, X... value) {
		field.apply(instance);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> in(BiConsumer<T, X> field, X... value) {
		field.accept(instance, null);
		in(queryWrapper.getCurrentProperty().values(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> like(Function<T, String> field, String value) {
		field.apply(instance);
		like(queryWrapper.getCurrentProperty().value(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> like(BiConsumer<T, String> field, String value) {
		field.accept(instance, null);
		like(queryWrapper.getCurrentProperty().value(value));
		return this;
	}


	@Override
	public ValqueriesQuery<T> freetext(Function<T, String> field, String value) {
		field.apply(instance);
		freetext(queryWrapper.getCurrentProperty().value(value));
		return this;
	}

	@Override
	public ValqueriesQuery<T> isNotNull(BiConsumer<T, String> field) {
		field.accept(instance, null);
		isNotNull(queryWrapper.getCurrentProperty());
		return this;
	}

	@Override
	public ValqueriesQuery<T> isNotNull(Function<T, String> field) {
		field.apply(instance);
		isNotNull(queryWrapper.getCurrentProperty());
		return this;
	}

	@Override
	public ValqueriesQuery<T> freetext(BiConsumer<T, String> field, String value) {
		field.accept(instance, null);
		freetext(queryWrapper.getCurrentProperty().value(value));
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQuery(Function<T, X> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.apply(instance);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQuery(BiConsumer<T, X> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.accept(instance, null);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQueryList(Function<T, List<X>> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.apply(instance);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}

	@Override
	public <X> ValqueriesQuery<T> subQueryList(BiConsumer<T, List<X>> field, Consumer<ValqueriesQuery<X>> subQuery) {
		field.accept(instance, null);
		this.subQuery(typeDescriber.relations().get(queryWrapper.getCurrentProperty().getToken().snake_case()), subQuery);
		return this;
	}
}
