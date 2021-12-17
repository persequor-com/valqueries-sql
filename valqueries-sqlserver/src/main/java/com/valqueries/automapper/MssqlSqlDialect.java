package com.valqueries.automapper;

import io.ran.Clazz;
import io.ran.Property;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class MssqlSqlDialect implements SqlDialect {
	private SqlNameFormatter sqlNameFormatter;

	public MssqlSqlDialect(SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	public String escapeColumnOrTable(String name) {
		return "["+name+"]";
	}

	@Override
	public <O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		return "MERGE "+getTableName(Clazz.of(oClass))+" as target USING " +
				"(VALUES "+columnizer.getValueTokens().stream().map((l) -> "("+l.stream().map(e -> ":"+e).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", "))+
				") incoming ("+columnizer.getFields().entrySet().stream().map((e) -> "["+e.getValue()+"]").collect(Collectors.joining(", "))+") on "+columnizer.getKeys().stream().map(k -> "target.["+k+"] = incoming.["+k+"]").collect(Collectors.joining(" AND "))+
				(columnizer.getFieldsWithoutKeys().size() > 0 ? " WHEN MATCHED THEN UPDATE SET "+columnizer.getFieldsWithoutKeys().entrySet().stream().map(e -> "["+e.getValue()+"] = incoming.["+e.getValue()+"]").collect(Collectors.joining(", ")):"")+
				" WHEN NOT MATCHED THEN INSERT ("+columnizer.getFields().entrySet().stream().map(e -> "["+e.getValue()+"]").collect(Collectors.joining(", "))+") " +
				"VALUES ("+columnizer.getFields().entrySet().stream().map(e -> "incoming.["+e.getKey()+"]").collect(Collectors.joining(", "))+");";
	}

	public String getSqlType(Class type, Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		if (mappedType != null) {
			return mappedType.value();
		}
		if (type == UUID.class) {
			return "UNIQUEIDENTIFIER";
		}
		if (type == boolean.class || type == Boolean.class) {
			return "TINYINT";
		}
		return SqlDialect.super.getSqlType(type, property);
	}

	@Override
	public String column(Token token) {
		return sqlNameFormatter.column(token);
	}

	@Override
	public String limit(int offset, Integer limit) {
		return " OFFSET "+offset+" ROWS" +
				"    FETCH NEXT "+limit+" ROWS ONLY";
	}

	public String getTableName(Clazz<? extends Object> modeltype) {
		return escapeColumnOrTable(sqlNameFormatter.table(modeltype.clazz));
	}
}
