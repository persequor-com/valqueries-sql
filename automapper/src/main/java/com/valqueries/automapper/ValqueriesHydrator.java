/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import io.ran.ObjectMapHydrator;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ValqueriesHydrator implements ObjectMapHydrator {
	private String prefix;
	private OrmResultSet row;
	private SqlNameFormatter sqlNameFormatter;

	public ValqueriesHydrator(OrmResultSet row, SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
		this.prefix = "";
		this.row = row;
	}

	public ValqueriesHydrator(String prefix, OrmResultSet row, SqlNameFormatter sqlNameFormatter) {
		this.prefix = prefix;
		this.row = row;
		this.sqlNameFormatter = sqlNameFormatter;
	}

	private String transformKey(Token key) {
		return prefix+sqlNameFormatter.column(key);
	}

	@Override
	public String getString(Token key) {
		try {
			return row.getString(transformKey(key));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Character getCharacter(Token token) {
		try {
			return row.getString(transformKey(token)).charAt(0);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ZonedDateTime getZonedDateTime(Token key) {
		try {
			return row.getDateTime(transformKey(key));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Instant getInstant(Token key) {
		try {
			return row.getDateTime(transformKey(key)).toInstant();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Integer getInteger(Token key) {
		try {
			return row.getInt(transformKey(key));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Short getShort(Token token) {
		try {
			return row.getInt(transformKey(token)).shortValue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	}

	@Override
	public Long getLong(Token key) {
		try {
			return row.getLong(transformKey(key));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UUID getUUID(Token key) {
		try {
			return row.getUUID(transformKey(key));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Double getDouble(Token key) {
		try {
			return row.getDouble(transformKey(key));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BigDecimal getBigDecimal(Token key) {
		try {
			return new BigDecimal(row.getString(transformKey(key)));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Float getFloat(Token key) {
		try {
			return row.getFloat(transformKey(key));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Boolean getBoolean(Token token) {
		try {
			return row.getBoolean(transformKey(token));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Byte getByte(Token token) {
		try {
			return row.getInt(transformKey(token)).byteValue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}	}

	@Override
	public <T extends Enum<T>> T getEnum(Token token, Class<T> aClass) {
		try {
			return row.getEnum(transformKey(token), aClass);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> Collection<T> getCollection(Token token, Class<T> aClass, Class<? extends Collection<T>> collectionClass) {
		List<T> collection = Arrays.asList(getString(token).split(",")).stream().map(s -> convert(aClass, s)).collect(Collectors.toList());
		if (List.class.isAssignableFrom(collectionClass)) {
			return collection;
		} else if (Set.class.isAssignableFrom(collectionClass)) {
			return new HashSet<>(collection);
		} else {
			throw new RuntimeException("Only Lists or Sets are supported collection types by valqueries");
		}
	}


	private <T> T convert(Class<T> aClass, String s) {
		if (aClass.equals(String.class)) {
			return (T)s;
		}
		if (aClass.equals(Integer.class)) {
			if (s == null || s.trim().equals("")) {
				return (T)Integer.valueOf(0);
			}
			return (T)Integer.valueOf(s);
		}
		throw new RuntimeException("Currently unsupported list elemeent type: "+aClass.getName());
	}
}
