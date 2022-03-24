package com.valqueries.automapper;

import com.google.inject.Injector;
import com.valqueries.Database;
import io.ran.CrudRepository;
import io.ran.GenericFactory;
import io.ran.Resolver;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public abstract class AutoMapperBaseTests {
	@Rule
	public TestName name = new TestName();
	static Database database;
	static Injector injector;
	static GenericFactory factory;
	static TypeDescriber<Car> carDescriber;
	static TypeDescriber<Door> doorDescriber;
	static TypeDescriber<Engine> engineDescriber;
	static TypeDescriber<EngineCar> engineCarDescriber;
	static TypeDescriber<Tire> tireDescriber;
	static TypeDescriber<WithCollections> withCollectionsDescriber;
	static TypeDescriber<Bike> bikeDescriber;
	static TypeDescriber<BikeGear> bikeGearDescriber;
	static TypeDescriber<BikeGearBike> bikeGearBikeDescriber;
	static TypeDescriber<BikeWheel> bikeWheelDescriber;
	static TypeDescriber<PrimaryKeyModel> primaryKeyDescriber;

	@Mock
	Resolver resolver;
	CarRepository carRepository;
	DoorRepository doorRepository;
	SqlGenerator sqlGenerator;
	private EngineRepository engineRepository;
	TypeDescriber<Exhaust> exhaustDescriber;
	ExhaustRepository exhaustRepository;
	TireRepository tireRepository;
	WithCollectionsRepository withCollectionsRepository;
	BikeRepository bikeRepository;
	AllFieldTypesRepository allFieldTypesRepository;
	PrimaryKeyModelRepository primayKeyModelRepository;
	BipodRepository podRepository;


	@Before
	public void setupBase() {
		setInjector();
		carDescriber = TypeDescriberImpl.getTypeDescriber(Car.class);
		doorDescriber = TypeDescriberImpl.getTypeDescriber(Door.class);
		engineDescriber = TypeDescriberImpl.getTypeDescriber(Engine.class);
		engineCarDescriber = TypeDescriberImpl.getTypeDescriber(EngineCar.class);
		exhaustDescriber = TypeDescriberImpl.getTypeDescriber(Exhaust.class);
		tireDescriber = TypeDescriberImpl.getTypeDescriber(Tire.class);
		withCollectionsDescriber = TypeDescriberImpl.getTypeDescriber(WithCollections.class);
		primaryKeyDescriber = TypeDescriberImpl.getTypeDescriber(PrimaryKeyModel.class);

		bikeDescriber = TypeDescriberImpl.getTypeDescriber(Bike.class);
		bikeGearDescriber = TypeDescriberImpl.getTypeDescriber(BikeGear.class);
		bikeGearBikeDescriber = TypeDescriberImpl.getTypeDescriber(BikeGearBike.class);
		bikeWheelDescriber = TypeDescriberImpl.getTypeDescriber(BikeWheel.class);
		withCollectionsDescriber = TypeDescriberImpl.getTypeDescriber(WithCollections.class);


		carRepository = injector.getInstance(CarRepository.class);
		doorRepository = injector.getInstance(DoorRepository.class);
		engineRepository = injector.getInstance(EngineRepository.class);
		exhaustRepository = injector.getInstance(ExhaustRepository.class);
		tireRepository = injector.getInstance(TireRepository.class);
		withCollectionsRepository = injector.getInstance(WithCollectionsRepository.class);
		bikeRepository = injector.getInstance(BikeRepository.class);
		primayKeyModelRepository = injector.getInstance(PrimaryKeyModelRepository.class);
		podRepository = injector.getInstance(BipodRepository.class);
		allFieldTypesRepository = injector.getInstance(AllFieldTypesRepository.class);
	}

	protected abstract void setInjector();

	@After
	public void resetDb() {

	}

	@Test
	@TestClasses(Car.class)
	public void happy() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setBrand(Brand.Porsche);
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Optional<Car> actualOptional = carRepository.get(model.getId());
		Car actual = actualOptional.orElseThrow(RuntimeException::new);
		assertEquals(model.getId(), actual.getId());
		assertEquals(model.getTitle(), actual.getTitle());
		assertEquals(model.getCreatedAt(), actual.getCreatedAt());
		assertEquals(Brand.Porsche, actual.getBrand());
	}

	@Test
	@TestClasses({Car.class, Door.class})
	public void lazyLoad() {
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

		Door actualLazy = doorRepository.get(lazyModel.getId()).orElseThrow(RuntimeException::new);
		Car actual = actualLazy.getCar();
		assertEquals(model.getId(), actual.getId());
		assertEquals(model.getTitle(), actual.getTitle());
		assertEquals(model.getCreatedAt(), actual.getCreatedAt());
		assertEquals(2, actual.getDoors().size());
	}

	@Test
	@TestClasses({Car.class})
	public void queryBuilder() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh 2");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);


		Collection<Car> cars = carRepository.query()
				.eq(Car::getTitle,"Muh 2")
				.execute()
				.collect(Collectors.toList());

		assertEquals(1, cars.size());
	}


	@Test
	@TestClasses({Car.class, Door.class})
	public void queryBuilder_multipleRestrictionsOnSameRelationField() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh 1");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		Door door = factory.get(Door.class);
		door.setCarId(model.getId());
		door.setTitle("Door of Muh 1");
		door.setId(UUID.randomUUID());

		doorRepository.save(door);
		carRepository.save(model);

		model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh 2");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		door = factory.get(Door.class);
		door.setCarId(model.getId());
		door.setTitle("Door of Muh 2");
		door.setId(UUID.randomUUID());

		doorRepository.save(door);
		carRepository.save(model);


		Collection<Car> cars = carRepository.query()
				.withEager(Car::getDoors)
				.subQueryList(Car::getDoors, sq -> sq.eq(Door::getTitle, "Door of Muh 1"))
				.subQueryList(Car::getDoors, sq -> sq.eq(Door::getTitle, "Door of Muh 2"))
				.execute()
				.collect(Collectors.toList());

		assertEquals(0, cars.size());

		cars = carRepository.query()
				.withEager(Car::getDoors)
				.subQueryList(Car::getDoors, sq -> sq.like(Door::getTitle, "% 1"))
				.subQueryList(Car::getDoors, sq -> sq.like(Door::getTitle, "% 2"))
				.execute()
				.collect(Collectors.toList());
		assertEquals(0, cars.size());

		cars = carRepository.query()
				.withEager(Car::getDoors)
				.subQueryList(Car::getDoors, sq -> sq.lte(Door::getTitle, "Door of Muh 1"))
				.subQueryList(Car::getDoors, sq -> sq.gte(Door::getTitle, "Door of Muh 2"))
				.execute()
				.collect(Collectors.toList());


		assertEquals(0, cars.size());
	}

	@Test
	@TestClasses({Car.class, Door.class})
	public void queryBuilder_subQuery() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Door door = factory.get(Door.class);
		door.setCarId(model.getId());
		door.setTitle("Door title");
		door.setId(UUID.randomUUID());
		doorRepository.save(door);


		Collection<Door> doors = doorRepository.query()
				.subQuery(Door::getCar
						, query -> query.eq(Car::getTitle, "Muh")
				)
				.execute().collect(Collectors.toList());
		assertEquals(1, doors.size());
	}


	@Test(expected = IllegalArgumentException.class)
	@TestClasses({Car.class})
	public void queryBuilder_inCondition_typeMismatch() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setBrand(Brand.Hyundai);
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		List<String> brands = Arrays.asList("Hyundai", "Porsche");
		//Car::getBrand is an enum and brands Collection are strings
		carRepository.query().in(Car::getBrand, brands).execute();
	}

	@Test
	@TestClasses({Car.class, Door.class})
	public void queryBuilder_subQuery_in() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Door door = factory.get(Door.class);
		door.setCarId(model.getId());
		door.setTitle("Door title");
		door.setId(UUID.randomUUID());
		doorRepository.save(door);


		Collection<Door> doors = doorRepository.query()
				.subQuery(Door::getCar
						, query -> query.in(Car::getTitle, Arrays.asList("Muh"))
				)
				.execute().collect(Collectors.toList());
		assertEquals(1, doors.size());
	}

	@Test
	@TestClasses({Car.class, Door.class})
	public void queryBuilder_subQuery_in_withEmptyArray_returnsEmptyResult() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Door door = factory.get(Door.class);
		door.setCarId(model.getId());
		door.setTitle("Door title");
		door.setId(UUID.randomUUID());
		doorRepository.save(door);

		Stream<Door> result = doorRepository.query()
				.subQuery(Door::getCar
						, query -> query.in(Car::getTitle, Collections.emptyList())
				).execute();
		assertEquals(0, result.count());
	}

	@Test
	@TestClasses({Car.class, Door.class})
	public void queryBuilder_subQuery_in_withEmptyArray_delete_deletesNothing() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Door door = factory.get(Door.class);
		door.setCarId(model.getId());
		door.setTitle("Door title");
		door.setId(UUID.randomUUID());
		doorRepository.save(door);

		CrudRepository.CrudUpdateResult result = doorRepository.query()
				.subQuery(Door::getCar
						, query -> query.in(Car::getTitle, Collections.emptyList())
				).delete();
		assertEquals(0, result.affectedRows());
		assertTrue(doorRepository.get(door.getId()).isPresent());
	}

	@Test
	@TestClasses({Car.class, Engine.class, EngineCar.class})
	public void queryBuilder_subQueryVia() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Engine engine = factory.get(Engine.class);
		engine.setId(UUID.randomUUID());
		engine.setCars(new ArrayList<>());
		engine.getCars().add(model);
		engineRepository.save(engine);


		Collection<Engine> doors = engineRepository.query()
				.subQueryList(Engine::getCars
						, query -> query.eq(Car::getTitle, "Muh")
				)
				.execute().collect(Collectors.toList());
		assertEquals(1, doors.size());
	}

	@Test
	@TestClasses({Car.class})
	public void queryBuilder_inQuery() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh 2");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh 3");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Collection<Car> cars = carRepository.query()
				.in(Car::getTitle, "Muh 2", "Muh")
				.execute().collect(Collectors.toList());
		assertEquals(2, cars.size());
		verify(resolver, never()).getTypedCollection(any(), any(),any(), any());
		verify(resolver, never()).getCollection(any(), any(), any());
		verify(resolver, never()).get(any(), any(), any());
	}

	@Test
	@TestClasses({Car.class})
	public void queryBuilder_sortAndLimit() {
		Car model1 = factory.get(Car.class);
		model1.setId(UUID.randomUUID());
		model1.setTitle("Muh 1");
		model1.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model1);

		Car model2 = factory.get(Car.class);
		model2.setId(UUID.randomUUID());
		model2.setTitle("Muh 2");
		model2.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model2);

		Car model3 = factory.get(Car.class);
		model3.setId(UUID.randomUUID());
		model3.setTitle("Muh 3");
		model3.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model3);

		Car model4 = factory.get(Car.class);
		model4.setId(UUID.randomUUID());
		model4.setTitle("Muh 4");
		model4.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model4);

		Collection<Car> cars = carRepository.query()
				.sortDescending(Car::getTitle)
				.limit(1,2)
				.execute().collect(Collectors.toList());
		assertEquals(2, cars.size());
		assertEquals("Muhs: "+cars.stream().map(c -> c.getTitle()).collect(Collectors.joining(", ")),"Muh 3", cars.stream().findFirst().get().getTitle());
		assertEquals("Muh 2", cars.stream().skip(1).findFirst().get().getTitle());
	}

	@Test
	@TestClasses({WithCollections.class})
	public void withCollections() {
		WithCollections w = factory.get(WithCollections.class);
		w.setId("id");
		w.setField1(Arrays.asList("id1","id2"));
		w.setField2(new HashSet<>(Arrays.asList("field1","field2")));
		withCollectionsRepository.save(w);

		Optional<WithCollections> actualOptional = withCollectionsRepository.query().eq(WithCollections::getId, "id").execute().findFirst();
		WithCollections actual = actualOptional.orElseThrow(RuntimeException::new);
		assertEquals(w.getField1(), actual.getField1());
		assertEquals(w.getField2(), actual.getField2());
	}

	@Test
	@TestClasses({WithCollections.class})
	public void withNullCollections() {
		WithCollections w = factory.get(WithCollections.class);
		w.setId("id");
		w.setField1(null);
		w.setField2(null);
		withCollectionsRepository.save(w);

		Optional<WithCollections> actualOptional = withCollectionsRepository.query().eq(WithCollections::getId, "id").execute().findFirst();
		WithCollections actual = actualOptional.orElseThrow(RuntimeException::new);
		assertNull(actual.getField1());
		assertNull(actual.getField2());
	}


	@Test
	@TestClasses({Car.class})
	public void deleteByQueryBuilder() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh 2");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		carRepository.query().eq(Car::getTitle, "Muh").delete();

		Assert.assertEquals(1, carRepository.query().count());
	}

	@Test
	@TestClasses({Car.class, Door.class})
	public void deleteByqueryBuilder_subQuery() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Door door = factory.get(Door.class);
		door.setCarId(model.getId());
		door.setTitle("Door title");
		door.setId(UUID.randomUUID());
		doorRepository.save(door);


		doorRepository.query()
				.subQuery(Door::getCar
						, query -> query.eq(Car::getTitle, "Muh")
				)
				.delete();

		Assert.assertEquals(0, doorRepository.query().count());
	}

	@Test
	@TestClasses({Car.class})
	public void deleteById() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);


		Assert.assertEquals(1, carRepository.query().count());

		carRepository.deleteById(model.getId());

		Assert.assertEquals(0, carRepository.query().count());
	}

	@Test
	@TestClasses({Car.class})
	public void deleteByIds() {
		Car car1 = factory.get(Car.class);
		car1.setId(UUID.randomUUID());
		car1.setTitle("Muh");
		car1.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(car1);

		Car car2 = factory.get(Car.class);
		car2.setId(UUID.randomUUID());
		car2.setTitle("Muh");
		car2.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(car2);


		Assert.assertEquals(2, carRepository.query().count());
		CrudRepository.CrudUpdateResult results = carRepository.deleteByIds(Arrays.asList(car1.getId(), car2.getId()));
		Assert.assertEquals(2, results.affectedRows());

		Assert.assertEquals(0, carRepository.query().count());
	}

	@Test
	@TestClasses({Car.class})
	public void saveMultiple() {
		List<Car> models = new ArrayList<>();
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setBrand(Brand.Porsche);
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		models.add(model);
		Car model2 = factory.get(Car.class);
		model2.setId(UUID.randomUUID());
		model2.setTitle("Muh");
		model2.setBrand(Brand.Porsche);
		model2.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		models.add(model2);

		carRepository.obtainInTransaction(tx -> {
			carRepository.save(tx, models);
			return null;
		});

		List<Car> actual = carRepository.query().eq(Car::getBrand, Brand.Porsche).execute().collect(Collectors.toList());
		assertEquals(2, actual.size());
		assertEquals("Muh", actual.get(0).getTitle());
		assertEquals("Muh", actual.get(1).getTitle());

		model.setTitle("Muh2");
		model2.setTitle("Muh2");

		carRepository.obtainInTransaction(tx -> {
			carRepository.save(tx, models);
			return null;
		});

		actual = carRepository.query().eq(Car::getBrand, Brand.Porsche).execute().collect(Collectors.toList());
		assertEquals(2, actual.size());
		assertEquals("Muh2", actual.get(0).getTitle());
		assertEquals("Muh2", actual.get(1).getTitle());
	}

	@Test
	@TestClasses({Car.class, Door.class, Exhaust.class})
	public void save_autoSaveRelations() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		Door door1 = factory.get(Door.class);
		door1.setId(UUID.randomUUID());
		door1.setTitle("Lazy as such");
		door1.setCar(model);
		model.getDoors().add(door1);

		Door door2 = factory.get(Door.class);
		door2.setId(UUID.randomUUID());
		door2.setTitle("Lazy as well");
		door2.setCar(model);
		model.getDoors().add(door2);

		Exhaust exhaust = factory.get(Exhaust.class);
		exhaust.setBrand(Brand.Hyundai);
		exhaust.setId(UUID.randomUUID());
		model.setExhaust(exhaust);

		carRepository.save(model);

		Door actual1 = doorRepository.get(door1.getId()).orElseThrow(RuntimeException::new);
		Door actual2 = doorRepository.get(door2.getId()).orElseThrow(RuntimeException::new);
		assertEquals(door1.getId(), actual1.getId());
		assertEquals(door2.getId(), actual2.getId());
		// Since the exhaust relation is not marked as auto save, save including relations will not include it
		Optional<Exhaust> actualExhaust = exhaustRepository.get(exhaust.getId());
		assertFalse(actualExhaust.isPresent());
	}

	@Test
	@TestClasses({Car.class, Door.class, Exhaust.class})
	public void save_autoSaveRelations_multiple() {
		Car car1 = factory.get(Car.class);
		car1.setId(UUID.randomUUID());
		car1.setTitle("Muh");
		car1.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		Door door1 = factory.get(Door.class);
		door1.setId(UUID.randomUUID());
		door1.setTitle("Lazy as such");
		door1.setCar(car1);
		car1.getDoors().add(door1);

		Car car2 = factory.get(Car.class);
		car2.setId(UUID.randomUUID());
		car2.setTitle("Muh");
		car2.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		Door door2 = factory.get(Door.class);
		door2.setId(UUID.randomUUID());
		door2.setTitle("Lazy as well");
		door2.setCar(car2);
		car1.getDoors().add(door2);

		Exhaust exhaust = factory.get(Exhaust.class);
		exhaust.setBrand(Brand.Hyundai);
		exhaust.setId(UUID.randomUUID());
		car1.setExhaust(exhaust);
		carRepository.doRetryableInTransaction(tx -> {
			carRepository.save(tx, Arrays.asList(car1, car2));
		});

		Door actual1 = doorRepository.get(door1.getId()).orElseThrow(RuntimeException::new);
		Door actual2 = doorRepository.get(door2.getId()).orElseThrow(RuntimeException::new);
		assertEquals(door1.getId(), actual1.getId());
		assertEquals(door2.getId(), actual2.getId());
		// Since the exhaust relation is not marked as auto save, save including relations will not include it
		Optional<Exhaust> actualExhaust = exhaustRepository.get(exhaust.getId());
		assertFalse(actualExhaust.isPresent());
	}

	@Test
	@TestClasses({Car.class, Door.class, Exhaust.class})
	public void save_autoSaveRelations_multipleSameDoor() {
		Car car1 = factory.get(Car.class);
		car1.setId(UUID.randomUUID());
		car1.setTitle("Muh");
		car1.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		Door door1 = factory.get(Door.class);
		door1.setId(UUID.randomUUID());
		door1.setTitle("Lazy as such");
		car1.getDoors().add(door1);

		Door door2 = factory.get(Door.class);
		door2.setId(UUID.randomUUID());
		door2.setTitle("Lazy as such");
		car1.getDoors().add(door2);

		Car car2 = factory.get(Car.class);
		car2.setId(UUID.randomUUID());
		car2.setTitle("Muh");
		car2.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		car2.getDoors().add(door2);

		Exhaust exhaust = factory.get(Exhaust.class);
		exhaust.setBrand(Brand.Hyundai);
		exhaust.setId(UUID.randomUUID());
		car1.setExhaust(exhaust);
		carRepository.doRetryableInTransaction(tx -> {
			carRepository.save(tx, Arrays.asList(car1, car2));
		});

		Door actual1 = doorRepository.get(door1.getId()).orElseThrow(RuntimeException::new);
		Door actual2 = doorRepository.get(door2.getId()).orElseThrow(RuntimeException::new);
		assertEquals(door1.getId(), actual1.getId());
		assertEquals(door2.getId(), actual2.getId());
		// Since the exhaust relation is not marked as auto save, save including relations will not include it
		Optional<Exhaust> actualExhaust = exhaustRepository.get(exhaust.getId());
		assertFalse(actualExhaust.isPresent());
	}

	@Test
	@TestClasses({Bike.class, BikeWheel.class})
	public void save_autoSaveRelations_withCompoundKey() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		BikeWheel wheel = factory.get(BikeWheel.class);
		wheel.setBikeType(BikeType.Mountain);
		wheel.setSize(20);
		wheel.setColor("red");

		bike.setFrontWheel(wheel);
		bike.setBackWheel(wheel);

		bikeRepository.save(bike);

		Bike actualMountain = bikeRepository.get(bike.getId()).orElseThrow(RuntimeException::new);
		assertEquals(bike.getId(), actualMountain.getId());
		assertEquals(wheel.getBikeType(), actualMountain.getFrontWheel().getBikeType());
		assertEquals(wheel.getBikeType(), actualMountain.getBackWheel().getBikeType());
	}

	@Test
	@TestClasses({Bike.class, BikeWheel.class})
	public void save_noAutoSaveRelation() {
		Bike raceBike = factory.get(Bike.class);
		raceBike.setId(UUID.randomUUID().toString());
		raceBike.setBikeType(BikeType.Racer);
		raceBike.setWheelSize(16);

		BikeWheel auxWheel = factory.get(BikeWheel.class);
		auxWheel.setBikeType(BikeType.Racer);
		auxWheel.setSize(16);
		auxWheel.setColor("blue");

		raceBike.setAuxiliaryWheel(auxWheel);

		bikeRepository.save(raceBike);

		Bike actualRace = bikeRepository.get(raceBike.getId()).orElseThrow(RuntimeException::new);
		assertNull(actualRace.getAuxiliaryWheel());
	}

	@Test
	@TestClasses({Bike.class, BikeGear.class})
	public void save_autoSaveRelation_viaRelation() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		BikeGear gear = factory.get(BikeGear.class);
		gear.setGearNum(8);
		bike.getGears().add(gear);

		bikeRepository.save(bike);

		Bike actual = bikeRepository.get(bike.getId()).orElseThrow(RuntimeException::new);
		assertEquals(bike.getId(), actual.getId());
		assertEquals(1, actual.getGears().size());
		assertEquals(8, actual.getGears().get(0).getGearNum());
	}

	@Test
	@TestClasses({Bike.class, BikeGear.class, BikeGearBike.class})
	public void queryOtherClass() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		BikeGear gear = factory.get(BikeGear.class);
		gear.setGearNum(8);
		bike.getGears().add(gear);

		bikeRepository.save(bike);

		BikeGear actual = bikeRepository.getGear(8);
		assertEquals(8, actual.getGearNum());
	}

	@Test
	@TestClasses({Bike.class, BikeGear.class})
	public void deleteOtherClass() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		BikeGear gear = factory.get(BikeGear.class);
		gear.setGearNum(8);
		bike.getGears().add(gear);

		bikeRepository.save(bike);

		BikeGear actual = bikeRepository.getGear(8);
		assertEquals(8, actual.getGearNum());

		bikeRepository.deleteGear(8);

		try {
			bikeRepository.getGear(8);
			fail();
		} catch (RuntimeException e) {

		}
	}

	@Test
	@TestClasses({AllFieldTypes.class})
	public void allFieldTypes() {
		AllFieldTypes obj = factory.get(AllFieldTypes.class);
		obj.setId(UUID.randomUUID());
		obj.setString("string");
		obj.setCharacter('c');
		obj.setZonedDateTime(ZonedDateTime.parse("2021-01-01T00:00:00.000Z"));
		obj.setInstant(Instant.parse("2021-01-01T00:00:00.000Z"));
		obj.setLocalDateTime(LocalDateTime.parse("2021-01-01T00:00:00"));
		obj.setLocalDate(LocalDate.parse("2021-01-01"));
		obj.setBigDecimal(BigDecimal.valueOf(3.1415));
		obj.setAnEnum(Brand.Hyundai);

		obj.setInteger(24);
		obj.setaShort((short) 44);
		obj.setaLong(42L);
		obj.setaDouble(3.14);
		obj.setaFloat(3.15f);
		obj.setaBoolean(false);
		obj.setaByte((byte) 3);

		obj.setPrimitiveInteger(666);
		obj.setPrimitiveShort((short) 115);
		obj.setPrimitiveLong(444L);
		obj.setPrimitiveDouble(99.99);
		obj.setPrimitiveFloat(88.88f);
		obj.setPrimitiveBoolean(true);
		obj.setPrimitiveByte((byte) 9);

		allFieldTypesRepository.save(obj);

		AllFieldTypes actual = allFieldTypesRepository.get(obj.getId()).get();

		assertEquals(obj.getId(), actual.getId());
		assertEquals(obj.getString(), actual.getString());
		assertEquals(obj.getCharacter(), actual.getCharacter());
		assertEquals(obj.getZonedDateTime(), actual.getZonedDateTime());
		assertEquals(obj.getInstant(), actual.getInstant());
		assertEquals(obj.getLocalDateTime(), actual.getLocalDateTime());
		assertEquals(obj.getLocalDate(), actual.getLocalDate());
		assertEquals(obj.getBigDecimal().stripTrailingZeros(), actual.getBigDecimal().stripTrailingZeros());

		assertEquals(obj.getInteger(), actual.getInteger());
		assertEquals(obj.getaShort(), actual.getaShort());
		assertEquals(obj.getaLong(), actual.getaLong());
		assertEquals(obj.getaDouble(), actual.getaDouble());
		assertEquals(obj.getaFloat(), actual.getaFloat());
		assertEquals(obj.getaBoolean(), actual.getaBoolean());
		assertEquals(obj.getaByte(), actual.getaByte());

		assertEquals(obj.getPrimitiveInteger(), actual.getPrimitiveInteger());
		assertEquals(obj.getPrimitiveShort(), actual.getPrimitiveShort());
		assertEquals(obj.getPrimitiveLong(), actual.getPrimitiveLong());
		assertEquals(obj.getPrimitiveDouble(), actual.getPrimitiveDouble(),0.001);
		assertEquals(obj.getPrimitiveFloat(), actual.getPrimitiveFloat(), 0.001);
		assertEquals(obj.isPrimitiveBoolean(), actual.isPrimitiveBoolean());
		assertEquals(obj.getPrimitiveByte(), actual.getPrimitiveByte());
	}


	@Test
	@TestClasses({AllFieldTypes.class})
	public void allFieldTypes_nulls() {
		AllFieldTypes obj = factory.get(AllFieldTypes.class);
		obj.setId(UUID.randomUUID());

		allFieldTypesRepository.save(obj);

		AllFieldTypes actual = allFieldTypesRepository.get(obj.getId()).get();

		assertEquals(obj.getId(), actual.getId());
		assertNull(actual.getString());
		assertNull(actual.getCharacter());
		assertNull(actual.getZonedDateTime());
		assertNull(actual.getInstant());
		assertNull(actual.getLocalDateTime());
		assertNull(actual.getLocalDate());
		assertNull(actual.getBigDecimal());

		assertNull(actual.getInteger());
		assertNull(actual.getaShort());
		assertNull(actual.getaLong());
		assertNull(actual.getaDouble());
		assertNull(actual.getaFloat());
		//assertFalse(actual.getaBoolean()); //edge case should we be able to save null booleans?
		assertNull(actual.getaByte());
		assertNull(actual.getUuid());

		assertEquals(0, actual.getPrimitiveInteger());
		assertEquals(0, actual.getPrimitiveShort());
		assertEquals(0, actual.getPrimitiveLong());
		assertEquals(0, actual.getPrimitiveDouble(),0.001);
		assertEquals(0, actual.getPrimitiveFloat(), 0.001);
		assertFalse(actual.isPrimitiveBoolean());
		assertEquals(0, actual.getPrimitiveByte());
	}

	@Test
	@TestClasses({AllFieldTypes.class})
	public void allFieldTypes_update() {
		AllFieldTypes obj = factory.get(AllFieldTypes.class);
		obj.setId(UUID.randomUUID());
		obj.setString("string");
		obj.setCharacter('c');
		obj.setZonedDateTime(ZonedDateTime.parse("2021-01-01T00:00:00.000Z"));
		obj.setInstant(Instant.parse("2021-01-01T00:00:00.000Z"));
		obj.setLocalDateTime(LocalDateTime.parse("2021-01-01T00:00:00"));
		obj.setLocalDate(LocalDate.parse("2021-01-01"));
		obj.setBigDecimal(BigDecimal.valueOf(3.1415));
		obj.setAnEnum(Brand.Hyundai);

		obj.setInteger(24);
		obj.setaShort((short) 44);
		obj.setaLong(42L);
		obj.setaDouble(3.14);
		obj.setaFloat(3.15f);
		obj.setaBoolean(false);
		obj.setaByte((byte) 3);

		obj.setPrimitiveInteger(666);
		obj.setPrimitiveShort((short) 115);
		obj.setPrimitiveLong(444L);
		obj.setPrimitiveDouble(99.99);
		obj.setPrimitiveFloat(88.88f);
		obj.setPrimitiveBoolean(true);
		obj.setPrimitiveByte((byte) 9);

		allFieldTypesRepository.save(obj);

		allFieldTypesRepository.query()
				.eq(AllFieldTypes::getId, obj.getId())
				.update(u -> {
					u.set(AllFieldTypes::getString, "string2");
					u.set(AllFieldTypes::getCharacter, 'a');
					u.set(AllFieldTypes::getZonedDateTime, ZonedDateTime.parse("2022-01-01T00:00:00.000Z"));
					u.set(AllFieldTypes::getInstant, Instant.parse("2022-01-01T00:00:00.000Z"));
					u.set(AllFieldTypes::getLocalDateTime, LocalDateTime.parse("2022-01-01T00:00:00"));
					u.set(AllFieldTypes::getLocalDate, LocalDate.parse("2022-01-01"));
					u.set(AllFieldTypes::getBigDecimal, BigDecimal.valueOf(4.4135));
					u.set(AllFieldTypes::getAnEnum, Brand.Porsche);

					u.set(AllFieldTypes::getInteger, 42);
					u.set(AllFieldTypes::getaShort, (short) 88);
					u.set(AllFieldTypes::getaLong, 84L);
					u.set(AllFieldTypes::getaDouble, 7.11);
					u.set(AllFieldTypes::getaFloat, 6.3f);
					u.set(AllFieldTypes::getaBoolean, true);
					u.set(AllFieldTypes::getaByte, (byte) 5);

					u.set(AllFieldTypes::getPrimitiveInteger, 777);
					u.set(AllFieldTypes::getPrimitiveShort, (short) 15);
					u.set(AllFieldTypes::getPrimitiveLong, 888L);
					u.set(AllFieldTypes::getPrimitiveDouble, 55.55);
					u.set(AllFieldTypes::getPrimitiveFloat, 44.44f);
					u.set(AllFieldTypes::isPrimitiveBoolean, false);
					u.set(AllFieldTypes::getPrimitiveByte, (byte) 10);
				});
		AllFieldTypes actual = allFieldTypesRepository.get(obj.getId()).get();

		assertEquals(obj.getId(), actual.getId());
		assertEquals("string2", actual.getString());
		assertEquals(Character.valueOf('a'), actual.getCharacter());
		//Why do we still have this assertion?
		//Currently, the obj is getting update when we use the update() method because we use the obj reference.
		//And so, the line bellow would be incrementing 12 months to the already updated date, making it 2023 instead of
		//the expected 2022.
		//This reference problem should be tackled in the near future by using a clone/copy version of the obj.
		//assertEquals(obj.getZonedDateTime().plusMonths(12), actual.getZonedDateTime());
		assertEquals(ZonedDateTime.parse("2022-01-01T00:00:00Z"), actual.getZonedDateTime());
		assertEquals(Instant.parse("2022-01-01T00:00:00Z"), actual.getInstant());
		assertEquals(LocalDateTime.parse("2022-01-01T00:00:00"), actual.getLocalDateTime());
		assertEquals(LocalDate.parse("2022-01-01"), actual.getLocalDate());
		assertEquals(new BigDecimal("4.4135"), actual.getBigDecimal().stripTrailingZeros());

		assertEquals(Integer.valueOf(42), actual.getInteger());
		assertEquals(Short.valueOf((short) 88), actual.getaShort());
		assertEquals(Long.valueOf(84), actual.getaLong());
		assertEquals(Double.valueOf(7.11), actual.getaDouble());
		assertEquals(Float.valueOf(6.3f), actual.getaFloat());
		assertEquals(Boolean.TRUE, actual.getaBoolean());
		assertEquals(Byte.valueOf((byte) 5), actual.getaByte());

		assertEquals(777, actual.getPrimitiveInteger());
		assertEquals(15, actual.getPrimitiveShort());
		assertEquals(888L, actual.getPrimitiveLong());
		assertEquals(55.55, actual.getPrimitiveDouble(),0.001);
		assertEquals(44.44f, actual.getPrimitiveFloat(), 0.001);
		assertEquals(false, actual.isPrimitiveBoolean());
		assertEquals((byte)10, actual.getPrimitiveByte());
	}

	@Test
	@TestClasses({Bike.class})
	public void groupByAggregate_count() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Racer);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		GroupNumericResult result = bikeRepository.query().groupBy(Bike::getBikeType).count(Bike::getId);
		assertEquals(2, result.size());
		assertEquals(2, result.get(BikeType.Mountain));
		assertEquals(1, result.get(BikeType.Racer));
		assertEquals(2, result.keys().size());
		assertEquals(1, result.keys().stream().filter(k -> k.contains(BikeType.Mountain)).count());
		assertEquals(1, result.keys().stream().filter(k -> k.contains(BikeType.Racer)).count());
	}

	@Test
	@TestClasses({Bike.class})
	public void groupByAggregate_multipleFields_count() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(22);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Racer);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		GroupNumericResult result = bikeRepository.query().groupBy(Bike::getBikeType, Bike::getWheelSize).count(Bike::getId);
		assertEquals(3, result.size());
		assertEquals(1, result.get(BikeType.Mountain, 22));
		assertEquals(1, result.get(BikeType.Mountain, 20));
		assertEquals(1, result.get(BikeType.Racer, 20));
		assertEquals(3, result.keys().size());
	}


	@Test
	@TestClasses({Bike.class})
	public void groupByAggregate_sum() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Racer);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		GroupNumericResult result = bikeRepository.query().groupBy(Bike::getBikeType).sum(Bike::getWheelSize);
		assertEquals(2, result.size());
		assertEquals(40, result.get(BikeType.Mountain));
		assertEquals(20, result.get(BikeType.Racer));
		assertEquals(2, result.keys().size());
		assertEquals(1, result.keys().stream().filter(k -> k.contains(BikeType.Mountain)).count());
		assertEquals(1, result.keys().stream().filter(k -> k.contains(BikeType.Racer)).count());
	}

	@Test
	@TestClasses({Bike.class})
	public void groupByAggregate_max() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(22);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Racer);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		GroupNumericResult result = bikeRepository.query().groupBy(Bike::getBikeType).max(Bike::getWheelSize);
		assertEquals(2, result.size());
		assertEquals(22, result.get(BikeType.Mountain));
		assertEquals(20, result.get(BikeType.Racer));
	}


	@Test
	@TestClasses({Bike.class})
	public void groupByAggregate_min() {
		Bike bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Mountain);
		bike.setWheelSize(22);

		bikeRepository.save(bike);

		bike = factory.get(Bike.class);
		bike.setId(UUID.randomUUID().toString());
		bike.setBikeType(BikeType.Racer);
		bike.setWheelSize(20);

		bikeRepository.save(bike);

		GroupNumericResult result = bikeRepository.query().groupBy(Bike::getBikeType).min(Bike::getWheelSize);
		assertEquals(2, result.size());
		assertEquals(20, result.get(BikeType.Mountain));
		assertEquals(20, result.get(BikeType.Racer));
	}

	@Test
	@TestClasses({Car.class})
	public void update_objectsThatMatchConditions() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh");
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		Car model2 = factory.get(Car.class);
		model2.setId(UUID.randomUUID());
		model2.setTitle("Muh");
		model2.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		carRepository.save(model);
		carRepository.save(model2);

		carRepository.updateTitle(model, "new title");

		Car actual = carRepository.get(model.getId()).get();
		assertEquals("new title", actual.getTitle());
		actual = carRepository.get(model2.getId()).get();
		assertEquals("Muh", actual.getTitle());
	}


	@Test
	@TestClasses({Car.class})
	public void update_withoutPreviousRecord() {
		Car car1 = factory.get(Car.class);
		car1.setId(UUID.randomUUID());
		car1.setTitle("car1");
		car1.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		Car car2 = factory.get(Car.class);
		car2.setId(UUID.randomUUID());
		car2.setTitle("car2");
		car2.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(car2);

		int affectedRows = carRepository.query()
				.eq(Car::getId, car1.getId())
				.update(u -> u.set(Car::getTitle, "new_car1_title"))
				.affectedRows();

		assertEquals(0, affectedRows);
	}


	@Test
	@TestClasses({Car.class, Door.class, CarWheel.class})
	public void sorting_happy() throws Throwable {
		Arrays.asList("C","B","A")
				.forEach(this::carWithDoors);

		List<String> ascendingTitles = carRepository.query()
				.subQueryList(Car::getDoors, sq -> {
					sq.in(Door::getTitle, "Nissan door 1");
				})
				.sortAscending(Car::setTitle)
				.execute().map(Car::getTitle).collect(Collectors.toList());

		assertEquals(Arrays.asList("A", "B", "C"), ascendingTitles);

		List<String> descendingTitles = carRepository.query()
				.subQueryList(Car::getDoors, sq -> {
					sq.in(Door::getTitle, "Nissan door 1");
				})
				.sortDescending(Car::setTitle)
				.execute().map(Car::getTitle).collect(Collectors.toList());

		assertEquals(Arrays.asList("C", "B", "A"), descendingTitles);
	}

	private void carWithDoors(String carTitle) {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle(carTitle);
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

		Door door1 = factory.get(Door.class);
		door1.setId(UUID.randomUUID());
		door1.setTitle("Nissan door 1");
		door1.setCarId(model.getId());

		Door door2 = factory.get(Door.class);
		door2.setId(UUID.randomUUID());
		door2.setTitle("Nissan door 1");
		door2.setCarId(model.getId());

		model.setDoors(Arrays.asList(door1, door2));
		carRepository.save(model);
	}

}




