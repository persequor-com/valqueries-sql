package com.valqueries.automapper;

import javax.inject.Inject;

public class PrimaryKeyModelRepository extends ValqueriesCrudRepositoryImpl<PrimaryKeyModel, PrimaryKeyModel> {
	@Inject
	public PrimaryKeyModelRepository(ValqueriesRepositoryFactory factory) {
		super(factory, PrimaryKeyModel.class, PrimaryKeyModel.class);
	}
}
