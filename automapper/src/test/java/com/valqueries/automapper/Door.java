package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.Relation;

import java.util.UUID;

@Mapper(dbType = Valqueries.class)
public class Door {
	private UUID id;
	private String title;
	@Relation()
	private Car car;
	private UUID carId;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.carId = car.getId();
		this.car = car;
	}

	public UUID getCarId() {
		return carId;
	}

	public void setCarId(UUID carId) {
		this.carId = carId;
	}
}
