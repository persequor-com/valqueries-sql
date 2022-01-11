package com.valqueries.automapper.schema;

import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import io.ran.token.ColumnToken;
import io.ran.token.TableToken;
import io.ran.token.Token;

public class ValqueriesTableToken extends TableToken {
	private SqlNameFormatter sqlNameFormatter;
	private final SqlDialect dialect;

	public ValqueriesTableToken(SqlNameFormatter sqlNameFormatter, SqlDialect dialect, Token token) {
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
