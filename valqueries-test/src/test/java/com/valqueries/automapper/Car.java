package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(dbType = Valqueries.class)
public class Car {
	@PrimaryKey
	private UUID id;
	@Fulltext
//	@MappedType("TEXT")
	private String title;
	private String key;
	private Brand brand;
	private ZonedDateTime createdAt;
	private UUID exhaustId;
	private List<Integer> numbers = new ArrayList<>();
	@Relation(collectionElementType = Door.class, fields = "id", relationFields = "carId", autoSave = true)
	private List<Door> doors;
	@Relation
	private Exhaust exhaust;
	@Relation(collectionElementType = Driver.class, via = DriverCar.class, autoSave = true)
	List<Driver> drivers;
	@Relation(collectionElementType = Engine.class, via = EngineCar.class, autoSave = true)
	private List<Engine> engines;
	@Relation(collectionElementType = CarWheel.class, fields = "id", relationFields = "carId", autoSave = true)
	private List<CarWheel> wheels;

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

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<Door> getDoors() {
		return doors;
	}

	public void setDoors(List<Door> doors) {
		this.doors = doors;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	public List<Integer> getNumbers() {
		return numbers;
	}

	public void setNumbers(List<Integer> numbers) {
		this.numbers = numbers;
	}

	public Exhaust getExhaust() {
		return exhaust;
	}

	public void setExhaust(Exhaust exhaust) {
		this.exhaust = exhaust;
		if (exhaust != null) {
			this.exhaustId = exhaust.getId();
		}
	}

	public UUID getExhaustId() {
		return exhaustId;
	}

	public void setExhaustId(UUID exhaustId) {
		this.exhaustId = exhaustId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Driver> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<Driver> drivers) {
		this.drivers = drivers;
	}

	public List<Engine> getEngines() {
		return engines;
	}

	public void setEngines(List<Engine> engines) {
		this.engines = engines;
	}

	public List<CarWheel> getWheels() {
		return wheels;
	}

	public void setWheels(List<CarWheel> wheels) {
		this.wheels = wheels;
	}
}
