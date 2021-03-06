package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.ITransactionContext;
import io.ran.CrudRepository;
import io.ran.GenericFactory;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CarRepository extends ValqueriesCrudRepositoryImpl<Car, UUID> {
	@Inject
	public CarRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Car.class, UUID.class);
	}

	public Collection<Car> getAllEager() {
		return query().withEager(Car::getDoors).execute().collect(Collectors.toList());
	}

	public void updateTitle(Car model, String new_title) {
		CrudUpdateResult r = query().eq(Car::getId, model.getId())
				.update(u -> u.set(Car::getTitle, new_title));
	}
}
