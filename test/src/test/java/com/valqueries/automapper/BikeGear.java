package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;
import io.ran.Relation;

import java.util.List;

@Mapper(dbType = Valqueries.class)
public class BikeGear {
	@PrimaryKey
	int gearNum;

	@Relation(collectionElementType = Bike.class, via = BikeGearBike.class)
	private List<Bike> bike;

	public int getGearNum() {
		return gearNum;
	}

	public void setGearNum(int gearNum) {
		this.gearNum = gearNum;
	}

	public List<Bike> getBike() {
		return bike;
	}

	public void setBike(List<Bike> bike) {
		this.bike = bike;
	}
}
