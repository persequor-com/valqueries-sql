package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.GenericFactory;

import javax.inject.Inject;
import java.util.UUID;

public class ExhaustRepository extends ValqueriesCrudRepositoryImpl<Exhaust, UUID> {
	@Inject
	public ExhaustRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Exhaust.class, UUID.class);
	}
}
