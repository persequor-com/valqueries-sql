package com.valqueries.automapper;

import io.ran.Mapper;

import javax.inject.Inject;
import java.util.UUID;

@Mapper(dbType = Valqueries.class)
public class Exhaust {
	private UUID id;
	private Brand brand;

	@Inject
	public Exhaust() {}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}
}
