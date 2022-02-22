
package com.valqueries;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//not thread-safe
public class NamedSqlStatement {
	private static final Pattern PARAMETER_REGEXP = Pattern.compile(":([_a-zA-Z0-9]+)");

	private Matcher parametersMatcher;

	public NamedSqlStatement() {
	}

	public NamedSqlStatement(String sqlString) {
		setSqlString(sqlString);
	}

	public void setSqlString(String sqlString) {
		parametersMatcher = PARAMETER_REGEXP.matcher(sqlString);
	}

	protected Matcher getParametersMatcher() {
		return parametersMatcher.reset();
	}


	public String toJdbcQuery() {
		Matcher parametersMatcher = getParametersMatcher();
		StringBuffer jdbcQuery = new StringBuffer();
		while (parametersMatcher.find()) {
			parametersMatcher.appendReplacement(jdbcQuery, getJdbcPlaceHolder(parametersMatcher.group(1)));
		}
		parametersMatcher.appendTail(jdbcQuery);
		return jdbcQuery.toString();
	}

	protected String getJdbcPlaceHolder(String parameterName) {
		return "?";
	}

	public List<String> getParameterNames() {
		Matcher parametersMatcher = getParametersMatcher();
		List<String> parameters = new ArrayList<>();
		while (parametersMatcher.find()) {
			parameters.add(parametersMatcher.group(1));
		}
		return parameters;
	}
}
