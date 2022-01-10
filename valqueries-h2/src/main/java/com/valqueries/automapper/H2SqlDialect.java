package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import com.valqueries.automapper.elements.Element;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.token.Token;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class H2SqlDialect implements SqlDialect {
	private SqlNameFormatter sqlNameFormatter;

	public H2SqlDialect(SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	public String escapeColumnOrTable(String name) {
		return "\""+name.toUpperCase()+"\"";
	}

	@Override
	public <O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		String sql =  "MERGE INTO "+getTableName(Clazz.of(oClass))+" as target USING " +
				"(VALUES "+columnizer.getValueTokens().stream().map((l) -> "("+l.stream().map(e -> ":"+e).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", "))+
				") incoming ("+columnizer.getFields().entrySet().stream().map((e) -> escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", "))+") on "+columnizer.getKeys().stream().map(k -> "target."+escapeColumnOrTable(k)+" = incoming."+escapeColumnOrTable(k)).collect(Collectors.joining(" AND "))+
				(columnizer.getFieldsWithoutKeys().size() > 0 ? " WHEN MATCHED THEN UPDATE SET "+columnizer.getFieldsWithoutKeys().entrySet().stream().map(e -> escapeColumnOrTable(e.getValue())+" = incoming."+escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", ")):"")+
				" WHEN NOT MATCHED THEN INSERT ("+columnizer.getFields().entrySet().stream().map(e -> escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", "))+") " +
				"VALUES ("+columnizer.getFields().entrySet().stream().map(e -> "incoming."+escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", "))+");";

		return sql;
	}

	public String getSqlType(Class type, Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		if (mappedType != null) {
			return mappedType.value();
		}
		if (type == UUID.class) {
			return "UUID";
		}
		if (type == boolean.class || type == Boolean.class) {
			return "BOOLEAN";
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
	public String update(TypeDescriber<?> typeDescriber, List<Element> elements, List<Property.PropertyValue> newPropertyValues) {
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
	public SqlDescriber.DbRow getDbRow(OrmResultSet ormResultSet) {
		// The H2 does not yet support any sql generator related methods
		return null;
	}


	@Override
	public String changeColumn(String tablename, String columnName, String sqlType) {
		return "ALTER TABLE " + tablename + " ALTER COLUMN " + escapeColumnOrTable(columnName) + " " + escapeColumnOrTable(columnName) + " " + sqlType + ";";
	}

	@Override
	public String addIndex(String tablename, KeySet key, boolean isUnique) {
		return "CREATE INDEX "+escapeColumnOrTable(key.getName())+" ON " + tablename + " (" + key.stream().map(f -> escapeColumnOrTable(column(f.getToken()))).collect(Collectors.joining(", "))+")" + ";";
	}

	public String getTableName(Clazz<? extends Object> modeltype) {
		return escapeColumnOrTable(sqlNameFormatter.table(modeltype.clazz));
	}

	@Override
	public String getTableName(Token token) {
		return escapeColumnOrTable(sqlNameFormatter.table(token));
	}

	@Override
	public String createTableStatement() {
		return "CREATE TABLE ";
	}

	public String delete(String tableAlias, TypeDescriber<?> typeDescriber, List<Element> elements, int offset, Integer limit) {
		String sql = "DELETE FROM " + getTableName(Clazz.of(typeDescriber.clazz()))+ " AS del";
		sql += " WHERE exists (SELECT * FROM "+getTableName(Clazz.of(typeDescriber.clazz()))+" AS "+tableAlias+ " WHERE "+typeDescriber.primaryKeys().stream().map(f -> "del."+column(f.getToken())+" = main."+column(f.getToken())).collect(Collectors.joining(" AND "));
		if (!elements.isEmpty()) {
			sql += " AND "+elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		sql += ")";
		if (limit !=  null) {
			sql += limit(offset, limit);
		}
//		System.out.println(sql);
		return sql;
	}

	@Override
	public String addIndexOnCreate(Token name, KeySet keyset, boolean isUnique) {
		String indexType = "INDEX ";
		if (keyset.isPrimary()) {
			indexType = "PRIMARY KEY";
		} else if (isUnique) {
			indexType = "UNIQUE ";
		}
		return (keyset.isPrimary() ? "PRIMARY KEY ":indexType+" ")+"("+keyset.stream().map(f -> escapeColumnOrTable(column(f.getToken()))).collect(Collectors.joining(", "))+")";
	}

	public String alterColumn(Token name) {
		return "ALTER COLUMN "+escapeColumnOrTable(column(name))+" ";
	}
}
