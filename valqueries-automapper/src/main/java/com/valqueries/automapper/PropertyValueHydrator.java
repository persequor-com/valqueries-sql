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
	public String getString(Property property) {
		return (String) getValue(property);
	}

	private Object getValue(Property property) {
		return newValues.stream().filter(p -> p.getProperty().matchesSnakeCase(property.getSnakeCase())).findFirst().get().getValue();
	}

	@Override
	public Character getCharacter(Property property) {
		return (Character) getValue(property);
	}

	@Override
	public ZonedDateTime getZonedDateTime(Property property) {
		return (ZonedDateTime) getValue(property);
	}

	@Override
	public Instant getInstant(Property property) {
		return (Instant) getValue(property);
	}

	@Override
	public LocalDateTime getLocalDateTime(Property property) {
		return (LocalDateTime) getValue(property);
	}

	@Override
	public LocalDate getLocalDate(Property property) {
		return (LocalDate) getValue(property);
	}

	@Override
	public Integer getInteger(Property property) {
		return (Integer) getValue(property);
	}

	@Override
	public Short getShort(Property property) {
		return (Short) getValue(property);
	}

	@Override
	public Long getLong(Property property) {
		return (Long) getValue(property);
	}

	@Override
	public UUID getUUID(Property property) {
		return (UUID) getValue(property);
	}

	@Override
	public Double getDouble(Property property) {
		return (Double) getValue(property);
	}

	@Override
	public BigDecimal getBigDecimal(Property property) {
		return (BigDecimal) getValue(property);
	}

	@Override
	public Float getFloat(Property property) {
		return (Float) getValue(property);
	}

	@Override
	public Boolean getBoolean(Property property) {
		return (Boolean) getValue(property);
	}

	@Override
	public Byte getByte(Property property) {
		return (Byte) getValue(property);
	}

	@Override
	public byte[] getBytes(Property property) {
		return (byte[]) getValue(property);
	}

	@Override
	public <T extends Enum<T>> T getEnum(Property property, Class<T> aClass) {
		return (T) getValue(property);
	}

	@Override
	public <T> Collection<T> getCollection(Property property, Class<T> aClass, Class<? extends Collection<T>> aClass1) {
		return (Collection<T>) getValue(property);
	}
}
