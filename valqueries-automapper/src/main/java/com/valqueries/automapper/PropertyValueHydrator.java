package com.valqueries.automapper;

import io.ran.ObjectMapHydrator;
import io.ran.Property;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public class PropertyValueHydrator implements ObjectMapHydrator {
	private Property.PropertyValueList<?> newValues;

	public PropertyValueHydrator(Property.PropertyValueList newValues) {
		this.newValues = newValues;
	}

	@Override
	public String getString(Property token) {
		return (String) getValue(token);
	}

	private Object getValue(Property token) {
		return newValues.stream().filter(p -> p.getProperty().matchesSnakeCase(token.getSnakeCase())).findFirst().get().getValue();
	}

	@Override
	public Character getCharacter(Property token) {
		return (Character) getValue(token);
	}

	@Override
	public ZonedDateTime getZonedDateTime(Property token) {
		return (ZonedDateTime) getValue(token);
	}

	@Override
	public Instant getInstant(Property token) {
		return (Instant) getValue(token);
	}

	@Override
	public LocalDateTime getLocalDateTime(Property token) {
		return (LocalDateTime) getValue(token);
	}

	@Override
	public LocalDate getLocalDate(Property token) {
		return (LocalDate) getValue(token);
	}

	@Override
	public Integer getInteger(Property token) {
		return (Integer) getValue(token);
	}

	@Override
	public Short getShort(Property token) {
		return (Short) getValue(token);
	}

	@Override
	public Long getLong(Property token) {
		return (Long) getValue(token);
	}

	@Override
	public UUID getUUID(Property token) {
		return (UUID) getValue(token);
	}

	@Override
	public Double getDouble(Property token) {
		return (Double) getValue(token);
	}

	@Override
	public BigDecimal getBigDecimal(Property token) {
		return (BigDecimal) getValue(token);
	}

	@Override
	public Float getFloat(Property token) {
		return (Float) getValue(token);
	}

	@Override
	public Boolean getBoolean(Property token) {
		return (Boolean) getValue(token);
	}

	@Override
	public Byte getByte(Property token) {
		return (Byte) getValue(token);
	}

	@Override
	public byte[] getBytes(Property token) {
		return (byte[]) getValue(token);
	}

	@Override
	public <T extends Enum<T>> T getEnum(Property token, Class<T> aClass) {
		return (T) getValue(token);
	}

	@Override
	public <T> Collection<T> getCollection(Property token, Class<T> aClass, Class<? extends Collection<T>> aClass1) {
		return (Collection<T>) getValue(token);
	}
}
