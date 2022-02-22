package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.UUID;

@Mapper(dbType = Valqueries.class)
public class Door {
	@PrimaryKey
	private UUID id;
	private String title;
	@Relation(fields = "carId", relationFields = "id")
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
		this.car = car;
		if (car != null) {
			this.carId = car.getId();
		}
	}

	public UUID getCarId() {
		return carId;
	}

	public void setCarId(UUID carId) {
		this.carId = carId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Door door = (Door) o;

		return id.equals(door.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
