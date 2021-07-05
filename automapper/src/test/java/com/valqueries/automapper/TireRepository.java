package com.valqueries.automapper;

import javax.inject.Inject;

public class TireRepository extends ValqueriesCrudRepositoryImpl<Tire, Tire> {
	@Inject
	public TireRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Tire.class, Tire.class);
	}
}
