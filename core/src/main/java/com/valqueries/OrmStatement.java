
package com.valqueries;

import jdk.vm.ci.meta.Local;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class OrmStatement extends NamedSqlStatement implements IStatement {

	//There is no UNKNOWN type in java.sql.Types class. The min integer value hack is copied from spring's jdbcTemplate
	private static final int UNKNOWN = Integer.MIN_VALUE;

	private final Map<String, Object> values = new LinkedHashMap<>();

	public OrmStatement(String sqlString) {
		super(sqlString);
	}

	public OrmStatement() {
	}

	private boolean hasValue(String s) {
		return values.containsKey(s);
	}

	public Object getValue(String s) {
		return values.get(s);
	}

	public Set<String> getValueNames() {
		return values.keySet();
	}

	@Override
	public void set(String column, String value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, UUID value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, Enum<?> enumValue) {
		values.put(column, enumValue == null ? null : enumValue.name());
	}

	@Override
	public void set(String column, Long value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, ZonedDateTime value) {
		values.put(column, value == null ? null : new Timestamp(value.toInstant().toEpochMilli()));
	}

	@Override
	public void set(String column, LocalDateTime value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, Integer value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, boolean value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, Float value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, Double value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, byte[] value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, Object value) {
		values.put(column, value);
	}

	@Override
	public void set(String column, Collection<?> value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(String.format("Empty or null collection was set to statement. Parameter: `%s`, value: `%s`)", column, value));
		}
		values.put(column, value);
	}

	public void setValue(PreparedStatement preparedStatement, JdbcParameterIndex index, String parameterName) throws SQLException {
		Object value = getValue(parameterName);

		setValueInternal(preparedStatement, index, value);
	}

	private void setValueInternal(PreparedStatement preparedStatement, JdbcParameterIndex index, Object value) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(index.getAndIncrement(), UNKNOWN);
		} else if (value instanceof Boolean) {
			preparedStatement.setBoolean(index.getAndIncrement(), (Boolean) value);
		} else if (value instanceof Integer) {
			preparedStatement.setInt(index.getAndIncrement(), (Integer) value);
		} else if (value instanceof Long) {
			preparedStatement.setLong(index.getAndIncrement(), (Long) value);
		} else if (value instanceof Float) {
			preparedStatement.setDouble(index.getAndIncrement(), (Float) value);
		} else if (value instanceof Double) {
			preparedStatement.setDouble(index.getAndIncrement(), (Double) value);
		} else if (value instanceof String) {
			preparedStatement.setString(index.getAndIncrement(), ((String) value));
		} else if (value instanceof UUID) {
			preparedStatement.setString(index.getAndIncrement(), ((UUID) value).toString());
		} else if (value instanceof Enum) {
			preparedStatement.setString(index.getAndIncrement(), ((Enum) value).name());
		} else if (value instanceof Timestamp) {
			preparedStatement.setTimestamp(index.getAndIncrement(), ((Timestamp) value));
		} else if (value instanceof ZonedDateTime) {
			preparedStatement.setTimestamp(index.getAndIncrement(), ((Timestamp) Timestamp.from(((ZonedDateTime)value).toInstant()
			)));
		} else if (value instanceof LocalDateTime) {
			preparedStatement.setTimestamp(index.getAndIncrement(), (Timestamp.valueOf((LocalDateTime)value)));
		} else if (value instanceof byte[]) {
			preparedStatement.setBytes(index.getAndIncrement(), (byte[]) value);
		} else if (value instanceof Collection) {
			for (Object element : (Collection<?>) value) {
				setValueInternal(preparedStatement, index, element);
			}
		} else {
			throw new OrmException("Unknown value type for parameter " + index + ": " + value.getClass());
		}
	}

	@Override
	protected String getJdbcPlaceHolder(String parameterName) {
		Object value = getValue(parameterName);
		if (value instanceof Collection) {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < ((Collection<?>) value).size(); i++) {
				if (i > 0) {
					result.append(", ");
				}
				result.append("?");
			}
			return result.toString();
		}
		return "?";
	}
}
