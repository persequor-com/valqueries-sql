package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

public class UnaryOperatorElement implements Element {
	private final ValqueriesQueryImpl<?> query;
	private final Property<?> property;
	private final UnaryOperator operator;
	private SqlDialect dialect;

	public UnaryOperatorElement(ValqueriesQueryImpl<?> query, Property<?> property, UnaryOperator operator, SqlDialect dialect) {
		this.query = query;
		this.property = property;
		this.operator = operator;
		this.dialect = dialect;
	}

	public String queryString() {
		return query.getTableAlias()+"."+dialect.column(property)+" "+dialect.operator(operator);
	}

	@Override
	public void set(IStatement statement) {
		// No value
	}
}