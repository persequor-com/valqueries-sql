package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

public class FreeTextElement implements Element {

	private final ValqueriesQueryImpl<?> query;
	private final Property.PropertyValue<?> propertyValue;
	private SqlNameFormatter sqlNameFormatter;
	private final String field;
	private SqlDialect dialect;

	public FreeTextElement(ValqueriesQueryImpl<?> query, Property.PropertyValue<?> propertyValue, int fieldNum, SqlNameFormatter sqlNameFormatter, SqlDialect dialect) {
		this.query = query;
		this.propertyValue = propertyValue;
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
		this.field = propertyValue.getProperty().getToken().snake_case()+fieldNum;
	}

	@Override
	public String queryString() {
		return "MATCH("+query.getTableAlias()+"."+dialect.escapeColumnOrTable(sqlNameFormatter.column(propertyValue.getProperty().getToken()))+") AGAINST(:"+field+")";
	}

	@Override
	public void set(IStatement statement) {
		statement.set(field, propertyValue.getValue());
	}

}
