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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class H2SqlDialect implements SqlDialect {
	private SqlNameFormatter sqlNameFormatter;

	public H2SqlDialect(SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	public String prepareColumnOrTable(String name) {
		return name.toUpperCase();
	}

	@Override
	public String escapeColumnOrTable(String name) {
		return "\""+prepareColumnOrTable(name)+"\"";
	}

	@Override
	public <O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		String sql =  "MERGE INTO "+getTableName(Clazz.of(oClass))+" as target USING " +
				"(VALUES "+columnizer.getValueTokens().stream().map((l) -> "("+l.stream().map(e -> ":"+e).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", "))+
				") incoming ("+columnizer.getFields().entrySet().stream().map((e) -> (e.getValue())).collect(Collectors.joining(", "))+") on "+columnizer.getKeys().stream().map(k -> "target."+(k)+" = incoming."+(k)).collect(Collectors.joining(" AND "))+
				(columnizer.getFieldsWithoutKeys().size() > 0 ? " WHEN MATCHED THEN UPDATE SET "+columnizer.getFieldsWithoutKeys().entrySet().stream().map(e -> (e.getValue())+" = incoming."+(e.getValue())).collect(Collectors.joining(", ")):"")+
				" WHEN NOT MATCHED THEN INSERT ("+columnizer.getFields().entrySet().stream().map(e -> (e.getValue())).collect(Collectors.joining(", "))+") " +
				"VALUES ("+columnizer.getFields().entrySet().stream().map(e -> "incoming."+(e.getValue())).collect(Collectors.joining(", "))+");";

		return sql;
	}

	public String getSqlType(Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		Class type = property.getType().clazz;
		if (mappedType != null) {
			return mappedType.value();
		}
		if (type == String.class) {
			return "character varying(255)";
		}
		if (type == UUID.class) {
			return "UUID";
		}
		if (type == boolean.class || type == Boolean.class) {
			return "BOOLEAN";
		}
		if (type == ZonedDateTime.class) {
			return "timestamp";
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
	public String generateUpdateStatement(TypeDescriber<?> typeDescriber, List<Element> elements, List<Property.PropertyValue> newPropertyValues) {
		StringBuilder updateStatement = new StringBuilder();

		updateStatement.append("UPDATE " + getTableName(Clazz.of(typeDescriber.clazz())) + " as main SET ");

		String columnsToUpdate = newPropertyValues.stream()
				.map(pv -> "main." + sqlNameFormatter.column(pv.getProperty().getToken()) + " = :" + pv.getProperty().getToken().snake_case())
				.collect(Collectors.joining(", "));
		updateStatement.append(columnsToUpdate);

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
				columnizer.getColumns().stream().map(s -> s ).collect(Collectors.joining(", ")) +
				") values " +
				columnizer.getValueTokens().stream()
						.map(tokens -> "(" + tokens.stream().map(t -> ":" + t).collect(Collectors.joining(", ")) + ")")
						.collect(Collectors.joining(", "))
				;
	}

	@Override
	public String describe(TableToken tablename) {
		return "SHOW COLUMNS FROM "+tablename;
	}

	@Override
	public String describeIndex(TableToken tablename) {
//		return "SELECT * FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = '"+tablename.unescaped()+"'";
		return "SELECT * FROM INFORMATION_SCHEMA.INDEX_COLUMNS kcu JOIN INFORMATION_SCHEMA.INDEXES i ON i.TABLE_NAME = kcu.TABLE_NAME AND i.INDEX_NAME = kcu.INDEX_NAME WHERE kcu.TABLE_NAME = '"+tablename.unescaped()+"'";
//		return "SELECT * FROM INFORMATION_SCHEMA.INDEXES i WHERE i.TABLE_NAME = '"+tablename.unescaped()+"'";
//		return "SELECT * FROM INFORMATION_SCHEMA.INDEX_COLUMNS kcu JOIN INFORMATION_SCHEMA.INDEXES i ON i.TABLE_NAME = kcu.TABLE_NAME AND i.INDEX_NAME = kcu.INDEX_NAME";
//		return "SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE";
//		return "SHOW COLUMNS FROM INFORMATION_SCHEMA.INDEXES";
//		return "SHOW TABLES FROM INFORMATION_SCHEMA";
	}

	@Override
	public SqlDescriber.DbIndex getDescribeIndexResult(OrmResultSet ormResultSet) {
		try {
			return new SqlDescriber.DbIndex(
					ormResultSet.getString("INDEX_TYPE_NAME").equals("UNIQUE") || ormResultSet.getString("INDEX_TYPE_NAME").equals("PRIMARY KEY")
					,ormResultSet.getString("INDEX_NAME")
					, ormResultSet.getString("INDEX_TYPE_NAME").equals("PRIMARY KEY")
						? "PRIMARY"
						: ormResultSet.getString("INDEX_NAME").toLowerCase()
					, ormResultSet.getString("COLUMN_NAME").toLowerCase());
		} catch (Exception e) {
			throw new RuntimeException(e);
//			return new SqlDescriber.DbIndex(false, "blah", "blah", "blah");
		}
	}

	public String generateDropIndexStatement(TableToken tableName, IndexToken index, boolean isPrimary) {
		String indexName;
		if (isPrimary) {
			return "ALTER TABLE "+tableName+" "+"DROP PRIMARY KEY";
		} else {
			return "DROP INDEX "+index+"  ON "+tableName;
		}

	}

	@Override
	public SqlDescriber.DbRow getDescribeDbResult(OrmResultSet ormResultSet) {
		try {
			return new SqlDescriber.DbRow(ormResultSet.getString("FIELD").toLowerCase(), ormResultSet.getString("TYPE").toLowerCase(), ormResultSet.getString("NULL").equals("YES"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String generatePrimaryKeyStatement(TableToken name, KeySet key, boolean isUnique) {
		return  "PRIMARY KEY (" + key.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
	}

	@Override
	public String generateIndexStatement(TableToken tablename, KeySet key, boolean isUnique) {

		return "CREATE INDEX "+escapeColumnOrTable(key.getName())+" ON " + tablename + " (" + key.stream().map(f -> column(f.getProperty())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")" + ";";
	}
	public String generateDeleteStatement(String tableAlias, TypeDescriber<?> typeDescriber, List<Element> elements, int offset, Integer limit) {
		String sql = "DELETE FROM " + getTableName(Clazz.of(typeDescriber.clazz()))+ " AS del";
		sql += " WHERE exists (SELECT * FROM "+getTableName(Clazz.of(typeDescriber.clazz()))+" AS "+tableAlias+ " WHERE "+typeDescriber.primaryKeys().stream().map(f -> "del."+column(f.getProperty())+" = main."+column(f.getProperty())).collect(Collectors.joining(" AND "));
		if (!elements.isEmpty()) {
			sql += " AND "+elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		sql += ")";
		if (limit !=  null) {
			sql += getLimitDefinition(offset, limit);
		}
//		System.out.println(sql);
		return sql;
	}

	public String generateAlterColumnPartStatement(ColumnToken name) {
		return "ALTER COLUMN "+name+" ";
	}

	public String dropTableStatement(Clazz clazz) {
		return "DROP TABLE IF EXISTS "+getTableName(clazz)+" CASCADE";
	}

	@Override
	public boolean allowsConversion(Clazz sqlType, String type) {
		if (sqlType.clazz == String.class && (type.toLowerCase().contains("char") || type.toLowerCase().contains("text"))) {
			return true;
		}
		return false;
	}
}
