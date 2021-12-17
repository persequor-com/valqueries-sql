package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.Clazz;
import io.ran.Key;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import io.ran.token.Token;

import javax.inject.Inject;
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

public class SqlGenerator {
	private SqlNameFormatter sqlNameFormatter;
	private SqlDialect dialect;

	@Inject
	public SqlGenerator(SqlNameFormatter sqlNameFormatter, DialectFactory dialectFactory, Database database) {
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialectFactory.get(database);
	}

	public String getTableName(TypeDescriber<?> typeDescriber) {
		return dialect.getTableName(Clazz.of(typeDescriber.clazz()));
	}

	public String generateCreateTable(TypeDescriber<?> typeDescriber) {
		return dialect.generateCreateTable(typeDescriber);
	}

	public String generateCreateTable(Class<?> clazz) {
		return generateCreateTable(TypeDescriberImpl.getTypeDescriber(clazz));
	}






}
