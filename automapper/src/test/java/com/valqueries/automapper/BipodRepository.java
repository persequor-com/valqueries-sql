package com.valqueries.automapper;

import javax.inject.Inject;

public class BipodRepository extends ValqueriesCrudRepositoryImpl<Bipod, String> {

	@Inject
	public BipodRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Bipod.class, String.class);
	}

	public Bipod getEagerBipod(String id) {
		return query().withEager(Bipod::getPod1).withEager(Bipod::getPod2).execute().findFirst().orElse(null);
	}
}
