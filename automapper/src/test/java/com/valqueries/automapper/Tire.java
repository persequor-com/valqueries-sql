package com.valqueries.automapper;

import io.ran.CompoundKey;
import io.ran.Mapper;
import io.ran.MappingHelper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.UUID;

@Mapper(dbType = Valqueries.class)
public class Tire {
	@PrimaryKey
	private Brand brand;
	@PrimaryKey
	private UUID carId;
	@Relation
	private Car car;

	public UUID getCarId() {
		return carId;
	}

	public void setCarId(UUID carId) {
		this.carId = carId;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.carId = car.getId();
		this.car = car;
	}
}
