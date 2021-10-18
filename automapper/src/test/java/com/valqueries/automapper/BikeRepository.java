package com.valqueries.automapper;

import javax.inject.Inject;

public class BikeRepository extends ValqueriesCrudRepositoryImpl<Bike, String> {
	@Inject
	public BikeRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Bike.class, String.class);
	}
}
