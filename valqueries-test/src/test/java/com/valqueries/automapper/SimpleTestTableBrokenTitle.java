package com.valqueries.automapper;

import io.ran.DbName;
import io.ran.Key;
import io.ran.PrimaryKey;

import java.time.ZonedDateTime;

@DbName("simple_test_table")
public class SimpleTestTableBrokenTitle {
	@PrimaryKey
	private String id;
	private ZonedDateTime title;
	@Key(name = "created_idx")
	private ZonedDateTime createdAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ZonedDateTime getTitle() {
		return title;
	}

	public void setTitle(ZonedDateTime title) {
		this.title = title;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
