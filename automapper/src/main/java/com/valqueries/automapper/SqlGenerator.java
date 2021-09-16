package com.valqueries.automapper;

import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SqlGenerator {
	public String getTableName(TypeDescriber<?> typeDescriber) {
		return Token.CamelCase(typeDescriber.clazz().getSimpleName()).snake_case();
	}

	public String generate(TypeDescriber<?> typeDescriber) {
		return "CREATE TABLE IF NOT EXISTS "+ getTableName(typeDescriber)+" ("+typeDescriber.fields().stream().map(property -> {
			return "`"+property.getToken().snake_case()+ "` "+getSqlType(property.getType().clazz, property);
		}).collect(Collectors.joining(", "))+", PRIMARY KEY("+typeDescriber.primaryKeys().stream().map(property -> {
			return "`"+property.getToken().snake_case()+"`";
		}).collect(Collectors.joining(", "))+")"+getIndexes(typeDescriber)+");";
	}

	private String getIndexes(TypeDescriber<?> typeDescriber) {
		List<String> indexes = new ArrayList<>();
		for (Property property : typeDescriber.fields()) {
			Fulltext fullText = property.getAnnotations().get(Fulltext.class);
			if (fullText != null) {
				indexes.add("FULLTEXT(`"+property.getToken().snake_case()+"`)");
			}
		}
		if (indexes.isEmpty()) {
			return "";
		}
		return ", "+String.join(", ",indexes);
	}

	private String getSqlType(Class<?> type, Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		if (mappedType != null) {
			return mappedType.value();
		}
		if (type == String.class) {
			return "VARCHAR(255)";
		}
		if (type == UUID.class) {
			return "CHAR(36) CHARACTER SET latin1";
		}
		if (type == ZonedDateTime.class) {
			return "DATETIME";
		}
		if (Collection.class.isAssignableFrom(type)) {
			return "TEXT";
		}
		if (type.isEnum()) {
			return "VARCHAR(255) CHARACTER SET latin1";
		}
		if (type == int.class || type == Integer.class) {
			return "INT";
		}
		if (type == long.class || type == Long.class) {
			return "BIGINT";
		}
		throw new RuntimeException("So far unsupported column type: "+type.getName());
	}
}
