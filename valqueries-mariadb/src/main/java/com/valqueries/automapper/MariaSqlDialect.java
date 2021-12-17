package com.valqueries.automapper;

import io.ran.Clazz;
import io.ran.token.Token;

import java.util.stream.Collectors;

public class MariaSqlDialect implements SqlDialect {
	private SqlNameFormatter sqlNameFormatter;

	public MariaSqlDialect(SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	public String escapeColumnOrTable(String name) {
		return "`"+name+"`";
	}

	@Override
	public <O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		String sql = "INSERT INTO "+getTableName(Clazz.of(oClass))+" ("+columnizer.getColumns().stream().map(s -> "`"+s+"`").collect(Collectors.joining(", "))+") values "+(columnizer.getValueTokens().stream().map(tokens -> "("+tokens.stream().map(t -> ":"+t).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", ")));

		if (!columnizer.getColumnsWithoutKey().isEmpty()) {
			sql += " on duplicate key update "+columnizer.getColumnsWithoutKey().stream().distinct().map(column -> "`"+column+"` = VALUES(`"+column+"`)").collect(Collectors.joining(", "));
		} else {
			sql += " on duplicate key update "+columnizer.getColumns().stream().distinct().map(column -> "`"+column+"` = VALUES(`"+column+"`)").collect(Collectors.joining(", "));
		}
		return sql;
	}

	@Override
	public String getTableName(Clazz<?> modeltype) {
		return escapeColumnOrTable(sqlNameFormatter.table(modeltype.clazz));
	}

	@Override
	public String column(Token token) {
		return sqlNameFormatter.column(token);
	}

	@Override
	public String limit(int offset, Integer limit) {
		return " LIMIT "+offset+","+limit;
	}


}
