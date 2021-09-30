package com.valqueries.automapper;

import io.ran.Clazz;
import io.ran.CompoundKey;
import io.ran.Key;
import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Property;
import io.ran.Relation;
import io.ran.token.Token;

import java.util.UUID;

@Mapper(dbType = Valqueries.class)
public class EngineCar {
	@PrimaryKey
	private UUID carId;
	@PrimaryKey
	private UUID engineId;
	@Relation
	private Car car;
	@Relation
	private Engine engine;

	public UUID getCarId() {
		return carId;
	}

	public void setCarId(UUID carId) {
		this.carId = carId;
	}

	public UUID getEngineId() {
		return engineId;
	}

	public void setEngineId(UUID engineId) {
		this.engineId = engineId;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Engine getEngine() {
		return engine;
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
	}
}
