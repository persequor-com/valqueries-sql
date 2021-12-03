package com.valqueries.automapper;

import io.ran.Key;
import io.ran.PrimaryKey;

import java.time.ZonedDateTime;

public class SimpleTestTable {
	@PrimaryKey
	private String id;
	private String title;
	@Key(name = "created_idx")
	private ZonedDateTime createdAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
