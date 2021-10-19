package com.valqueries.automapper;

import com.google.inject.Guice;
import com.valqueries.DataSourceProvider;
import com.valqueries.Database;
import com.valqueries.IOrm;
import io.ran.CrudRepository;
import io.ran.GenericFactory;
import io.ran.Resolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperIT extends AutoMapperBaseTests {
	@Override
	protected void setInjector() {
		database = new Database(DataSourceProvider.get());
		GuiceModule module = new GuiceModule(database, ValqueriesResolver.class);
		injector = Guice.createInjector(module);
		factory = injector.getInstance(GenericFactory.class);
	}

	@Before
	public void setup() {
		try (IOrm orm = database.getOrm()) {
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(carDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(carDescriber));
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(doorDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(doorDescriber));
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(engineDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(engineDescriber));
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(engineCarDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(engineCarDescriber));
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(exhaustDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(exhaustDescriber));
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(tireDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(tireDescriber));

			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(withCollectionsDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(withCollectionsDescriber));

			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(bikeDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(bikeDescriber));
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(bikeGearDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(bikeGearDescriber));

			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(bikeGearBikeDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(bikeGearBikeDescriber));
			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(bikeWheelDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(bikeWheelDescriber));

			orm.update("DROP TABLE IF EXISTS "+sqlGenerator.getTableName(primaryKeyDescriber)+";");
			orm.update(sqlGenerator.generateCreateTable(primaryKeyDescriber));
		}
	}

	@After
	public void cleanup() {

	}

	@Test
	public void eagerLoad() throws Throwable {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);


		Door lazyModel = factory.get(Door.class);
		lazyModel.setId(UUID.randomUUID());
		lazyModel.setTitle("Lazy as such");
		lazyModel.setCar(model);
		doorRepository.save(lazyModel);

		Door lazyModelToo = factory.get(Door.class);
		lazyModelToo.setId(UUID.randomUUID());
		lazyModelToo.setTitle("Lazy as well");
		lazyModelToo.setCar(model);
		doorRepository.save(lazyModelToo);

		Collection<Car> cars = carRepository.getAllEager();
		Class<? extends Car> cl = cars.stream().findFirst().get().getClass();
		cl.getMethod("_resolverInject", Resolver.class).invoke(cars.stream().findFirst().get(), resolver);

		assertEquals(1, cars.size());
		List<Door> doors = cars.stream().findFirst().get().getDoors();
		assertEquals(2, doors.size());

		verifyNoInteractions(resolver);
	}

	@Test
	public void eagerLoad_multiple() throws Throwable {
		Exhaust exhaust = factory.get(Exhaust.class);
		exhaust.setId(UUID.randomUUID());
		exhaust.setBrand(Brand.Porsche);
		CrudRepository.CrudUpdateResult updresult = exhaustRepository.save(exhaust);

		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setExhaust(exhaust);
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Door lazyModel = factory.get(Door.class);
		lazyModel.setId(UUID.randomUUID());
		lazyModel.setTitle("Lazy as such");
		lazyModel.setCar(model);
		doorRepository.save(lazyModel);

		Door lazyModelToo = factory.get(Door.class);
		lazyModelToo.setId(UUID.randomUUID());
		lazyModelToo.setTitle("Lazy as well");
		lazyModelToo.setCar(model);
		doorRepository.save(lazyModelToo);

		Collection<Car> cars = carRepository.query().withEager(Car::getDoors).withEager(Car::getExhaust).execute().collect(Collectors.toList());

		Class<? extends Car> cl = cars.stream().findFirst().get().getClass();
		cl.getMethod("_resolverInject", Resolver.class).invoke(cars.stream().findFirst().get(), resolver);

		assertEquals(1, cars.size());
		List<Door> doors = cars.stream().findFirst().get().getDoors();
		assertEquals(2, doors.size());
		Exhaust actualExhaust = cars.stream().findFirst().get().getExhaust();
		assertEquals(exhaust.getId(), actualExhaust.getId());

		verifyNoInteractions(resolver);
	}

	@Test
	public void eagerLoad_fromCompoundKey() throws Throwable {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Tire tire = factory.get(Tire.class);
		tire.setCar(model);
		tire.setBrand(Brand.Porsche);
		tireRepository.save(tire);

		Tire res = tireRepository.query()
				.eq(Tire::getCarId, model.getId())
				.withEager(Tire::getCar)
				.execute().findFirst().orElseThrow(() -> new RuntimeException());

		res.getClass().getMethod("_resolverInject", Resolver.class).invoke(res, resolver);

		Car actual = res.getCar();
		assertNotNull(actual);

		verifyNoInteractions(resolver);
	}

	@Test
	public void primaryKeyOnlyModel_savedMultipleTimes() throws Throwable {
		PrimaryKeyModel model = factory.get(PrimaryKeyModel.class);
		model.setFirst("1");
		model.setSecond("2");
		primayKeyModelRepository.save(model);
		primayKeyModelRepository.save(model);
	}

	@Test
	public void primaryKeyOnlyModelList_savedMultipleTimes() throws Throwable {
		PrimaryKeyModel model = factory.get(PrimaryKeyModel.class);
		model.setFirst("1");
		model.setSecond("2");
		PrimaryKeyModel model2 = factory.get(PrimaryKeyModel.class);
		model2.setFirst("2");
		model2.setSecond("3");
		primayKeyModelRepository.doRetryableInTransaction(tx -> {
			primayKeyModelRepository.save(tx, Arrays.asList(model, model2));
		});
		primayKeyModelRepository.doRetryableInTransaction(tx -> {
			primayKeyModelRepository.save(tx, Arrays.asList(model, model2));
		});
	}
}
