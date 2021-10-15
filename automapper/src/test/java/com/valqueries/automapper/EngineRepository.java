package com.valqueries.automapper;

import io.ran.GenericFactory;

import javax.inject.Inject;
import java.util.UUID;

public class EngineRepository extends ValqueriesCrudRepositoryImpl<Engine, UUID> {
	private EngineCarRepository engineCarRepository;
	private final GenericFactory genericFactory;

	@Inject
	public EngineRepository(ValqueriesRepositoryFactory factory, EngineCarRepository engineCarRepository, GenericFactory genericFactory) {
		super(factory, Engine.class, UUID.class);
		this.engineCarRepository = engineCarRepository;
		this.genericFactory = genericFactory;
	}

	@Override
	public CrudUpdateResult save(Engine entity) {
		return obtainInTransaction(tx -> {
			entity.getCars().forEach(car -> {
				EngineCar engineCar = genericFactory.get(EngineCar.class);
				engineCar.setCarId(car.getId());
				engineCar.setEngineId(entity.getId());
				engineCarRepository.save(tx, engineCar);
			});
			return super.save(tx, entity);
		});
	}
}
