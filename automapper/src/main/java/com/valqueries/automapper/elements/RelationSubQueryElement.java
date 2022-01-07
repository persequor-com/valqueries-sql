package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.RelationDescriber;

public class RelationSubQueryElement implements Element {
	private final ValqueriesQueryImpl<?> otherQuery;
	private SqlNameFormatter sqlNameFormatter;
	private final RelationDescriber relation;
	private final String tableAlias;
	private String parentTableAlias;
	private int subQueryNum;
	private SqlDialect dialect;

	public RelationSubQueryElement(String parentTableAlias, String tableAlias, int subQueryNum, RelationDescriber relation, ValqueriesQueryImpl<?> otherQuery, SqlNameFormatter sqlNameFormatter, SqlDialect dialect) {
		this.parentTableAlias = parentTableAlias;
		this.tableAlias = tableAlias;
		this.subQueryNum = subQueryNum;
		this.relation = relation;
		this.otherQuery = otherQuery;
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
	}

	public String queryString() {
		return parentTableAlias + "." + dialect.escapeColumnOrTable(sqlNameFormatter.column(relation.getFromKeys().get(0).getToken())) + " IN (" + otherQuery.buildSelectSql(tableAlias, relation.getToKeys().stream().map(p -> tableAlias + "." + sqlNameFormatter.column(p.getToken())).toArray(String[]::new)) + ")";

	}

	@Override
	public void set(IStatement statement) {
		otherQuery.set(statement);
	}
}
