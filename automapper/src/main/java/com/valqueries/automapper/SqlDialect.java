package com.valqueries.automapper;

import com.valqueries.automapper.elements.Element;
import io.ran.Clazz;
import io.ran.Key;
import io.ran.KeySet;
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

public interface SqlDialect {
	String escapeColumnOrTable(String name);
	<O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass);
	String getTableName(Clazz<? extends Object> modeltype);

	default String generateCreateTable(TypeDescriber<?> typeDescriber) {
		return "CREATE TABLE "+ getTableName(Clazz.of(typeDescriber.clazz()))+" ("+typeDescriber.fields().stream().map(property -> {
			return ""+escapeColumnOrTable(column(property.getToken()))+ " "+getSqlType(property.getType().clazz, property);
		}).collect(Collectors.joining(", "))+", PRIMARY KEY("+typeDescriber.primaryKeys().stream().map(property -> {
			return escapeColumnOrTable(column(property.getToken()));
		}).collect(Collectors.joining(", "))+")"+getIndexes(typeDescriber)+");";
	}

	default String getIndexes(TypeDescriber<?> typeDescriber) {
		List<String> indexes = new ArrayList<>();
		for (Property property : typeDescriber.fields()) {
			Fulltext fullText = property.getAnnotations().get(Fulltext.class);
			if (fullText != null) {
//				indexes.add("FULLTEXT(`"+sqlNameFormatter.column(property.getToken())+"`)");
			}
		}
		typeDescriber.indexes().forEach(keySet -> {
			indexes.add(getIndex(keySet));
		});
		if (indexes.isEmpty()) {
			return "";
		}
		return ", "+String.join(", ",indexes);
	}

	default String getIndex(KeySet keySet) {
		String name = keySet.get(0).getProperty().getAnnotations().get(Key.class).name();
		return "INDEX "+name+" ("+keySet.stream().map(f -> "`"+column(f.getToken())+"`").collect(Collectors.joining(", "))+")";

	}

	default String getSqlType(Class type, Property property) {
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
		if (type == Character.class) {
			return "CHAR(1)";
		}
		if (type == ZonedDateTime.class || type == Instant.class) {
			return "DATETIME";
		}
		if (type == LocalDateTime.class) {
			return "DATETIME";
		}
		if (type == LocalDate.class) {
			return "DATE";
		}
		if (Collection.class.isAssignableFrom(type)) {
			return "VARCHAR(4000)";
		}
		if (type.isEnum()) {
			return "VARCHAR(255)";
		}
		if (type == int.class || type == Integer.class || type == Short.class || type == short.class) {
			return "INT";
		}
		if (type == boolean.class || type == Boolean.class) {
			return "TINYINT";
		}
		if (type == byte.class || type == Byte.class) {
			return "TINYINT";
		}
		if (type == long.class || type == Long.class) {
			return "BIGINT";
		}
		if (type == BigDecimal.class || type == Double.class || type == double.class || type == Float.class || type == float.class) {
			return "DECIMAL(18,9)";
		}
		throw new RuntimeException("So far unsupported column type: "+type.getName());

	}

	String column(Token token);

	String limit(int offset, Integer limit);

	default String delete(String tableAlias, TypeDescriber<?> typeDescriber, List<Element> elements, int offset, Integer limit) {
		String sql = "DELETE "+tableAlias+" FROM " + getTableName(Clazz.of(typeDescriber.clazz())) + " AS "+tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		if (limit !=  null) {
			sql += limit(offset, limit);
		}
//		System.out.println(sql);
		return sql;
	}
}
