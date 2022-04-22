package com.valqueries.automapper.schema;

import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import io.ran.DbName;
import io.ran.Property;
import io.ran.token.ColumnToken;
import io.ran.token.Token;

public class ValqueriesColumnToken extends ColumnToken {
	private SqlNameFormatter sqlNameFormatter;
	private final SqlDialect dialect;

	public ValqueriesColumnToken(SqlNameFormatter sqlNameFormatter, SqlDialect dialect, Property property) {
		super(property);
		DbName dbName = property.getAnnotations().get(DbName.class);
		if (dbName != null) {
			this.specifiedName = dbName.value();
		}
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
	}

	public ValqueriesColumnToken(SqlNameFormatter sqlNameFormatter, SqlDialect dialect, Token token) {
		super(token);
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
	}

	public ValqueriesColumnToken(SqlNameFormatter sqlNameFormatter, SqlDialect dialect, String specifiedName) {
		super(specifiedName);
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
	}

	@Override
	public String toSql() {
		return dialect.escapeColumnOrTable(unescaped());
	}

	@Override
	public String unescaped() {
		return specifiedName != null ? specifiedName : sqlNameFormatter.column(token);
	}
}
