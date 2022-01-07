package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;

@Mapper(dbType = Valqueries.class)
public class BikeGear {
	@PrimaryKey
	int gearNum;

	public int getGearNum() {
		return gearNum;
	}

	public void setGearNum(int gearNum) {
		this.gearNum = gearNum;
	}
}
