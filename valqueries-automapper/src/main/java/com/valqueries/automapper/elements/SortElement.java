package com.valqueries.automapper.elements;

import com.valqueries.IStatement;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;

public class SortElement<T> implements Element {
	private final ValqueriesQueryImpl<T> query;
	private final Property<?> property;
	private boolean ascending;
	private SqlDialect dialect;

	public SortElement(ValqueriesQueryImpl<T> query, Property<?> property, boolean ascending, SqlDialect dialect) {
		this.query = query;
		this.property = property;
		this.ascending = ascending;
		this.dialect = dialect;
	}

	@Override
	public String queryString() {
		return query.getTableAlias()+"."+dialect.column(property)+" "+(ascending ? "ASC" : "DESC");
	}

	@Override
	public void set(IStatement statement) {

	}
}