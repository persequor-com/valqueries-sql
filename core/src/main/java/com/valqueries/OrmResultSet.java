
package com.valqueries;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

public class OrmResultSet {
	private final ResultSet resultSet;
	private final Orm orm;

	public OrmResultSet(ResultSet resultSet, Orm orm) {
		this.resultSet = resultSet;
		this.orm = orm;
	}

	public boolean getBoolean(String column) throws SQLException {
		return resultSet.getBoolean(column);
	}

	public Integer getInt(String column) throws SQLException {
		final int result = resultSet.getInt(column);
		if (resultSet.wasNull()) {
			return null;
		}
		return Integer.valueOf(result);
	}

	public Long getLong(String column) throws SQLException {
		final long result = resultSet.getLong(column);
		if (resultSet.wasNull()) {
			return null;
		}
		return Long.valueOf(result);
	}

	public Float getFloat(String column) throws SQLException {
		final float result = resultSet.getFloat(column);
		if (resultSet.wasNull()) {
			return null;
		}
		return result;
	}

	public Double getDouble(String column) throws SQLException {
		final double result = resultSet.getDouble(column);
		if (resultSet.wasNull()) {
			return null;
		}
		return result;
	}

	public <T extends Enum<T>> T getEnum(String columnName, Class<T> enumType) throws SQLException {
		final String enumValue = getString(columnName);
		if (enumValue == null) {
			return null;
		}
		return Enum.valueOf(enumType, enumValue);
	}

	public Long getLong(int index) throws SQLException {
		final long result = resultSet.getLong(index);
		if (resultSet.wasNull()) {
			return null;
		}
		return Long.valueOf(result);
	}

	public ZonedDateTime getDateTime(String column) throws SQLException {
		final Timestamp result = resultSet.getTimestamp(column);
		if (result == null) {
			return null;
		}
		return ZonedDateTime.ofInstant(result.toInstant(), ZoneOffset.UTC);
	}

	public String getString(String column) throws SQLException {
		return resultSet.getString(column);
	}

	public UUID getUUID(String column) throws SQLException {
		String uuid = resultSet.getString(column);
		return uuid == null ? null : UUID.fromString(uuid);
	}

	public String getString(int index) throws SQLException {
		return resultSet.getString(index);
	}

	public byte[] getBlob(String column) throws SQLException {
		return resultSet.getBytes(column);
	}

	public byte[] getBlob(int index) throws SQLException {
		return resultSet.getBytes(index);
	}

	public Orm getOrm() {
		return orm;
	}

	public LocalDate getDate(String column) throws SQLException {
		Date date = resultSet.getDate(column);
		if (date == null)  {
			return null;
		}
		return date.toLocalDate();
	}
}
