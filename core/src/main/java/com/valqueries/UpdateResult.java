/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2019-08-09
 */
package com.valqueries;

public class UpdateResult {
	private Number lastInsertedId;
	private int affectedRows;

	public void setAffectedRows(int affectedRows) {
		this.affectedRows = affectedRows;
	}

	public int getAffectedRows() {
		return affectedRows;
	}

	public Number getLastInsertedId() {
		return lastInsertedId;
	}

	public void setLastInsertedId(Number lastInsertedId) {
		this.lastInsertedId = lastInsertedId;
	}
}
