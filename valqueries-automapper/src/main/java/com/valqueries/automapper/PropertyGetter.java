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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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

	public void set(Token token, String s) {
		put(token.snake_case(), s);
	}

	@Override
	public void set(Token token, Character character) {
		put(token.snake_case(), character);
	}

	@Override
	public void set(Token token, ZonedDateTime zonedDateTime) {
		put(token.snake_case(), zonedDateTime);
	}

	@Override
	public void set(Token token, LocalDateTime localDateTime) {
		put(token.snake_case(), localDateTime);
	}

	@Override
	public void set(Token token, Instant instant) {
		put(token.snake_case(), instant);
	}

	@Override
	public void set(Token token, LocalDate localDate) {
		put(token.snake_case(), localDate);
	}

	@Override
	public void set(Token token, Integer integer) {
		put(token.snake_case(), integer);
	}

	@Override
	public void set(Token token, Short aShort) {
		put(token.snake_case(), aShort);
	}

	@Override
	public void set(Token token, Long aLong) {
		put(token.snake_case(), aLong);
	}

	@Override
	public void set(Token token, UUID uuid) {
		put(token.snake_case(), uuid);
	}

	@Override
	public void set(Token token, Double aDouble) {
		put(token.snake_case(), aDouble);
	}

	@Override
	public void set(Token token, BigDecimal bigDecimal) {
		put(token.snake_case(), bigDecimal);
	}

	@Override
	public void set(Token token, Float aFloat) {
		put(token.snake_case(), aFloat);
	}

	@Override
	public void set(Token token, Boolean aBoolean) {
		put(token.snake_case(), aBoolean);
	}

	@Override
	public void set(Token token, Byte aByte) {
		put(token.snake_case(), aByte);
	}

	@Override
	public void set(Token token, byte[] bytes) {
		put(token.snake_case(), bytes);
	}

	@Override
	public void set(Token token, Enum<?> anEnum) {
		put(token.snake_case(), anEnum);
	}

	@Override
	public void set(Token token, Collection<?> list) {
		put(token.snake_case(), list);
	}
}
