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
	public String getString(Token token) {
		return (String) getValue(token);
	}

	private Object getValue(Token token) {
		return newValues.stream().filter(p -> p.getProperty().getToken().equals(token)).findFirst().get().getValue();
	}

	@Override
	public Character getCharacter(Token token) {
		return (Character) getValue(token);
	}

	@Override
	public ZonedDateTime getZonedDateTime(Token token) {
		return (ZonedDateTime) getValue(token);
	}

	@Override
	public Instant getInstant(Token token) {
		return (Instant) getValue(token);
	}

	@Override
	public LocalDateTime getLocalDateTime(Token token) {
		return (LocalDateTime) getValue(token);

	}

	@Override
	public LocalDate getLocalDate(Token token) {
		return (LocalDate) getValue(token);
	}

	@Override
	public Integer getInteger(Token token) {
		return (Integer) getValue(token);
	}

	@Override
	public Short getShort(Token token) {
		return (Short) getValue(token);
	}

	@Override
	public Long getLong(Token token) {
		return (Long) getValue(token);

	}

	@Override
	public UUID getUUID(Token token) {
		return (UUID) getValue(token);
	}

	@Override
	public Double getDouble(Token token) {
		return (Double) getValue(token);

	}

	@Override
	public BigDecimal getBigDecimal(Token token) {
		return (BigDecimal) getValue(token);
	}

	@Override
	public Float getFloat(Token token) {
		return (Float) getValue(token);

	}

	@Override
	public Boolean getBoolean(Token token) {
		return (Boolean) getValue(token);

	}

	@Override
	public Byte getByte(Token token) {
		return (Byte) getValue(token);

	}

	@Override
	public <T extends Enum<T>> T getEnum(Token token, Class<T> aClass) {
		return (T) getValue(token);
	}

	@Override
	public <T> Collection<T> getCollection(Token token, Class<T> aClass, Class<? extends Collection<T>> aClass1) {
		return (Collection<T>) getValue(token);

	}
}
