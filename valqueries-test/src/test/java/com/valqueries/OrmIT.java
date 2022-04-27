
package com.valqueries;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OrmIT {
	private Database database;

	@Before
	public void setUp() throws Exception {
		database = new Database(MariaDbDataSourceProvider.get());
		try (IOrm orm = database.getOrm()) {
			orm.update("DROP TABLE IF EXISTS it_orm_basic;");
			orm.update(
					"CREATE TABLE it_orm_basic " +
							"( " +
							"  id CHAR(36) NULL, " +
							"  moment_in_time  DATETIME(3) NOT NULL DEFAULT 0, " +
							"  nullable_field  INT, " +
							"  floating_point  FLOAT, " +
							"  double_precision  DOUBLE, " +
							"  CONSTRAINT it_orm_basic_pk " +
							"  PRIMARY KEY (id) " +
							");");
		}
	}

	@AfterClass
	public static void tearDown() {
		MariaDbDataSourceProvider.shutdown();
	}

	@Test
	public void writeAndReadZonedDateTime() {
		String id = UUID.randomUUID().toString();
		long moment = 1565352770011l;

		String sql = "INSERT INTO it_orm_basic SET id = :id, moment_in_time = :moment_in_time";
		try (IOrm orm = database.getOrm()) {
			orm.update("REPLACE INTO it_orm_basic SET id = 1, moment_in_time = '2018-09-08 17:51:04.777'");
			orm.update(sql, statement -> {
				statement.set("id", id);
				statement.set("moment_in_time", ZonedDateTime.ofInstant(Instant.ofEpochMilli(moment), ZoneOffset.UTC));
			});

			assertEquals("Failed to read fractional seconds", 1536421864777l, orm.querySingle("SELECT * FROM it_orm_basic WHERE id=1", s->{}, rowMapper).get().getTimestamp());

			Optional<ItOrmBasic> row = orm.querySingle("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", id);
			}, rowMapper);

			assertEquals("Failed to write and then read fractional seconds", moment, row.get().getTimestamp());

			// can use fractional seconds in where clauses
			List<ItOrmBasic> found = orm.query("SELECT * FROM it_orm_basic WHERE moment_in_time > :time_in", statement -> {
				statement.set("time_in", Instant.ofEpochMilli(moment).minusMillis(1).atZone(ZoneOffset.UTC));
			}, rowMapper);

			assertEquals("Failed to use fractional seconds in 'where' clause", 1, found.size());

			List<ItOrmBasic> everythingStored = orm.query("SELECT * FROM it_orm_basic WHERE moment_in_time > :time_in", statement -> {
				statement.set("time_in", Instant.ofEpochMilli(0).atZone(ZoneOffset.UTC));
			}, rowMapper);


			assertEquals(2, everythingStored.size());
		}
	}

	@Test
	public void writeAndReadFloatAndDouble() {
		String id = UUID.randomUUID().toString();
		long moment = 1565352770000l;
		double double_precision = 5.0;
		float floating_point = 6.0f;

		String sql = "INSERT INTO it_orm_basic SET id = :id, moment_in_time = :moment_in_time, double_precision = :double_precision, floating_point = :floating_point";
		try (IOrm orm = database.getOrm()) {
			orm.update(sql, statement -> {
				statement.set("id", id);
				statement.set("moment_in_time", ZonedDateTime.ofInstant(Instant.ofEpochMilli(moment), ZoneOffset.UTC));
				statement.set("double_precision", double_precision);
				statement.set("floating_point", floating_point);
			});

			Optional<ItOrmBasic> row = orm.querySingle("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", id);
			}, rowMapper);

			assertTrue(0.1 > floating_point - row.get().getFloat());
			assertTrue(0.1 > double_precision - row.get().getDouble());
		}
	}

	@Test
	public void readUnsetFloatDouble() {
		String id = UUID.randomUUID().toString();
		long moment = 1565352770000l;
		double double_precision = 5.0;
		float floating_point = 6.0f;

		String sql = "INSERT INTO it_orm_basic SET id = :id, moment_in_time = :moment_in_time, double_precision = :double_precision, floating_point = :floating_point";
		try (IOrm orm = database.getOrm()) {
			orm.update(sql, statement -> {
				statement.set("id", id);
				statement.set("moment_in_time", ZonedDateTime.ofInstant(Instant.ofEpochMilli(moment), ZoneOffset.UTC));
				statement.set("double_precision", (Double) null);
				statement.set("floating_point", (Float) null);
			});

			Optional<ItOrmBasic> row = orm.querySingle("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", id);
			}, rowMapper);

			assertTrue(row.isPresent());
			assertNull(row.get().getFloat());
			assertNull(row.get().getDouble());
		}
	}

	@Test
	public void multipleParametersOfSameName() {
		long moment = 1565352770000l;
		String sql = "INSERT INTO it_orm_basic SET id = :id, nullable_field = :id, moment_in_time = :moment_in_time";
		try (IOrm orm = database.getOrm()) {
			orm.update(sql, statement -> {
				statement.set("id", 45);
				statement.set("moment_in_time", ZonedDateTime.ofInstant(Instant.ofEpochMilli(moment), ZoneOffset.UTC));
			});

			Optional<ItOrmBasic> row = orm.querySingle("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", 45);
			}, rowMapper);

			assertEquals(Integer.valueOf(45), row.get().getNullableField());
		}
	}

	@Test
	public void collection() {
		long moment = 1565352770000l;
		String sql = "INSERT INTO it_orm_basic SET id = :id, nullable_field = :id, moment_in_time = :moment_in_time";
		try (IOrm orm = database.getOrm()) {
			orm.update(sql, statement -> {
				statement.set("id", "45");
				statement.set("moment_in_time", ZonedDateTime.ofInstant(Instant.ofEpochMilli(moment), ZoneOffset.UTC));
			});
			orm.update(sql, statement -> {
				statement.set("id", "46");
				statement.set("moment_in_time", ZonedDateTime.ofInstant(Instant.ofEpochMilli(moment), ZoneOffset.UTC));
			});

			List<ItOrmBasic> res = orm.query("SELECT * FROM it_orm_basic WHERE id IN (:id) AND (id = :id_1 OR id = :id_2) AND id IN (:id)", statement -> {
				statement.set("id", Arrays.asList(45, 46));
				statement.set("id_1", 45);
				statement.set("id_2", 46);
			}, rowMapper);

			assertEquals("45", res.get(0).id);
			assertEquals("46", res.get(1).id);
		}
	}

	@Test
	public void testParameterlessQueryOne() {
		try (IOrm orm = database.getOrm()) {
			Optional<Long> count = orm.querySingle("SELECT COUNT(1) FROM it_orm_basic", row -> row.getLong(1));

			assertEquals(Long.valueOf(0), count.get());
		}
	}

	@Test
	public void testQueryOne_mappingToNull() {
		try (IOrm orm = database.getOrm()) {
			Optional<String> foundRowButMappedToNull = orm.querySingle("SELECT NULL", row -> row.getString(1));

			assertFalse(foundRowButMappedToNull.isPresent());
		}
	}

	@Test(expected = OrmException.MoreThanOneRowFound.class)
	public void testQueryOne_moreThanOneRowFound() {
		try (IOrm orm = database.getOrm()) {
			orm.querySingle("SELECT 1 UNION SELECT 2", row -> row.getString(1));
		}
	}

	@Test
	public void save() {
		try (IOrm orm = database.getOrm()) {
			orm.save("it_orm_basic", statement -> {
				statement.set("id", "333");
				statement.set("nullable_field", 4l);
			});
			orm.save("it_orm_basic", statement -> {
				statement.set("id", "333");
				statement.set("nullable_field", 55l);
			});

			List<Long> query = orm.query("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", "333");
			}, row -> row.getLong("nullable_field"));

			assertEquals(1, query.size());
			assertEquals(Long.valueOf(55), query.get(0));
		}
	}

	@Test
	public void writeManyRecords() {
		String sql = "INSERT INTO it_orm_basic SET id = :id, nullable_field = :nullable_field";
		try (IOrm orm = database.getOrm()) {
			for (long i = 0; i < 100; i++) {
				String id = UUID.randomUUID().toString();
				Long index = i;
				orm.update(sql, statement -> {
					statement.set("id", id);
					statement.set("nullable_field", index);
				});

				List<Long> dbIndex = orm.query("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
					statement.set("id", id);
				}, row -> row.getLong("nullable_field"));

				assertEquals(index, dbIndex.get(0));
			}
		}
	}

	@Test
	public void writeUTF8Characters() {
		String sql = "INSERT INTO it_orm_basic SET id = :id";
		try (IOrm orm = database.getOrm()) {
			orm.update(sql, statement -> {
				statement.set("id", "øscår");
			});

			Optional<String> dbIndex = orm.querySingle("SELECT id FROM it_orm_basic WHERE id=:id", statement -> statement.set("id", "øscår"), row -> row.getString("id"));

			assertTrue(dbIndex.isPresent());
		} catch (Exception e) {
			fail("UTF8 persisting/retrieving should have worked");
		}
	}

	@Test
	public void transaction_successful() {
		String sql = "INSERT INTO it_orm_basic SET id = :id";
		String id = UUID.randomUUID().toString();
		database.doInTransaction(transactionContext -> {
			transactionContext.update(sql, statement -> {
				statement.set("id", id);
			});
		});

		try (IOrm orm = database.getOrm()) {
			List<String> dbIndex = orm.query("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", id);
			}, row -> row.getString("id"));
			assertEquals(1, dbIndex.size());
		}
	}

	@Test
	public void transaction_rollback() {
		String sql = "INSERT INTO it_orm_basic SET id = :id";
		String id = UUID.randomUUID().toString();

		try {
			database.doInTransaction(transactionContext -> {
				transactionContext.update(sql, statement -> {
					statement.set("id", id);
				});
				List<String> dbIndex = transactionContext.query("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
					statement.set("id", id);
				}, row -> row.getString("id"));
				assertEquals(1, dbIndex.size());
				throw new RuntimeException("failure requiring rollback");
			});
		} catch (OrmException ex) {
			assert true; // inner runtime exception has been rethrown
		}

		try (IOrm orm = database.getOrm()) {
			List<String> dbIndex = orm.query("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", id);
			}, row -> row.getString("id"));
			assertEquals(0, dbIndex.size());
		}
	}

	@Test
	public void writeNull() {
		try (IOrm orm = database.getOrm()) {
			String idOfNonNullRecord = UUID.randomUUID().toString();

			final String sql = "INSERT INTO it_orm_basic SET id = :id, moment_in_time = :moment_in_time, nullable_field=:nullable_field";
			orm.update(sql, statement -> {
				statement.set("id", idOfNonNullRecord);
				statement.set("moment_in_time", ZonedDateTime.now());
				statement.set("nullable_field", 0L);
			});

			List<Long> withValue = orm.query("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", idOfNonNullRecord);
			}, row -> row.getLong("nullable_field"));

			assertEquals(Long.valueOf(0L), withValue.get(0));

			String idOfRecordWithNull = UUID.randomUUID().toString();
			orm.update(sql, statement -> {
				statement.set("id", idOfRecordWithNull);
				statement.set("moment_in_time", ZonedDateTime.now());
				statement.set("nullable_field", (Long) null);
			});

			List<Long> nullableFields = orm.query("SELECT * FROM it_orm_basic WHERE id=:id", statement -> {
				statement.set("id", idOfRecordWithNull);
			}, row -> row.getLong("nullable_field"));

			assertNull(nullableFields.get(0));
		}
	}

	@Test
	public void deadlock() throws InterruptedException {
		String id1 = "id1";
		String id2 = "id2";
		try (IOrm orm = database.getOrm()) {
			final String sql = "INSERT INTO it_orm_basic SET id = :id, moment_in_time = :moment_in_time, nullable_field=:nullable_field";
			orm.update(sql, statement -> {
				statement.set("id", id1);
				statement.set("moment_in_time", ZonedDateTime.now());
				statement.set("nullable_field", 5L);
			});
			orm.update(sql, statement -> {
				statement.set("id", id2);
				statement.set("moment_in_time", ZonedDateTime.now());
				statement.set("nullable_field", 10L);
			});
		}
		CountDownLatch firstStep = new CountDownLatch(2);
		CountDownLatch finalCondition = new CountDownLatch(2);

		List<Thread> threads = new ArrayList<>();
		threads.add(new Thread(() -> database.doInTransaction(tx -> {
					tx.update("UPDATE it_orm_basic SET nullable_field = 11 WHERE id = :id", statement -> statement.set("id", id1));
					firstStep.countDown();
					firstStep.await(100, TimeUnit.MILLISECONDS);
					tx.update("UPDATE it_orm_basic SET nullable_field = 12 WHERE id = :id", statement -> statement.set("id", id2));
					finalCondition.countDown();
				})));

		threads.add(new Thread(() -> database.doInTransaction(tx -> {
					tx.update("UPDATE it_orm_basic SET nullable_field = 13 WHERE id = :id", statement -> statement.set("id", id2));
					firstStep.countDown();
					firstStep.await(100, TimeUnit.MILLISECONDS);
					tx.update("UPDATE it_orm_basic SET nullable_field = 14 WHERE id = :id", statement -> statement.set("id", id1));
					finalCondition.countDown();
				})));

		threads.forEach(Thread::start);
		finalCondition.await(100, TimeUnit.MILLISECONDS);
		assertEquals(1, finalCondition.getCount());
	}

	@Test
	public void noDeadlock_dueToRetry() throws InterruptedException {
		String id1 = "id1";
		String id2 = "id2";
		try (IOrm orm = database.getOrm()) {
			final String sql = "INSERT INTO it_orm_basic SET id = :id, moment_in_time = :moment_in_time, nullable_field=:nullable_field";
			orm.update(sql, statement -> {
				statement.set("id", id1);
				statement.set("moment_in_time", ZonedDateTime.now());
				statement.set("nullable_field", 5L);
			});
			orm.update(sql, statement -> {
				statement.set("id", id2);
				statement.set("moment_in_time", ZonedDateTime.now());
				statement.set("nullable_field", 10L);
			});
		}
		CountDownLatch firstStep = new CountDownLatch(2);
		CountDownLatch finalCondition = new CountDownLatch(2);

		List<Thread> threads = new ArrayList<>();
		threads.add(new Thread(() -> database.doInTransaction(2, Duration.ofMillis(100),
				tx -> {
					tx.update("UPDATE it_orm_basic SET nullable_field = 11 WHERE id = :id", statement -> statement.set("id", id1));
					firstStep.countDown();
					firstStep.await(5, TimeUnit.SECONDS);
					tx.update("UPDATE it_orm_basic SET nullable_field = 12 WHERE id = :id", statement -> statement.set("id", id2));
					finalCondition.countDown();
				})));

		threads.add(new Thread(() -> database.doInTransaction(2, Duration.ofMillis(100),
				tx -> {
					tx.update("UPDATE it_orm_basic SET nullable_field = 13 WHERE id = :id", statement -> statement.set("id", id2));
					firstStep.countDown();
					firstStep.await(5, TimeUnit.SECONDS);
					tx.update("UPDATE it_orm_basic SET nullable_field = 14 WHERE id = :id", statement -> statement.set("id", id1));
					finalCondition.countDown();
				})));

		threads.forEach(Thread::start);
		finalCondition.await(5, TimeUnit.SECONDS);
		assertEquals(0, finalCondition.getCount());
	}

	private IRowMapper<ItOrmBasic> rowMapper = row -> new ItOrmBasic(row.getString("id"), row.getDateTime("moment_in_time").toInstant().toEpochMilli(), row.getInt("nullable_field"), row.getFloat("floating_point"), row.getDouble("double_precision"));

	public static class ItOrmBasic {
		private final String id;
		private final long timestamp;
		private final Integer nullableField;
		private final Float aFloat;
		private final Double aDouble;

		public ItOrmBasic(String id, long timestamp, Integer nullableField, Float aFloat, Double aDouble) {
			this.id = id;
			this.timestamp = timestamp;
			this.nullableField = nullableField;
			this.aFloat = aFloat;
			this.aDouble = aDouble;
		}


		public long getTimestamp() {
			return timestamp;
		}

		public Integer getNullableField() {
			return nullableField;
		}

		public Float getFloat() {
			return aFloat;
		}

		public Double getDouble() {
			return aDouble;
		}
	}
}
