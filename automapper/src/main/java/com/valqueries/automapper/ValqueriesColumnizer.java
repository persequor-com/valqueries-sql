/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries.automapper;

import com.valqueries.IStatement;
import com.valqueries.Setter;
import io.ran.CompoundKey;
import io.ran.GenericFactory;
import io.ran.MappingHelper;
import io.ran.ObjectMapColumnizer;
import io.ran.Property;
import io.ran.token.Token;

import java.awt.image.ImageProducer;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ValqueriesColumnizer<T> implements ObjectMapColumnizer, Setter {
	private final List<Consumer<IStatement>> statements = new ArrayList<>();
	private final List<String> sqlStatements = new ArrayList<>();
	private final List<String> sqlWithoutKey = new ArrayList<>();
	private CompoundKey key;
	protected SqlNameFormatter sqlNameFormatter;

	public ValqueriesColumnizer(GenericFactory factory, MappingHelper mappingHelper, T t, SqlNameFormatter columnFormatter) {
		this.sqlNameFormatter = columnFormatter;
		key = mappingHelper.getKey(t);
		mappingHelper.columnize(t, this);
	}

	protected ValqueriesColumnizer() {}



	protected void add(Token key, Consumer<IStatement> consumer) {
		String sql = "`"+transformKey(key)+"` = :"+key.snake_case();
		sqlStatements.add(sql);


		if (!((Property.PropertyValueList<?>)this.key.getValues()).stream().noneMatch(pv -> {
			return pv.getProperty().getType().equals(key);
		})) {
			sqlWithoutKey.add(sql);
		}
		statements.add(consumer);
	}

	protected String transformKey(Token key) {
		return sqlNameFormatter.column(key);
	}

	protected String transformFieldPlaceholder(Token key) {
		return key.snake_case();
	}

	@Override
	public void set(Token key, String value) {
		add(key, s -> s.set(transformFieldPlaceholder(key), value));
	}

	@Override
	public void set(Token token, Character character) {
		add(token, s -> s.set(transformFieldPlaceholder(token), character.toString()));
	}

	@Override
	public void set(Token key, ZonedDateTime value) {
		add(key, s -> s.set(transformFieldPlaceholder(key), value));
	}

	@Override
	public void set(Token key, Integer value) {
		add(key, s -> s.set(transformFieldPlaceholder(key), value));
	}

	@Override
	public void set(Token token, Short aShort) {
		add(token, s -> s.set(transformKey(token), aShort.intValue()));
	}

	@Override
	public void set(Token key, Long value) {
		statements.add(s -> s.set(transformFieldPlaceholder(key), value));
	}

	@Override
	public void set(Token key, UUID value) {
		add(key, s -> s.set(transformFieldPlaceholder(key), value));
	}

	@Override
	public void set(Token key, Double value) {
		add(key, s -> s.set(transformFieldPlaceholder(key), value));
	}

	@Override
	public void set(Token key, BigDecimal value) {
		add(key, s -> s.set(transformFieldPlaceholder(key), value.toString()));
	}

	@Override
	public void set(Token key, Float value) {
		add(key, s -> s.set(transformFieldPlaceholder(key), value));
	}

	@Override
	public void set(Token token, Boolean value) {
		add(token, s -> s.set(transformFieldPlaceholder(token), value));
	}

	@Override
	public void set(Token token, Byte aByte) {
		add(token, s -> s.set(transformFieldPlaceholder(token), aByte.intValue()));
	}

	@Override
	public void set(Token token, Enum<?> anEnum) {
		add(token, s -> s.set(transformFieldPlaceholder(token), anEnum));
	}

	@Override
	public void set(Token token, Collection<?> list) {
		add(token, s -> s.set(transformFieldPlaceholder(token), list == null ? null : list.stream().map(Object::toString).collect(Collectors.joining(","))));
	}

	@Override
	public void set(IStatement statement) {
		statements.forEach(s -> s.accept(statement));
	}

	public String getSql() {
		return String.join(", ", sqlStatements);
	}

	public String getSqlWithoutKey() {
		return String.join(", ", sqlWithoutKey);
	}
}

