package com.valqueries.automapper;

import com.valqueries.IStatement;
import com.valqueries.Setter;
import io.ran.KeySet;
import io.ran.MappingHelper;
import io.ran.ObjectMapColumnizer;
import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PropertyGetter<FROM> implements ObjectMapColumnizer, Setter {
	Map<String, Object> values = new HashMap<>();
	Map<String, String> propertyMap = new HashMap<>();
	List<Property.PropertyValue> propertyValues;

	public PropertyGetter(KeySet properties, KeySet toProperties, FROM from, TypeDescriber<FROM> typeDescriber, MappingHelper mappingHelper) {
		for(int x=0;x<properties.size();x++) {
			propertyMap.put(properties.get(x).getToken().snake_case(), toProperties.get(x).getToken().snake_case());
		}

		mappingHelper.columnize(from, this);
		propertyValues = new ArrayList<>();
	}

	public List<Property.PropertyValue> getValues() {
		return propertyValues;
	}

	@Override
	public void set(IStatement statement) {
		values.forEach(statement::set);
	}

	private void put(String key, Object value) {
		if (propertyMap.containsKey(key)) {
			values.put(propertyMap.get(key), value);
		}
	}
	@Override
	public void set(Property token, String s) {
		put(token.getSnakeCase(), s);
	}

	@Override
	public void set(Property token, Character character) {
		put(token.getSnakeCase(), character);
	}

	@Override
	public void set(Property token, ZonedDateTime zonedDateTime) {
		put(token.getSnakeCase(), zonedDateTime);
	}

	@Override
	public void set(Property token, LocalDateTime localDateTime) {
		put(token.getSnakeCase(), localDateTime);
	}

	@Override
	public void set(Property token, Instant instant) {
		put(token.getSnakeCase(), instant);
	}

	@Override
	public void set(Property token, LocalDate localDate) {
		put(token.getSnakeCase(), localDate);
	}

	@Override
	public void set(Property token, LocalTime localTime) {
		put(token.getSnakeCase(), localTime);
	}

	@Override
	public void set(Property token, Integer integer) {
		put(token.getSnakeCase(), integer);
	}

	@Override
	public void set(Property token, Short aShort) {
		put(token.getSnakeCase(), aShort);
	}

	@Override
	public void set(Property token, Long aLong) {
		put(token.getSnakeCase(), aLong);
	}

	@Override
	public void set(Property token, UUID uuid) {
		put(token.getSnakeCase(), uuid);
	}

	@Override
	public void set(Property token, Double aDouble) {
		put(token.getSnakeCase(), aDouble);
	}

	@Override
	public void set(Property token, BigDecimal bigDecimal) {
		put(token.getSnakeCase(), bigDecimal);
	}

	@Override
	public void set(Property token, Float aFloat) {
		put(token.getSnakeCase(), aFloat);
	}

	@Override
	public void set(Property token, Boolean aBoolean) {
		put(token.getSnakeCase(), aBoolean);
	}

	@Override
	public void set(Property token, Byte aByte) {
		put(token.getSnakeCase(), aByte);
	}

	@Override
	public void set(Property token, byte[] bytes) {
		put(token.getSnakeCase(), bytes);
	}

	@Override
	public void set(Property token, Enum<?> anEnum) {
		put(token.getSnakeCase(), anEnum);
	}

	@Override
	public void set(Property token, Collection<?> list) {
		put(token.getSnakeCase(), list);
	}
}