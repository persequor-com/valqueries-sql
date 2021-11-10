package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;

@Mapper(dbType = Valqueries.class)
public class Pod {
	@PrimaryKey
	private String id;
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
