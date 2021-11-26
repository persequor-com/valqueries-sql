package com.valqueries.automapper;

import io.ran.Property;
import io.ran.QueryWrapper;

import java.util.function.Function;

public class ValqueriesUpdateImpl<T> implements ValqueriesUpdate<T> {
	private final T instance;
	private final QueryWrapper queryWrapper;
	Property.PropertyValueList propertyValues = new Property.PropertyValueList();

	public ValqueriesUpdateImpl(T instance, QueryWrapper queryWrapper) {
		this.instance = instance;
		this.queryWrapper = queryWrapper;
	}

	@Override
	public <X> void set(Function<T, X> field, X value) {
		field.apply(instance);
		Property p = queryWrapper.getCurrentProperty();
		this.propertyValues.add(p.value(value));
	}

	public Property.PropertyValueList getPropertyValues() {
		return propertyValues;
	}
}
