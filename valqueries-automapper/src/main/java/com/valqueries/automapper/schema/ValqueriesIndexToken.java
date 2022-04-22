package com.valqueries.automapper.schema;

import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import io.ran.Property;
import io.ran.token.IndexToken;
import io.ran.token.Token;

public class ValqueriesIndexToken extends IndexToken {
	private SqlNameFormatter sqlNameFormatter;
	private final SqlDialect dialect;

	public ValqueriesIndexToken(SqlNameFormatter sqlNameFormatter, SqlDialect dialect, Property property) {
		super(property);
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
	}

	public ValqueriesIndexToken(SqlNameFormatter sqlNameFormatter, SqlDialect dialect, Token token) {
		super(token);
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialect;
	}

	@Override
	public String toSql() {
		return dialect.escapeColumnOrTable(unescaped());
	}

	@Override
	public String unescaped() {
		return sqlNameFormatter.column(token);
	}
}
