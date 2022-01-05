package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import io.ran.Clazz;
import io.ran.Key;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.TypeDescriber;
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

	@Override
	public String update(TypeDescriber<?> typeDescriber, List<ValqueriesQueryImpl.Element> elements, List<Property.PropertyValue> newPropertyValues) {
		StringBuilder updateStatement = new StringBuilder();

		updateStatement.append("UPDATE main SET ");

		String columnsToUpdate = newPropertyValues.stream()
				.map(pv -> "main." + escapeColumnOrTable(column(pv.getProperty().getToken())) + " = :" + pv.getProperty().getToken().snake_case())
				.collect(Collectors.joining(", "));
		updateStatement.append(columnsToUpdate);

		updateStatement.append(" FROM "+getTableName(Clazz.of(typeDescriber.clazz()))+" main");

		if (!elements.isEmpty()) {
			updateStatement.append(" WHERE " + elements.stream().map(ValqueriesQueryImpl.Element::queryString).collect(Collectors.joining(" AND ")));
		}

		return updateStatement.toString();
	}

	public String getTableName(Clazz<? extends Object> modeltype) {
		return escapeColumnOrTable(sqlNameFormatter.table(modeltype.clazz));
	}

	@Override
	public String createTableStatement() {
		return "CREATE TABLE ";
	}

	public String describe(String tablename) {
		return "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = N'"+tablename.replace("[","").replace("]", "")+"' ";
	}

	public String describeIndex(String tablename) {
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
				"      t.name = '" + tablename.replace("[","").replace("]", "") + "'";
	}

	public SqlDescriber.DbRow getDbRow(OrmResultSet ormResultSet) {
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

	public SqlDescriber.DbIndex getDbIndex(OrmResultSet r) {
		try {
			return new SqlDescriber.DbIndex(r.getInt("UniqueConstraint") == 1, r.getString("RealIndexName"), r.getString("IndexName"), r.getString("ColumnName"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String changeColumn(String tablename, String columnName, String sqlType) {
		return "ALTER TABLE " + tablename + " ALTER COLUMN " + escapeColumnOrTable(columnName) + " " + sqlType + ";";
	}

	@Override
	public String addIndex(String tablename, KeySet key) {
		String name = key.get(0).getProperty().getAnnotations().get(Key.class).name();
		return "CREATE INDEX  "+name+" ON " + tablename + " (" + key.stream().map(f -> escapeColumnOrTable(column(f.getToken()))).collect(Collectors.joining(", ")) + ");";
	}

	public String addColumn(String tablename, String columnName, String sqlType) {
		return "ALTER TABLE " + tablename + " ADD " + escapeColumnOrTable(columnName) + " " + sqlType + ";";
	}
}
