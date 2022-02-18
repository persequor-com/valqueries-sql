package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

import java.util.List;
import java.util.stream.Collectors;

public class ListElement<T> implements Element {
	private final ValqueriesQueryImpl<T> query;
	private final Property.PropertyValueList<?> values;
	private final String operator;
	private final int fieldNum;
	private SqlNameFormatter sqlNameFormatter;
	private final String field;
	private final SqlDialect dialect;

	public ListElement(ValqueriesQueryImpl<T> query, Property.PropertyValueList<?> values, String operator, int fieldNum, SqlNameFormatter sqlNameFormatter, SqlDialect dialect) {
		this.query = query;
		this.values = values;
		this.operator = operator;
		this.fieldNum = fieldNum;
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
		this.field = query.getTableAlias() + "_" + values.getProperty().getToken().snake_case() + fieldNum;
	}

	public String queryString() {
		return query.getTableAlias()+"."+dialect.column(values.getProperty().getToken())+" "+operator+" (:"+field+")";
	}

	@Override
	public void set(IStatement statement) {
		if (values.isEmpty()) {
			query.setEmpty();
		} else {
			statement.set(field, values.stream().map(Property.PropertyValue::getValue).collect(Collectors.toList()));
		}
	}
}
