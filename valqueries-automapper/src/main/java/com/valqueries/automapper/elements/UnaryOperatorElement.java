package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

public class UnaryOperatorElement implements Element {
	private final ValqueriesQueryImpl<?> query;
	private final Property<?> property;
	private final String operator;
	private SqlDialect dialect;

	public UnaryOperatorElement(ValqueriesQueryImpl<?> query, Property<?> property, String operator, SqlDialect dialect) {
		this.query = query;
		this.property = property;
		this.operator = operator;
		this.dialect = dialect;
	}

	public String queryString() {
		return query.getTableAlias()+"."+dialect.column(property)+" "+operator;
	}

	@Override
	public void set(IStatement statement) {
		// No value
	}
}