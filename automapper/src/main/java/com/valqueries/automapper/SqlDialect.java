package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import com.valqueries.automapper.elements.Element;
import io.ran.Clazz;
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
	String getTableName(Token token);

	String createTableStatement();
	default String generateCreateTable(TypeDescriber<?> typeDescriber) {
		return createTableStatement() + getTableName(Clazz.of(typeDescriber.clazz()))+" ("+typeDescriber.fields().stream().map(property -> {
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
			if(!keySet.isPrimary()) {
				indexes.add(getIndex(keySet));
			}
		});
		if (indexes.isEmpty()) {
			return "";
		}
		return ", "+String.join(", ",indexes);
	}

	default String getIndex(KeySet keySet) {
		String name = keySet.getName();
		String keyName = "INDEX "+escapeColumnOrTable(name);
		if (keySet.isPrimary()) {
			keyName = "PRIMARY KEY";
		}
		return keyName+" ("+keySet.stream().map(f -> escapeColumnOrTable(column(f.getToken()))).collect(Collectors.joining(", "))+")";
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
		if (type == byte[].class) {
			return "BLOB";
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

	String update(TypeDescriber<?> typeDescriber, List<Element> elements, List<Property.PropertyValue> newPropertyValues);

	default String describe(String tablename) {
		return "DESCRIBE " + tablename + "";
	}

	default String describeIndex(String tablename) {
		return "show index from " + tablename + "";
	}

	default String getDescribedFieldColumnName() {
		return "Field";
	}

	default String getDescribedFieldColumnType() {
		return "Type";
	}

	SqlDescriber.DbRow getDbRow(OrmResultSet ormResultSet);

	default SqlDescriber.DbIndex getDbIndex(OrmResultSet r) {
		try {
			return new SqlDescriber.DbIndex(r.getInt("Non_unique") == 0, r.getString("Key_name")+" KEY", r.getString("Key_name"), r.getString("Column_name"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	String changeColumn(String table, String columnName, String sqlType);

	String addIndex(String tablename, KeySet key, boolean isUnique);

	default String addColumn(String tablename, String columnName, String sqlType) {
		return "ALTER TABLE " + tablename + " ADD COLUMN " + escapeColumnOrTable(columnName) + " " + sqlType + ";";
	}

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

	default String addColumn() {
		return "ADD COLUMN ";
	}

	default String alterColumn(Token name) {
		return "CHANGE COLUMN "+escapeColumnOrTable(column(name))+" "+escapeColumnOrTable(column(name));
	}

	default String addIndexOnCreate(Token name, KeySet keyset, boolean isUnique) {
		String indexType = "INDEX ";
		if (keyset.isPrimary()) {
			indexType = "PRIMARY KEY";
		} else if (isUnique) {
			indexType = "UNIQUE ";
		}
		return (keyset.isPrimary() ? "PRIMARY KEY ":indexType+" ")+escapeColumnOrTable(column(name))+" "+"("+keyset.stream().map(f -> escapeColumnOrTable(column(f.getToken()))).collect(Collectors.joining(", "))+")";
	}

	default String dropIndex(Token tableName, String indexName, boolean isPrimary) {
		if (isPrimary) {
			indexName = "PRIMARY KEY";
		} else {
			indexName = "INDEX "+indexName;
		}
		return "ALTER TABLE "+escapeColumnOrTable(column(tableName))+" "+"DROP "+indexName;
	}
}
