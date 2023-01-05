package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import io.ran.ObjectMapHydrator;
import io.ran.Property;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CapturingHydrator implements ObjectMapHydrator {
	private Map<Property, Object> values = new LinkedHashMap<>();
	private ObjectMapHydrator innerHydrator;

	public CapturingHydrator(ObjectMapHydrator innerHydrator) {
		this.innerHydrator = innerHydrator;
	}

	@Override
	public String getString(Property property) {
		try {
			String value = innerHydrator.getString(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Character getCharacter(Property property) {
		try {
			Character value = innerHydrator.getCharacter(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public ZonedDateTime getZonedDateTime(Property property) {
		try {
			ZonedDateTime value = innerHydrator.getZonedDateTime(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Instant getInstant(Property property) {
		try {
			Instant value = innerHydrator.getInstant(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public LocalDateTime getLocalDateTime(Property property) {
		try {
			LocalDateTime value = innerHydrator.getLocalDateTime(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public LocalDate getLocalDate(Property property) {
		try {
			LocalDate value = innerHydrator.getLocalDate(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public LocalTime getLocalTime(Property property) {
		try {
			LocalTime value = innerHydrator.getLocalTime(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Integer getInteger(Property property) {
		try {
			Integer value = innerHydrator.getInteger(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0;
		}
	}

	@Override
	public Short getShort(Property property) {
		try {
			Short value = innerHydrator.getShort(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0;
		}
	}

	@Override
	public Long getLong(Property property) {
		try {
			Long value = innerHydrator.getLong(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0L;
		}
	}

	@Override
	public UUID getUUID(Property property) {
		try {
			UUID value = innerHydrator.getUUID(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Double getDouble(Property property) {
		try {
			Double value = innerHydrator.getDouble(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0.0;
		}
	}


	@Override
	public BigDecimal getBigDecimal(Property property) {
		try {
			BigDecimal value = innerHydrator.getBigDecimal(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Float getFloat(Property property) {
		try {
			Float value = innerHydrator.getFloat(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0.0f;
		}
	}

	@Override
	public Boolean getBoolean(Property property) {
		try {
			Boolean value = innerHydrator.getBoolean(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return false;
		}
	}

	@Override
	public Byte getByte(Property property) {
		try {
			Byte value = innerHydrator.getByte(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return Byte.valueOf((byte)0);
		}
	}

	@Override
	public byte[] getBytes(Property property) {
		try {
			byte[] value = innerHydrator.getBytes(property);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return new byte[0];
		}
	}

	@Override
	public <T extends Enum<T>> T getEnum(Property property, Class<T> aClass) {
		try {
			T value = innerHydrator.getEnum(property, aClass);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public <T> Collection<T> getCollection(Property property, Class<T> aClass, Class<? extends Collection<T>> aClass1) {
		try {
			Collection<T> value = innerHydrator.getCollection(property, aClass, aClass1);
			values.put(property, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return Collections.emptyList();
		}
	}

	public Object getValue(Token property) {
		return values.get(property);
	}

	public List<Object> getValues() {
		return new ArrayList<>(values.values());
	}
}