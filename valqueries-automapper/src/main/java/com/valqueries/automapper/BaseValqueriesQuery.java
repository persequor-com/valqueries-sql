package com.valqueries.automapper;

import io.ran.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class BaseValqueriesQuery<T> extends CrudRepoBaseQuery<T, ValqueriesQuery<T>> implements ValqueriesQuery<T>, ValqueriesGroupQuery<T> {
	protected List<Property> groupByProperties = null;

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
		final Class<?> fieldType = this.typeDescriber.fields().get(queryWrapper.getCurrentProperty().getToken()).getType().clazz;

		if (value != null && value.length > 0 && !fieldType.isAssignableFrom(value[0].getClass())) {
			//it can happen for instance if we provide Collection of the type mismatching the field type then JVM would infer X as an object and make it the first element of the vararg array
			throw new IllegalArgumentException("The type of the values is incorrectly inferred,"
					+ " please make sure field type matches the type of the values array. "
					+ String.format("Field type is '%s', value type is '%s'.", fieldType, value[0].getClass()));
		}

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
	public ValqueriesGroupQuery<T> groupBy(Function<T, ?>... field) {
		groupByProperties = new ArrayList<>();
		for (Function<T, ?> f : field) {
			f.apply(instance);
			groupByProperties.add(queryWrapper.getCurrentProperty().copy());
		}
		return this;
	}

	@Override
	public ValqueriesGroupQuery<T> groupBy(BiConsumer<T, ?>... field) {
		groupByProperties = new ArrayList<>();
		for (BiConsumer<T, ?> f : field) {
			f.accept(instance, null);
			groupByProperties.add(queryWrapper.getCurrentProperty().copy());
		}
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

	@Override
	public <X> ValqueriesQuery<T> subQueryList(RelationDescriber relationDescriber, Consumer<ValqueriesQuery<X>> subQuery) {
		this.subQuery(relationDescriber, subQuery);
		return this;
	}

	@Override
	public GroupNumericResult count(Function<T, ?> field) {
		field.apply(instance);
		return count(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult count(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return count(queryWrapper.getCurrentProperty().copy());
	}

	protected abstract GroupNumericResult count(Property resultProperty);

	@Override
	public GroupNumericResult sum(Function<T, ?> field) {
		field.apply(instance);
		return sum(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult sum(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return sum(queryWrapper.getCurrentProperty().copy());
	}

	protected abstract GroupNumericResult sum(Property resultProperty);

	@Override
	public GroupNumericResult max(Function<T, ?> field) {
		field.apply(instance);
		return max(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult max(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return max(queryWrapper.getCurrentProperty().copy());
	}

	protected abstract GroupNumericResult max(Property resultProperty);

	@Override
	public GroupNumericResult min(Function<T, ?> field) {
		field.apply(instance);
		return min(queryWrapper.getCurrentProperty().copy());
	}

	@Override
	public GroupNumericResult min(BiConsumer<T, ?> field) {
		field.accept(instance, null);
		return min(queryWrapper.getCurrentProperty().copy());
	}

	protected abstract GroupNumericResult min(Property resultProperty);
}
