/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2019-08-14
 */
package com.valqueries;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SaveStatementTest {

	private SaveStatement saveStatement;

	@Before
	public void setUp() throws Exception {
		saveStatement = new SaveStatement("my_table");
	}

	@Test
	public void toJdbcQuery() {
		saveStatement.set("id", 1l);
		saveStatement.set("name", "roman");

		assertEquals("INSERT INTO `my_table` SET `id`=?, `name`=? ON DUPLICATE KEY UPDATE `id`=?, `name`=?", saveStatement.toJdbcQuery());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void toJdbcQuery_withCollection() {
		saveStatement.set("id", Arrays.asList("1","2","3"));
	}
}
