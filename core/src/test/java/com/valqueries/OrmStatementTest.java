/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class OrmStatementTest {

	@Test
	public void happyPath_collection() {
		OrmStatement statement = new OrmStatement("SELECT * FROM muh WHERE id = :id AND cat IN (:cat) AND another_cat IN (:cat)");
		statement.set("id","1");
		statement.set("cat", Arrays.asList("2","3","4"));

		assertEquals("SELECT * FROM muh WHERE id = ? AND cat IN (?, ?, ?) AND another_cat IN (?, ?, ?)",statement.toJdbcQuery());
	}

	@Test
	public void toJdbcQuery_similarNames_notClashing() {
		OrmStatement statement = new OrmStatement("SELECT * FROM muh WHERE cat IN (:id) AND something = :id_1 AND special = :id_special AND another_cat IN (:id)");
		statement.set("id", Arrays.asList("2","3"));

		assertEquals("SELECT * FROM muh WHERE cat IN (?, ?) AND something = ? AND special = ? AND another_cat IN (?, ?)",statement.toJdbcQuery());
	}

	@Test(expected = IllegalArgumentException.class)
	public void toJdbcQuery_emptyCollection() {
		OrmStatement statement = new OrmStatement("SELECT * FROM muh WHERE id = :id AND cat IN (:cat)");
		statement.set("cat", Collections.emptyList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void toJdbcQuery_nullCollection() {
		OrmStatement statement = new OrmStatement("SELECT * FROM muh WHERE id = :id AND cat IN (:cat)");
		statement.set("cat", (Collection<?>) null);
	}
}
