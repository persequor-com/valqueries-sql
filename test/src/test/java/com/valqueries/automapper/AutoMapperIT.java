package com.valqueries.automapper;

import com.google.inject.Guice;
import com.valqueries.Database;
import com.valqueries.IOrm;
import io.ran.CrudRepository;
import io.ran.GenericFactory;
import io.ran.Resolver;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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


public abstract class AutoMapperIT extends AutoMapperBaseTests {
	abstract Database database();

	@Override
	protected void setInjector() {
		database = database();
		GuiceModule module = new GuiceModule(database, ValqueriesResolver.class);
		injector = Guice.createInjector(module);
		factory = injector.getInstance(GenericFactory.class);
	}

	@Before
	public void setup() {
		sqlGenerator = injector.getInstance(SqlGenerator.class);

		try (IOrm orm = database.getOrm()) {
			List<Class> clazzes = Arrays.asList(Car.class, Door.class, Engine.class, EngineCar.class, Exhaust.class, Tire.class, WithCollections.class, Bike.class, BikeGear.class, BikeGearBike.class, BikeWheel.class, PrimaryKeyModel.class, Bipod.class, Pod.class, AllFieldTypes.class);
			clazzes.forEach(c -> {
				TypeDescriber desc = TypeDescriberImpl.getTypeDescriber(c);
				try {
					orm.update("DROP TABLE " + sqlGenerator.getTableName(desc) + ";");
				} catch (Exception e) {

				}
				orm.update(sqlGenerator.generateCreateTable(desc));
			});

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

		Mockito.verifyNoInteractions(resolver);
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

		Mockito.verifyNoInteractions(resolver);
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

		Mockito.verifyNoInteractions(resolver);
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

	@Test
	public void twoRelationsToSameClassOnOneObject() throws Throwable {
		Pod pod1 = factory.get(Pod.class);
		pod1.setId("pod1");
		pod1.setName("Pod number 1");

		Pod pod2 = factory.get(Pod.class);
		pod2.setId("pod2");
		pod2.setName("Pod number 2");

		Bipod bipod = factory.get(Bipod.class);
		bipod.setId(UUID.randomUUID().toString());
		bipod.setPod1(pod1);
		bipod.setPod2(pod2);

		podRepository.save(bipod);


		Bipod actual = podRepository.getEagerBipod(bipod.getId());
		actual.getClass().getMethod("_resolverInject", Resolver.class).invoke(actual, resolver);

		assertEquals(bipod.getId(), actual.getId());
		assertEquals("Pod number 1", actual.getPod1().getName());
		assertEquals("Pod number 2", actual.getPod2().getName());

		Mockito.verifyNoInteractions(resolver);
	}
}
