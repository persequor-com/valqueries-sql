package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.GenericFactory;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;

import javax.inject.Inject;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class CarRepository extends ValqueriesCrudRepositoryImpl<Car, UUID> {
	@Inject
	public CarRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Car.class, UUID.class);
	}

	public Collection<Car> getAllEager() {
		query().subQuery(Car::getExhaust, q -> {
			q.eq(Exhaust::getBrand, Brand.Hyundai);
		});
		return query().withEager(Car::getDoors).execute().collect(Collectors.toList());
	}
}
