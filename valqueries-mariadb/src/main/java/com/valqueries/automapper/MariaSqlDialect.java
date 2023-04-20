package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import com.valqueries.automapper.elements.Element;
import com.valqueries.automapper.schema.ValqueriesColumnToken;
import com.valqueries.automapper.schema.ValqueriesTableToken;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.schema.FormattingTokenList;
import io.ran.token.ColumnToken;
import io.ran.token.IndexToken;
import io.ran.token.TableToken;
import io.ran.token.Token;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MariaSqlDialect implements SqlDialect {
	private SqlNameFormatter sqlNameFormatter;

	public MariaSqlDialect(SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	public String escapeColumnOrTable(String name) {
		return "`"+name+"`";
	}

	@Override
	public <O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		String sql = "INSERT INTO "+getTableName(Clazz.of(oClass))+" ("+columnizer.getColumns().stream().map(s -> s).collect(Collectors.joining(", "))+") values "+(columnizer.getValueTokens().stream().map(tokens -> "("+tokens.stream().map(t -> ":"+t).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", ")));

		if (!columnizer.getColumnsWithoutKey().isEmpty()) {
			sql += " on duplicate key update "+columnizer.getColumnsWithoutKey().stream().distinct().map(column -> column+" = VALUES("+column+")").collect(Collectors.joining(", "));
		} else {
			sql += " on duplicate key update "+columnizer.getColumns().stream().distinct().map(column -> column+" = VALUES("+column+")").collect(Collectors.joining(", "));
		}
		return sql;
	}

	@Override
	public ColumnToken column(Token token) {
		return new ValqueriesColumnToken(sqlNameFormatter, this, token);
	}

	@Override
	public TableToken table(Token token) {
		return new ValqueriesTableToken(sqlNameFormatter, this, token);
	}

	@Override
	public SqlNameFormatter sqlNameFormatter() {
		return sqlNameFormatter;
	}

	@Override
	public String getLimitDefinition(int offset, Integer limit) {
		return " LIMIT "+offset+","+limit;
	}

	@Override
	public String generateUpdateStatement(TypeDescriber<?> typeDescriber, List<Element> elements, List<Property.PropertyValue> newPropertyValues, List<Property.PropertyValue> incrementPropertyValues) {
		StringBuilder updateStatement = new StringBuilder();

		updateStatement.append("UPDATE ").append(getTableName(Clazz.of(typeDescriber.clazz()))).append(" as main SET ");

		Stream<String> newValues = newPropertyValues.stream().map(Property.PropertyValue::getProperty).map(
				p -> "main." + column(p) + " = :" + p.getToken().snake_case());
		Stream<String> incrementValues = incrementPropertyValues.stream().map(Property.PropertyValue::getProperty).map(
				p -> "main." + column(p) + " = main." + column(p) + " + :" + p.getToken().snake_case());
		updateStatement.append(Stream.concat(newValues, incrementValues).collect(Collectors.joining(", ")));

		if (!elements.isEmpty()) {
			updateStatement.append(" WHERE ").append(elements.stream().map(Element::queryString).collect(Collectors.joining(" AND ")));
		}

		return updateStatement.toString();

	}

	@Override
	public <O> String getInsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		return "INSERT INTO " +
				getTableName(Clazz.of(oClass)) +
				" (" +
				columnizer.getColumns().stream().map(s ->  s ).collect(Collectors.joining(", ")) +
				") values " +
				columnizer.getValueTokens().stream()
						.map(tokens -> "(" + tokens.stream().map(t -> ":" + t).collect(Collectors.joining(", ")) + ")")
						.collect(Collectors.joining(", "))
				;
	}

	public String generateIndexOnCreateStatement(TableToken name, KeySet keyset, boolean isUnique) {
		String indexType = "INDEX "+keyset.getName();
		if (keyset.isPrimary()) {
			indexType = "PRIMARY KEY";
		} else if (isUnique) {
			indexType = "UNIQUE "+ keyset.getName();
		}
		return indexType+" "+"("+keyset.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
	}

	@Override
	public String getSqlType(Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		if (mappedType != null) {
			return mappedType.value();
		}

		Class<?> type = property.getType().clazz;
		if (type == ZonedDateTime.class || type == Instant.class) {
			return "DATETIME(3)";
		}

		return SqlDialect.super.getSqlType(property);
	}

	@Override
	public String generatePrimaryKeyStatement(TableToken name, KeySet key, boolean isUnique) {
		return "PRIMARY KEY ("+key.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
	}

	public String generateDropIndexStatement(TableToken tableName, IndexToken index, boolean isPrimary) {
		String indexName;
		if (isPrimary) {
			indexName = "PRIMARY KEY";
		} else {
			indexName = "INDEX "+index;
		}
		return "ALTER TABLE "+tableName+" "+"DROP "+indexName;
	}

	@Override
	public String generateIndexStatement(TableToken tablename, KeySet key, boolean isUnique) {
		String name = key.getName();
		String keyName = "INDEX "+escapeColumnOrTable(name);
		if (key.isPrimary()) {
			keyName = "PRIMARY KEY";
		}
		String index = keyName+" ("+key.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
		return "ALTER TABLE " + tablename + " ADD " + index + ";";
	}

	public SqlDescriber.DbRow getDescribeDbResult(OrmResultSet ormResultSet) {
		try {
			return new SqlDescriber.DbRow(ormResultSet.getString("Field"), ormResultSet.getString("Type"), ormResultSet.getString("Null").equals("Yes") ? true : false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public SqlDescriber.DbIndex getDescribeIndexResult(OrmResultSet r) {
		try {
			return new SqlDescriber.DbIndex(r.getInt("Non_unique") == 0, r.getString("Key_name")+" KEY", r.getString("Key_name"), r.getString("Column_name"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean allowsConversion(Clazz sqlType, String type) {
		if (sqlType.clazz == String.class && (type.toLowerCase().contains("char") || type.toLowerCase().contains("text"))) {
			return true;
		}
		return false;
	}
}
