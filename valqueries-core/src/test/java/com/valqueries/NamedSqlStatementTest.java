
package com.valqueries;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NamedSqlStatementTest {

	@Test
	public void namedToJdbc() {
		NamedSqlStatement namedSql = new NamedSqlStatement("insert into it_orm_basic set id = :id, moment_in_time = :moment_in_time");

		assertEquals("insert into it_orm_basic set id = ?, moment_in_time = ?", namedSql.toJdbcQuery());
	}

	@Test
	public void getParameters() {
		NamedSqlStatement namedSql = new NamedSqlStatement("insert into it_orm_basic set id = :id, moment_in_time = :moment_in_time");

		assertEquals(Arrays.asList("id", "moment_in_time"), namedSql.getParameterNames());
	}

	@Test
	public void toJdbcFirstGetParamsNext() {
		NamedSqlStatement namedSql = new NamedSqlStatement("insert into it_orm_basic set id = :id, moment_in_time = :moment_in_time");

		assertEquals("insert into it_orm_basic set id = ?, moment_in_time = ?", namedSql.toJdbcQuery());
		assertEquals(Arrays.asList("id", "moment_in_time"), namedSql.getParameterNames());
	}
}