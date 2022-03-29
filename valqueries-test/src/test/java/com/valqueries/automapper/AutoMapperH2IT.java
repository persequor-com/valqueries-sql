package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.H2DataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperH2IT extends AutoMapperIT {

	@Override
	Database database() {
		return new Database(H2DataSourceProvider.get());
	}


	/*
	* This test needs to be overridden to accomodate the case sensitive sorting in H2 order by clause. Other SQL databases and the in memory TestDouble database by default have a case insensitive ordering.
	*
	* */
	@Test
	@TestClasses({Car.class, Door.class})
	public void mixedMultiFieldSort_happy() throws Throwable {

		carWithDoors("SUV", Brand.Porsche);
		carWithDoors("Sedan", Brand.Porsche);
		carWithDoors("Sedan", Brand.Hyundai);

		List<List<?>> actual = carRepository.query()
				.subQueryList(Car::getDoors, sq -> {
					sq.in(Door::getTitle, "Nissan door 1");
				})
				.sortAscending(Car::getTitle).sortDescending(Car::getBrand)
				.execute().map(car -> Arrays.asList(car.getTitle(), car.getBrand())).collect(Collectors.toList());

		assertEquals(Arrays.asList(
						Arrays.asList("SUV", Brand.Porsche)
						, Arrays.asList("Sedan", Brand.Porsche)
						, Arrays.asList("Sedan", Brand.Hyundai))
				, actual
		);

		List<List<?>> actualReverse = carRepository.query()
				.subQueryList(Car::getDoors, sq -> {
					sq.in(Door::getTitle, "Nissan door 1");
				})
				.sortDescending(Car::getTitle).sortAscending(Car::getBrand)
				.execute().map(car -> Arrays.asList(car.getTitle(), car.getBrand())).collect(Collectors.toList());

		assertEquals(Arrays.asList(Arrays.asList("Sedan", Brand.Hyundai)
						, Arrays.asList("Sedan", Brand.Porsche)
						, Arrays.asList("SUV", Brand.Porsche)

				)
				, actualReverse
		);
	}
}