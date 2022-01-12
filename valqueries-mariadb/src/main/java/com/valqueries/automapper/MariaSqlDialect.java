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
	public TableToken getTableName(Clazz<?> modeltype) {
		return table(Token.get(modeltype.clazz.getSimpleName()));
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
	public String getLimitDefinition(int offset, Integer limit) {
		return " LIMIT "+offset+","+limit;
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

	public String generateIndexOnCreateStatement(TableToken name, KeySet keyset, boolean isUnique) {
		String indexType = "INDEX "+keyset.getName();
		if (keyset.isPrimary()) {
			indexType = "PRIMARY KEY";
		} else if (isUnique) {
			indexType = "UNIQUE "+ keyset.getName();
		}
		return indexType+" "+"("+keyset.stream().map(f -> column(f.getToken())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
	}

	@Override
	public String generatePrimaryKeyStatement(TableToken name, KeySet key, boolean isUnique) {
		return "PRIMARY KEY ("+key.stream().map(f -> column(f.getToken())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
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
		String index = keyName+" ("+key.stream().map(f -> column(f.getToken())).collect(Collectors.toCollection(FormattingTokenList::new)).join(", ")+")";
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

}
