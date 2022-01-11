package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import com.valqueries.automapper.elements.Element;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.schema.FormattingTokenList;
import io.ran.token.ColumnToken;
import io.ran.token.IndexToken;
import io.ran.token.TableToken;
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
	TableToken getTableName(Clazz<? extends Object> modeltype);

	String getCreateTableStatement();

	default String generateCreateTable(TypeDescriber<?> typeDescriber) {
		return getCreateTableStatement() + getTableName(Clazz.of(typeDescriber.clazz()))+" ("+typeDescriber.fields().stream().map(property -> {
			return ""+column(property.getToken())+ " "+getSqlType(property);
		}).collect(Collectors.joining(", "))+", PRIMARY KEY("+typeDescriber.primaryKeys().stream().map(property -> {
			return column(property.getToken());
		}).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")"+ generateIndexesForCreateTable(typeDescriber)+");";
	}

	default String generateIndexesForCreateTable(TypeDescriber<?> typeDescriber) {
		List<String> indexes = new ArrayList<>();
		for (Property property : typeDescriber.fields()) {
			Fulltext fullText = property.getAnnotations().get(Fulltext.class);
			if (fullText != null) {
//				indexes.add("FULLTEXT(`"+sqlNameFormatter.column(property.getToken())+"`)");
			}
		}
		typeDescriber.indexes().forEach(keySet -> {
			if(!keySet.isPrimary()) {
				indexes.add(generateIndex(keySet));
			}
		});
		if (indexes.isEmpty()) {
			return "";
		}
		return ", "+String.join(", ",indexes);
	}

	default String generateIndex(KeySet keySet) {
		String name = keySet.getName();
		String keyName = "INDEX "+escapeColumnOrTable(name);
		if (keySet.isPrimary()) {
			keyName = "PRIMARY KEY";
		}
		return keyName+" ("+keySet.stream().map(f -> column(f.getToken())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
	}

	default String getSqlType(Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		Class type = property.getType().clazz;
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

	ColumnToken column(Token token);
	TableToken table(Token token);

	String limit(int offset, Integer limit);

	String update(TypeDescriber<?> typeDescriber, List<Element> elements, List<Property.PropertyValue> newPropertyValues);

	default String describe(TableToken tablename) {
		return "DESCRIBE " + tablename + "";
	}

	default String describeIndex(TableToken tablename) {
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

	String changeColumn(TableToken table, ColumnToken columnName, String sqlType);

	String addIndex(TableToken tablename, KeySet key, boolean isUnique);

	default String addColumn(TableToken tablename, ColumnToken columnName, String sqlType) {
		return "ALTER TABLE " + tablename + " ADD COLUMN " + columnName + " " + sqlType + ";";
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

	default String alterColumn(ColumnToken name) {
		return "CHANGE COLUMN "+name+" "+name;
	}

	default String addIndexOnCreate(TableToken name, KeySet keyset, boolean isUnique) {
		String indexType = "INDEX ";
		if (keyset.isPrimary()) {
			indexType = "PRIMARY KEY";
		} else if (isUnique) {
			indexType = "UNIQUE ";
		}
		return (keyset.isPrimary() ? "PRIMARY KEY ":indexType+" ")+name.unescaped()+" "+"("+keyset.stream().map(f -> column(f.getToken())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
	}

	default String dropIndex(TableToken tableName, IndexToken index, boolean isPrimary) {
		String indexName;
		if (isPrimary) {
			indexName = "PRIMARY KEY";
		} else {
			indexName = "INDEX "+index;
		}
		return "ALTER TABLE "+tableName+" "+"DROP "+indexName;
	}
}
