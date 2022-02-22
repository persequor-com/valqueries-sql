/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2019-08-14
 */
package com.valqueries;

import java.util.Collection;

public class SaveStatement extends OrmStatement {

	private final String tableName;

	public SaveStatement(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public void set(String column, Collection<?> value) {
		throw new UnsupportedOperationException("It is unclear how to generate UPSERT for collection type");
	}

	public String generateSql() {
		StringBuilder setPairs = generateSetPairs();
		return "INSERT INTO `" + tableName + "` SET " +
			setPairs +
			" ON DUPLICATE KEY UPDATE " + setPairs;
	}

	private StringBuilder generateSetPairs() {
		StringBuilder pairs = new StringBuilder();
		String separator = ", ";
		for (String valueName : getValueNames()) {
			pairs.append("`"+valueName+"`").append("=:").append(valueName).append(separator);
		}
		pairs.setLength(pairs.length() - separator.length());
		return pairs;
	}

	@Override
	public String toJdbcQuery() {
		setSqlString(generateSql());
		return super.toJdbcQuery();
	}

}
