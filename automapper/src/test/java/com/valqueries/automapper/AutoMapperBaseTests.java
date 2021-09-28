package com.valqueries.automapper;

import com.google.inject.Injector;
import com.valqueries.Database;
import io.ran.GenericFactory;
import io.ran.Resolver;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public abstract class AutoMapperBaseTests {
	static Database database;
	static Injector injector;
	static GenericFactory factory;
	static TypeDescriber<Car> carDescriber;
	static TypeDescriber<Door> doorDescriber;
	static TypeDescriber<Engine> engineDescriber;
	static TypeDescriber<EngineCar> engineCarDescriber;
	static TypeDescriber<Tire> tireDescriber;
	static TypeDescriber<WithCollections> withCollectionsDescriber;

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

	@Before
	public void setupBase() {
		setInjector();
		sqlGenerator = injector.getInstance(SqlGenerator.class);
		carDescriber = TypeDescriberImpl.getTypeDescriber(Car.class);
		doorDescriber = TypeDescriberImpl.getTypeDescriber(Door.class);
		engineDescriber = TypeDescriberImpl.getTypeDescriber(Engine.class);
		engineCarDescriber = TypeDescriberImpl.getTypeDescriber(EngineCar.class);
		exhaustDescriber = TypeDescriberImpl.getTypeDescriber(Exhaust.class);
		tireDescriber = TypeDescriberImpl.getTypeDescriber(Tire.class);
		withCollectionsDescriber = TypeDescriberImpl.getTypeDescriber(WithCollections.class);
		carRepository = injector.getInstance(CarRepository.class);
		doorRepository = injector.getInstance(DoorRepository.class);
		engineRepository = injector.getInstance(EngineRepository.class);
		exhaustRepository = injector.getInstance(ExhaustRepository.class);
		tireRepository = injector.getInstance(TireRepository.class);
		withCollectionsRepository = injector.getInstance(WithCollectionsRepository.class);
	}

	protected abstract void setInjector();

	@After
	public void resetDb() {

	}

	@Test
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

	@Test
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
	public void freetext() {
		Car model = factory.get(Car.class);
		model.setId(UUID.randomUUID());
		model.setTitle("Muh says the cow");
		model.setBrand(Brand.Porsche);
		model.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
		carRepository.save(model);

		Optional<Car> actualOptional = carRepository.query().freetext(Car::getTitle, "says").execute().findFirst();
		Car actual = actualOptional.orElseThrow(RuntimeException::new);
		assertEquals(model.getId(), actual.getId());
	}

	@Test
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

		assertEquals(1, carRepository.query().count());
	}

	@Test
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

		assertEquals(0, doorRepository.query().count());
	}

	@Test
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

}




