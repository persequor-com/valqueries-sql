package com.valqueries.automapper;

import com.valqueries.OrmResultSet;
import com.valqueries.automapper.elements.Element;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.token.Token;

import java.util.List;
import java.util.stream.Collectors;

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
		String sql = "INSERT INTO "+getTableName(Clazz.of(oClass))+" ("+columnizer.getColumns().stream().map(s -> "`"+s+"`").collect(Collectors.joining(", "))+") values "+(columnizer.getValueTokens().stream().map(tokens -> "("+tokens.stream().map(t -> ":"+t).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", ")));

		if (!columnizer.getColumnsWithoutKey().isEmpty()) {
			sql += " on duplicate key update "+columnizer.getColumnsWithoutKey().stream().distinct().map(column -> "`"+column+"` = VALUES(`"+column+"`)").collect(Collectors.joining(", "));
		} else {
			sql += " on duplicate key update "+columnizer.getColumns().stream().distinct().map(column -> "`"+column+"` = VALUES(`"+column+"`)").collect(Collectors.joining(", "));
		}
		return sql;
	}

	@Override
	public String getTableName(Clazz<?> modeltype) {
		return escapeColumnOrTable(sqlNameFormatter.table(modeltype.clazz));
	}

	@Override
	public String createTableStatement() {
		return "CREATE TABLE IF NOT EXISTS ";
	}

	@Override
	public String column(Token token) {
		return sqlNameFormatter.column(token);
	}

	@Override
	public String limit(int offset, Integer limit) {
		return " LIMIT "+offset+","+limit;
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
	public String changeColumn(String tablename, String columnName, String sqlType) {
		return "ALTER TABLE " + tablename + " CHANGE COLUMN " + escapeColumnOrTable(columnName) + " " + escapeColumnOrTable(columnName) + " " + sqlType + ";";
	}

	@Override
	public String addIndex(String tablename, KeySet key) {
		return "ALTER TABLE " + tablename + " ADD " + getIndex(key) + ";";
	}

	public SqlDescriber.DbRow getDbRow(OrmResultSet ormResultSet) {
		try {
			return new SqlDescriber.DbRow(ormResultSet.getString("Field"), ormResultSet.getString("Type"), ormResultSet.getString("Null").equals("Yes") ? true : false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
