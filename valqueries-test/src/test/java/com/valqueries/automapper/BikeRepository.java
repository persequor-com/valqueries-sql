package com.valqueries.automapper;

import javax.inject.Inject;

public class BikeRepository extends ValqueriesCrudRepositoryImpl<Bike, String> {
	@Inject
	public BikeRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Bike.class, String.class);
	}

	public BikeGear getGear(int num) {
		return query(BikeGear.class).eq(BikeGear::getGearNum, num).execute().findFirst().orElseThrow(() -> new RuntimeException("Could not find gear: "+num));
	}

	public void deleteGear(int num) {
		query(BikeGear.class).eq(BikeGear::getGearNum, num).delete();
	}
}
