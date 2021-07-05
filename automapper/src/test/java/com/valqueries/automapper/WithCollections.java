package com.valqueries.automapper;

import io.ran.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(dbType = Valqueries.class)
public class WithCollections {
	private String id;
	private List<String> field1;
	private Set<String> field2;

	public List<String> getField1() {
		return field1;
	}

	public void setField1(List<String> field1) {
		this.field1 = field1;
	}

	public Set<String> getField2() {
		return field2;
	}

	public void setField2(Set<String> field2) {
		this.field2 = field2;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
