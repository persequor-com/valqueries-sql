package com.valqueries.automapper;

import io.ran.Mapper;
import io.ran.PrimaryKey;

@Mapper(dbType = Valqueries.class)
public class BikeWheel {
	@PrimaryKey
	private BikeType bikeType;
	@PrimaryKey
	private int size;

	public BikeType getBikeType() {
		return bikeType;
	}

	public void setBikeType(BikeType bikeType) {
		this.bikeType = bikeType;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
