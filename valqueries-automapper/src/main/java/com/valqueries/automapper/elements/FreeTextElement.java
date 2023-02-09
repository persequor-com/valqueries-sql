package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

public class FreeTextElement implements Element {

	private final ValqueriesQueryImpl<?> query;
	private final Property.PropertyValue<?> propertyValue;
	private final String field;
	private SqlDialect dialect;

	public FreeTextElement(ValqueriesQueryImpl<?> query, Property.PropertyValue<?> propertyValue, int fieldNum, SqlDialect dialect) {
		this.query = query;
		this.propertyValue = propertyValue;
		this.dialect = dialect;
		this.field = query.getTableAlias() + "_" + propertyValue.getProperty().getToken().snake_case() + fieldNum;
	}

	@Override
	public String queryString() {
		return "MATCH("+query.getTableAlias()+"."+dialect.column(propertyValue.getProperty())+") AGAINST(:"+field+")";
	}

	@Override
	public void set(IStatement statement) {
		statement.set(field, propertyValue.getValue());
	}

}