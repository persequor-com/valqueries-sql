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

import java.util.List;
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
		String s = "MERGE " + getTableName(Clazz.of(oClass)) + " as target USING " +
				"(VALUES " + columnizer.getValueTokens().stream().map((l) -> "(" + l.stream().map(e -> ":" + e).collect(Collectors.joining(", ")) + ")").collect(Collectors.joining(", ")) +
				") incoming (" + columnizer.getFields().entrySet().stream().map((e) -> e.getValue() ).collect(Collectors.joining(", ")) + ") on " + columnizer.getKeys().stream().map(k -> "target." + k + " = incoming." + k ).collect(Collectors.joining(" AND ")) +
				(columnizer.getFieldsWithoutKeys().size() > 0 ? " WHEN MATCHED THEN UPDATE SET " + columnizer.getFieldsWithoutKeys().entrySet().stream().map(e ->  e.getValue() + " = incoming." + e.getValue() ).collect(Collectors.joining(", ")) : "") +
				" WHEN NOT MATCHED THEN INSERT (" + columnizer.getFields().entrySet().stream().map(e ->  e.getValue() ).collect(Collectors.joining(", ")) + ") " +
				"VALUES (" + columnizer.getFields().entrySet().stream().map(e -> "incoming." + e.getValue() ).collect(Collectors.joining(", ")) + ");";
		return s;
	}

	public String getSqlType(Property property) {
		Class type = property.getType().clazz;
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
		return SqlDialect.super.getSqlType(property);
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
		return " OFFSET "+offset+" ROWS" +
				"    FETCH NEXT "+limit+" ROWS ONLY";
	}

	@Override
	public String generateUpdateStatement(TypeDescriber<?> typeDescriber, List<Element> elements, List<Property.PropertyValue> newPropertyValues, List<Property.PropertyValue> incrementValues) {
		StringBuilder updateStatement = new StringBuilder();

		updateStatement.append("UPDATE main SET ");

		String columnsToUpdate = newPropertyValues.stream()
				.map(pv -> "main." + column(pv.getProperty()) + " = :" + pv.getProperty().getToken().snake_case())
				.collect(Collectors.joining(", "));
		updateStatement.append(columnsToUpdate);
		columnsToUpdate = incrementValues.stream()
				.map(pv -> "main." + column(pv.getProperty()) + " = main." + column(pv.getProperty()) +" + " +
						" :" + pv.getProperty().getToken().snake_case())
				.collect(Collectors.joining(", "));
		updateStatement.append(columnsToUpdate);

		updateStatement.append(" FROM "+getTableName(Clazz.of(typeDescriber.clazz()))+" main");

		if (!elements.isEmpty()) {
			updateStatement.append(" WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND ")));
		}

		return updateStatement.toString();
	}

	@Override
	public <O> String getInsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		return "INSERT INTO " +
				getTableName(Clazz.of(oClass)) +
				" (" +
				columnizer.getColumns().stream().map(s -> s).collect(Collectors.joining(", ")) +
				") values " +
				columnizer.getValueTokens().stream()
						.map(tokens -> "(" + tokens.stream().map(t -> ":" + t).collect(Collectors.joining(", ")) + ")")
						.collect(Collectors.joining(", "))
				;
	}

	public String describe(TableToken tablename) {
		return "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = N'"+tablename.unescaped()+"' ";
	}

	public String describeIndex(TableToken tablename) {
		return "SELECT\n" +
				"    TableName = t.name,\n" +
				"    RealIndexName = ind.name,\n" +
				"    IndexName = (CASE\n" +
				"                    WHEN ind.is_primary_key = 1\n" +
				"                        THEN 'PRIMARY'\n" +
				"                    ELSE ind.name\n" +
				"                        END),\n" +
				"    ColumnName = col.name,\n" +
				"    UniqueConstraint = ind.is_unique\n" +
				"FROM\n" +
				"    sys.indexes ind\n" +
				"        INNER JOIN\n" +
				"    sys.index_columns ic ON  ind.object_id = ic.object_id and ind.index_id = ic.index_id\n" +
				"        INNER JOIN\n" +
				"    sys.columns col ON ic.object_id = col.object_id and ic.column_id = col.column_id\n" +
				"        INNER JOIN\n" +
				"    sys.tables t ON ind.object_id = t.object_id\n" +
				"WHERE\n" +
				"      t.name = '" + tablename.unescaped() + "'";
	}

	public SqlDescriber.DbRow getDescribeDbResult(OrmResultSet ormResultSet) {
		try {
			return new SqlDescriber.DbRow(ormResultSet.getString("COLUMN_NAME"), getSqlType(ormResultSet), ormResultSet.getString("IS_NULLABLe").equals("YES") ? true: false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getSqlType(OrmResultSet ormResultSet) {
		try {
			String type = ormResultSet.getString("DATA_TYPE");
			Integer length = ormResultSet.getInt("CHARACTER_MAXIMUM_LENGTH");
			if (length != null) {
				return type+"("+length+")";
			} else {
				return type;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public SqlDescriber.DbIndex getDescribeIndexResult(OrmResultSet r) {
		try {
			return new SqlDescriber.DbIndex(r.getInt("UniqueConstraint") == 1, r.getString("RealIndexName"), r.getString("IndexName"), r.getString("ColumnName"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String generatePrimaryKeyStatement(TableToken name, KeySet key, boolean isUnique) {
		String indexType = "CONSTRAINT " + escapeColumnOrTable(name.unescaped() + "_" + key.getName()) + " PRIMARY KEY ";
		return ""+indexType+" (" + key.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ") + ")";
	}

	@Override
	public String generateIndexStatement(TableToken tablename, KeySet key, boolean isUnique) {
		String name = key.getName();
		String keyType = "INDEX ";
		if (key.isPrimary()) {
			keyType = "PRIMARY KEY ";
		}
		return "CREATE "+keyType+" "+name+" ON " + tablename + " (" + key.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ") + ")";
	}

	public String addColumn(TableToken tablename, ColumnToken columnName, String sqlType) {
		return "ALTER TABLE " + tablename + " ADD " + columnName + " " + sqlType + ";";
	}

//	public String getAddColumnStatement() {
//		return "ADD ";
//	}

	public String generateAlterColumnPartStatement(ColumnToken name) {
		return "ALTER COLUMN "+name;
	}

	public String generateIndexOnCreateStatement(TableToken tableName, KeySet keyset, boolean isUnique) {
		String indexType = "INDEX "+escapeColumnOrTable(keyset.getName())+" ";
		if (keyset.isPrimary()) {
			indexType = "CONSTRAINT "+tableName.unescaped()+"_"+keyset.getName()+" PRIMARY KEY ";
		} else if (isUnique) {
			indexType = "UNIQUE "+escapeColumnOrTable(keyset.getName())+" ";
		}

		return indexType+"("+keyset.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
	}

	public String generateDropIndexStatement(TableToken tableName, IndexToken index, boolean isPrimary) {
		String indexName = index.unescaped();
		if (isPrimary) {
			indexName = tableName.unescaped()+"_"+index.unescaped();
//			return "DROP INDEX "+indexName+" ON "+escapeColumnOrTable(column(tableName));
			return "ALTER TABLE "+tableName+ " DROP CONSTRAINT "+indexName;
		}
		return "DROP INDEX "+indexName+" ON "+tableName;
	}

	@Override
	public boolean allowsConversion(Clazz sqlType, String type) {
		if (sqlType.clazz == String.class && (type.toLowerCase().contains("char") || type.toLowerCase().contains("text"))) {
			return true;
		}
		return false;
	}

	@Override
	public String groupConcat(Property<Object> resultProperty, String separator) {
		return "STRING_AGG " + "(" + column(resultProperty) + ", '" + separator + "')";
	}
}
