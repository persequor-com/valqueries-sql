package com.valqueries.automapper;

import io.ran.Property;
import io.ran.QueryWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ValqueriesUpdateImpl<T> implements ValqueriesUpdate<T> {
	private final T instance;
	private final QueryWrapper queryWrapper;
	List<Property.PropertyValue> propertyValues = new ArrayList<>();
	List<Property.PropertyValue> incrementValues = new ArrayList<>();

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

	@Override
	public <X> void increment(Function<T, X> field, X value) {
		field.apply(instance);
		Property p = queryWrapper.getCurrentProperty();
		this.incrementValues.add(p.value(value));
	}

	public List<Property.PropertyValue> getPropertyValues() {
		return propertyValues;
	}

	public List<Property.PropertyValue> getIncrementValues() {
		return incrementValues;
	}
}
