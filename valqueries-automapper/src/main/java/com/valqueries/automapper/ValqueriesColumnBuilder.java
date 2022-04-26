package com.valqueries.automapper;

import io.ran.DbName;
import io.ran.ObjectMapHydrator;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ValqueriesColumnBuilder implements ObjectMapHydrator {
	private List<Property> columns = new ArrayList<>();
	private String prefix;
	private SqlNameFormatter sqlNameFormatter;
	private SqlDialect dialect;
	private TypeDescriber typeDescriber;

	public ValqueriesColumnBuilder(String prefix, SqlNameFormatter sqlNameFormatter, SqlDialect dialect, TypeDescriber typeDescriber) {
		this.prefix = prefix;
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
		this.typeDescriber = typeDescriber;
	}

	@Override
	public String getString(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public Character getCharacter(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public ZonedDateTime getZonedDateTime(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public Instant getInstant(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public LocalDateTime getLocalDateTime(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public LocalDate getLocalDate(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public Integer getInteger(Property property) {
		columns.add(property);
		return 0;
	}

	@Override
	public Short getShort(Property property) {
		columns.add(property);
		return 0;
	}

	@Override
	public Long getLong(Property property) {
		columns.add(property);
		return 0L;
	}

	@Override
	public UUID getUUID(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public Double getDouble(Property property) {
		columns.add(property);
		return 0.0;
	}

	@Override
	public BigDecimal getBigDecimal(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public Float getFloat(Property property) {
		columns.add(property);
		return 0.0f;
	}

	@Override
	public Boolean getBoolean(Property property) {
		columns.add(property);
		return false;
	}

	@Override
	public Byte getByte(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public byte[] getBytes(Property property) {
		columns.add(property);
		return null;
	}

	@Override
	public <T extends Enum<T>> T getEnum(Property property, Class<T> aClass) {
		columns.add(property);
		return null;
	}

	@Override
	public <T> Collection<T> getCollection(Property property, Class<T> aClass, Class<? extends Collection<T>> collectionClass) {
		columns.add(property);
		return null;
	}

	public String getSql() {
		return columns.stream().map(t -> {
			DbName dbName = t.getAnnotations().get(DbName.class);
			if (dbName != null) {
				return prefix + "." + dialect.escapeColumnOrTable(dbName.value()) + " " + prefix + "_" + dbName.value();
			} else {
				return prefix + "." + dialect.escapeColumnOrTable(sqlNameFormatter.column(t.getToken())) + " " + prefix + "_" + sqlNameFormatter.column(t.getToken());
			}
		}).collect(Collectors.joining(", "));
	}
}
