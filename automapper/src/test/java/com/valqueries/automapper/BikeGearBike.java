package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

@Mapper(dbType = Valqueries.class)
public class BikeGearBike {
	@PrimaryKey
	private String bikeId;
	@PrimaryKey
	private int gearNum;
	@Relation
	private Bike bike;
	@Relation
	private BikeGear bikeGear;

	public String getBikeId() {
		return bikeId;
	}

	public void setBikeId(String bikeId) {
		this.bikeId = bikeId;
	}

	public int getGearNum() {
		return gearNum;
	}

	public void setGearNum(int gearNum) {
		this.gearNum = gearNum;
	}

	public Bike getBike() {
		return bike;
	}

	public void setBike(Bike bike) {
		this.bike = bike;
	}

	public BikeGear getBikeGear() {
		return bikeGear;
	}

	public void setBikeGear(BikeGear bikeGear) {
		this.bikeGear = bikeGear;
	}
}
