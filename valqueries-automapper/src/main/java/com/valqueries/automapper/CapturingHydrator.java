package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import io.ran.ObjectMapHydrator;
import io.ran.Property;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
	public String getString(Property token) {
		try {
			String value = innerHydrator.getString(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Character getCharacter(Property token) {
		try {
			Character value = innerHydrator.getCharacter(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public ZonedDateTime getZonedDateTime(Property token) {
		try {
			ZonedDateTime value = innerHydrator.getZonedDateTime(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Instant getInstant(Property token) {
		try {
			Instant value = innerHydrator.getInstant(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public LocalDateTime getLocalDateTime(Property token) {
		try {
			LocalDateTime value = innerHydrator.getLocalDateTime(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public LocalDate getLocalDate(Property token) {
		try {
			LocalDate value = innerHydrator.getLocalDate(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Integer getInteger(Property token) {
		try {
			Integer value = innerHydrator.getInteger(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0;
		}
	}

	@Override
	public Short getShort(Property token) {
		try {
			Short value = innerHydrator.getShort(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0;
		}
	}

	@Override
	public Long getLong(Property token) {
		try {
			Long value = innerHydrator.getLong(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0L;
		}
	}

	@Override
	public UUID getUUID(Property token) {
		try {
			UUID value = innerHydrator.getUUID(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Double getDouble(Property token) {
		try {
			Double value = innerHydrator.getDouble(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0.0;
		}
	}


	@Override
	public BigDecimal getBigDecimal(Property token) {
		try {
			BigDecimal value = innerHydrator.getBigDecimal(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public Float getFloat(Property token) {
		try {
			Float value = innerHydrator.getFloat(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return 0.0f;
		}
	}

	@Override
	public Boolean getBoolean(Property token) {
		try {
			Boolean value = innerHydrator.getBoolean(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return false;
		}
	}

	@Override
	public Byte getByte(Property token) {
		try {
			Byte value = innerHydrator.getByte(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return Byte.valueOf((byte)0);
		}
	}

	@Override
	public byte[] getBytes(Property token) {
		try {
			byte[] value = innerHydrator.getBytes(token);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return new byte[0];
		}
	}

	@Override
	public <T extends Enum<T>> T getEnum(Property token, Class<T> aClass) {
		try {
			T value = innerHydrator.getEnum(token, aClass);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return null;
		}
	}

	@Override
	public <T> Collection<T> getCollection(Property token, Class<T> aClass, Class<? extends Collection<T>> aClass1) {
		try {
			Collection<T> value = innerHydrator.getCollection(token, aClass, aClass1);
			values.put(token, value);
			return value;
		} catch (RuntimeException e) {
			// Ignoring non existing fields
			return Collections.emptyList();
		}
	}

	public Object getValue(Token token) {
		return values.get(token);
	}

	public List<Object> getValues() {
		return new ArrayList<>(values.values());
	}
}
