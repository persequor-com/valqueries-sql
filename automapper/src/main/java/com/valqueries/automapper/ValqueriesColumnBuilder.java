package com.valqueries.automapper;

import io.ran.ObjectMapHydrator;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ValqueriesColumnBuilder implements ObjectMapHydrator {
	private List<Token> columns = new ArrayList<>();
	private String prefix;
	private SqlNameFormatter sqlNameFormatter;

	public ValqueriesColumnBuilder(String prefix, SqlNameFormatter sqlNameFormatter) {
		this.prefix = prefix;
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	public String getString(Token token) {
		columns.add(token);
		return null;
	}

	@Override
	public Character getCharacter(Token token) {
		columns.add(token);
		return null;
	}

	@Override
	public ZonedDateTime getZonedDateTime(Token token) {
		columns.add(token);
		return null;
	}

	@Override
	public Instant getInstant(Token token) {
		columns.add(token);
		return null;
	}

	@Override
	public Integer getInteger(Token token) {
		columns.add(token);
		return 0;
	}

	@Override
	public Short getShort(Token token) {
		columns.add(token);
		return 0;
	}

	@Override
	public Long getLong(Token token) {
		columns.add(token);
		return 0L;
	}

	@Override
	public UUID getUUID(Token token) {
		columns.add(token);
		return null;
	}

	@Override
	public Double getDouble(Token token) {
		columns.add(token);
		return 0.0;
	}

	@Override
	public BigDecimal getBigDecimal(Token token) {
		columns.add(token);
		return null;
	}

	@Override
	public Float getFloat(Token token) {
		columns.add(token);
		return 0.0f;
	}

	@Override
	public Boolean getBoolean(Token token) {
		columns.add(token);
		return false;
	}

	@Override
	public Byte getByte(Token token) {
		columns.add(token);
		return null;
	}

	@Override
	public <T extends Enum<T>> T getEnum(Token token, Class<T> aClass) {
		columns.add(token);
		return null;
	}

	@Override
	public <T> Collection<T> getCollection(Token token, Class<T> aClass, Class<? extends Collection<T>> collectionClass) {
		columns.add(token);
		return null;
	}

	public String getSql() {
		return columns.stream().map(t -> prefix+"."+sqlNameFormatter.column(t)+" "+prefix+"_"+sqlNameFormatter.column(t)).collect(Collectors.joining(", "));
	}
}
