package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

@Mapper(dbType = Valqueries.class)
public class Bipod {
	@PrimaryKey
	private String id;
	@Relation(fields = "pod1Id", relationFields = "id", autoSave = true)
	private Pod pod1;
	private String pod1Id;
	@Relation(fields = "pod2Id", relationFields = "id", autoSave = true)
	private Pod pod2;
	private String pod2Id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Pod getPod1() {
		return pod1;
	}

	public void setPod1(Pod pod1) {
		this.pod1 = pod1;
		this.setPod1Id(pod1.getId());
	}

	public String getPod1Id() {
		return pod1Id;
	}

	public void setPod1Id(String pod1Id) {
		this.pod1Id = pod1Id;
	}

	public Pod getPod2() {
		return pod2;
	}

	public void setPod2(Pod pod2) {
		this.pod2 = pod2;
		this.setPod2Id(pod2.getId());
	}

	public String getPod2Id() {
		return pod2Id;
	}

	public void setPod2Id(String pod2Id) {
		this.pod2Id = pod2Id;
	}
}
