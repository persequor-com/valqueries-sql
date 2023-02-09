package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

public class SimpleElement implements Element {
	private final ValqueriesQueryImpl<?> query;
	private final Property.PropertyValue<?> propertyValue;
	private final BinaryOperator operator;
	private SqlNameFormatter sqlNameFormatter;
	private final String field;
	private SqlDialect dialect;

	public SimpleElement(ValqueriesQueryImpl<?> query, Property.PropertyValue<?> propertyValue, BinaryOperator operator, int fieldNum, SqlNameFormatter sqlNameFormatter, SqlDialect dialect) {
		this.query = query;
		this.propertyValue = propertyValue;
		this.operator = operator;
		this.dialect = dialect;
		this.field = query.getTableAlias() + "_" + propertyValue.getProperty().getToken().snake_case() + fieldNum;
	}

	public String queryString() {
		return query.getTableAlias()+"."+dialect.column(propertyValue.getProperty())+" "+dialect.operator(operator)+" (:"+field+")";
	}

	@Override
	public void set(IStatement statement) {
		statement.set(field, propertyValue.getValue());
	}
}