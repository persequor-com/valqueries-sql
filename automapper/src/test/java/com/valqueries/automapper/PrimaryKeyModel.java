package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;

@Mapper(dbType = Valqueries.class)
public class PrimaryKeyModel {
	@PrimaryKey
	private String first;
	@PrimaryKey
	private String second;

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}
}
