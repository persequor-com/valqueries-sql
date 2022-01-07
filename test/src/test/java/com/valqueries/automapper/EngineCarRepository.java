package com.valqueries.automapper;

import com.valqueries.Database;
//import com.valqueries.automapper.generated.EngineCarKey;
import io.ran.GenericFactory;

import javax.inject.Inject;

public class EngineCarRepository extends ValqueriesCrudRepositoryImpl<EngineCar, EngineCarKey> {
	@Inject
	public EngineCarRepository(ValqueriesRepositoryFactory factory) {
		super(factory, EngineCar.class, EngineCarKey.class);
	}
}
