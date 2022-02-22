package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.RelationDescriber;

import java.util.stream.Collectors;

public class RelationSubQueryElement implements Element {
	private final ValqueriesQueryImpl<?> otherQuery;
	private SqlNameFormatter sqlNameFormatter;
	private final RelationDescriber relation;
	private final String tableAlias;
	private String parentTableAlias;
	private SqlDialect dialect;

	public RelationSubQueryElement(String parentTableAlias, String tableAlias, RelationDescriber relation, ValqueriesQueryImpl<?> otherQuery, SqlNameFormatter sqlNameFormatter, SqlDialect dialect) {
		this.parentTableAlias = parentTableAlias;
		this.tableAlias = tableAlias;
		this.relation = relation;
		this.otherQuery = otherQuery;
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
	}

	public String queryString() {
		return "(" + relation.getFromKeys().stream().map(key -> parentTableAlias + "." + dialect.escapeColumnOrTable(sqlNameFormatter.column(key.getToken()))).collect(Collectors.joining(", ")) + ")" +
				" IN (" + otherQuery.buildSelectSql(tableAlias, relation.getToKeys().stream().map(p -> tableAlias + "." + sqlNameFormatter.column(p.getToken())).toArray(String[]::new)) + ")";

	}

	@Override
	public void set(IStatement statement) {
		otherQuery.set(statement);
	}
}
