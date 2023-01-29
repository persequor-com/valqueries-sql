package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

public class SimpleElement implements Element {
	private final ValqueriesQueryImpl<?> query;
	private final Property.PropertyValue<?> propertyValue;
	private final String operator;
	private final String field;
	private SqlDialect dialect;

	public SimpleElement(ValqueriesQueryImpl<?> query, Property.PropertyValue<?> propertyValue, String operator, int fieldNum, SqlDialect dialect) {
		this.query = query;
		this.propertyValue = propertyValue;
		this.operator = operator;
		this.dialect = dialect;
		this.field = query.getTableAlias() + "_" + propertyValue.getProperty().getToken().snake_case() + fieldNum;
	}

	public String queryString() {
		return query.getTableAlias()+"."+dialect.column(propertyValue.getProperty())+" "+operator+" (:"+field+")";
	}

	@Override
	public void set(IStatement statement) {
		statement.set(field, propertyValue.getValue());
	}
}