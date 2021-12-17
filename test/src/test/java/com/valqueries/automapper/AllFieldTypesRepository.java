package com.valqueries.automapper;

import javax.inject.Inject;
import java.util.UUID;

public class AllFieldTypesRepository extends ValqueriesCrudRepositoryImpl<AllFieldTypes, UUID> {
	@Inject
	public AllFieldTypesRepository(ValqueriesRepositoryFactory factory) {
		super(factory, AllFieldTypes.class, UUID.class);
	}
}
