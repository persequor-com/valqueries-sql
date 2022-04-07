package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import com.valqueries.automapper.elements.Element;
import com.valqueries.automapper.schema.ValqueriesColumnToken;
import com.valqueries.automapper.schema.ValqueriesTableToken;
import io.ran.*;
import io.ran.token.ColumnToken;
import io.ran.token.IndexToken;
import io.ran.token.TableToken;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface SqlDialect {

	// base methods
	String escapeColumnOrTable(String name);
	default String prepareColumnOrTable(String name) {
		return name;
	}
	default TableToken getTableName(Clazz<? extends Object> modeltype) {
		DbName dbName = modeltype.getAnnotations().get(DbName.class);
		if (dbName != null) {
			return new ValqueriesTableToken(sqlNameFormatter(), this, dbName.value());
		} else {
			return table(Token.get(modeltype.clazz.getSimpleName()));
		}
	}

	SqlNameFormatter sqlNameFormatter();

	default ColumnToken column(Property property) {
		DbName dbName = property.getAnnotations().get(DbName.class);
		if (dbName != null) {
			return new ValqueriesColumnToken(sqlNameFormatter(), this, dbName.value());
		} else {
			return column(property.getToken());
		}
	}
	ColumnToken column(Token token);
	TableToken table(Token token);
	default String getSqlType(Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		Class type = property.getType().clazz;
		if (mappedType != null) {
			return mappedType.value();
		}
		Serialized serialized = property.getAnnotations().get(Serialized.class);
		if (serialized != null) {
			return "VARCHAR(4000)";
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

	// query methods

	<O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass);
	String getLimitDefinition(int offset, Integer limit);
	String generateUpdateStatement(TypeDescriber<?> typeDescriber, List<Element> elements, List<Property.PropertyValue> newPropertyValues);
	
	<O> String getInsert(CompoundColumnizer<O> columnizer, Class<O> oClass);
	
	default String generateDeleteStatement(String tableAlias, TypeDescriber<?> typeDescriber, List<Element> elements, int offset, Integer limit) {
		String sql = "DELETE "+tableAlias+" FROM " + getTableName(Clazz.of(typeDescriber.clazz())) + " AS "+tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		if (limit !=  null) {
			sql += getLimitDefinition(offset, limit);
		}
		return sql;
	}

	// Schema manipulation methods

	default String getAddColumnStatement() {
		return "ADD ";
	}

	default String generateAlterColumnPartStatement(ColumnToken name) {
		return "CHANGE COLUMN "+name+" "+name;
	}


	String generateIndexStatement(TableToken tablename, KeySet key, boolean isUnique);

	String generateDropIndexStatement(TableToken tableName, IndexToken index, boolean isPrimary);

	/// Describe methods

	default String describe(TableToken tablename) {
		return "DESCRIBE " + tablename + "";
	}

	default String describeIndex(TableToken tablename) {
		return "show index from " + tablename + "";
	}

	SqlDescriber.DbRow getDescribeDbResult(OrmResultSet ormResultSet);

	SqlDescriber.DbIndex getDescribeIndexResult(OrmResultSet r);

	String generatePrimaryKeyStatement(TableToken name, KeySet keyset, boolean isUnique);

	default String dropTableStatement(Clazz clazz) {
		return "DROP TABLE IF EXISTS "+getTableName(clazz);
	}

	boolean allowsConversion(Clazz sqlType, String type);

    default String groupConcat(Property<Object> resultProperty, String separator) {
		return "GROUP_CONCAT " + "(" + column(resultProperty.getToken()) + " SEPARATOR '" + separator + "')";
	}
}
