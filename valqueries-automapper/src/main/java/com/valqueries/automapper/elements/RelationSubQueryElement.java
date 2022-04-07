package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.KeySet;
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
		String res = "";
		for(int i=0;i<relation.getFromKeys().size();i++) {
			KeySet.Field key = relation.getFromKeys().get(i);
			KeySet.Field toKey = relation.getToKeys().get(i);
			res += (res.length() > 0 ? " AND " : "")+tableAlias+"."+dialect.escapeColumnOrTable(sqlNameFormatter.column(toKey.getToken()))+ " = "+parentTableAlias + "." + dialect.escapeColumnOrTable(sqlNameFormatter.column(key.getToken()));
		}

		return "exists ("+otherQuery.buildSimpleSelectSql(tableAlias, res, relation.getToKeys().stream().map(p -> tableAlias + "." + sqlNameFormatter.column(p.getToken())).collect(Collectors.toList()))+")";
	}

	@Override
	public void set(IStatement statement) {
		otherQuery.set(statement);
	}
}
