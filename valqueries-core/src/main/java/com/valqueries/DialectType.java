package com.valqueries;

import java.util.Arrays;

public enum DialectType {
	MariaDB("mysql", "com.valqueries.automapper.MariaSqlDialect")
	, MsSql("sqlserver", "com.valqueries.automapper.MssqlSqlDialect")
	, H2("h2", "com.valqueries.automapper.H2SqlDialect");

	private String jdbcName;
	private String dialect;

	DialectType(String jdbcName, String dialect) {
		this.jdbcName = jdbcName;
		this.dialect = dialect;
	}

	public String getJdbcName() {
		return jdbcName;
	}

	public String getDialect() {
		return dialect;
	}

	public static DialectType from(String jdbcName) {
		return Arrays.asList(values()).stream().filter(dialectType -> dialectType.jdbcName.equals(jdbcName)).findFirst().orElseThrow(() -> new RuntimeException("Unknown dialect type: "+jdbcName));
	}
}
