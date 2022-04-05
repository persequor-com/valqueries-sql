package com.valqueries.automapper.schema;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.valqueries.Database;
import com.valqueries.OrmException;
import com.valqueries.automapper.GuiceModule;
import com.valqueries.automapper.SqlNameFormatter;
import com.valqueries.automapper.ValqueriesResolver;
import io.ran.Clazz;
import io.ran.Property;
import io.ran.token.Token;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class BaseSchemaBuilderIT {
	private Database database;
	private Injector injector;
	ValqueriesSchemaExecutor executor;
	ValqueriesSchemaBuilder builder;
	private SqlNameFormatter sqlNameFormatter;

	@Before
	public void setup() {
		database = database();
		sqlNameFormatter = new SqlNameFormatter();
		GuiceModule module = new GuiceModule(database, ValqueriesResolver.class);
		injector = Guice.createInjector(module);
		executor = injector.getInstance(ValqueriesSchemaExecutor.class);
		builder = new ValqueriesSchemaBuilder(executor, sqlNameFormatter);
		database.doInTransaction(tx -> {
			try {
				tx.update("drop table the_table");
			} catch (OrmException e) {
				// If it doesn't exist it is okay it fails
			}
		});
	}

	protected abstract Database database();

	@Test
	public void buildSimpleSchema() {
		builder.addTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("id"), Clazz.of(UUID.class)));
			tb.addColumn(Property.get(Token.get("title"), Clazz.of(String.class)));
			tb.addPrimaryKey(Token.get("id"));
		});
		builder.build();

		database.doInTransaction(tx -> {
			UUID id = UUID.randomUUID();
			tx.update("INSERT INTO the_table (id, title) values (:id, :title)", s -> {
				s.set("id", id);
				s.set("title", "The title");
			});

			try {
				tx.update("INSERT INTO the_table (id, title) values (:id, :title)", s -> {
					s.set("id", id);
					s.set("title", "The title too");
				});
				fail();
			} catch (OrmException e) {
				// Expected
			}

			List<String> result = tx.query("select * from the_table where id = :id", s -> {
				s.set("id", id);
			}, r -> {
				return r.getString("title");
			});
			assertEquals(1, result.size());
			assertEquals("The title", result.get(0));
		});
	}

	@Test
	public void buildSimpleSchema_supportTemporal() {
		builder.addTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("id"), Clazz.of(UUID.class)));
			tb.addColumn(Property.get(Token.get("timeField"), Clazz.of(ZonedDateTime.class)));
			tb.addPrimaryKey(Token.get("id"));
		});
		builder.build();

		ZonedDateTime timeWitlMilliseconds = Instant.ofEpochMilli(1536421864777L).atZone(ZoneOffset.UTC);
		database.doInTransaction(tx -> {
			UUID id = UUID.randomUUID();
			tx.update("INSERT INTO the_table (id, time_field) values (:id, :time_field)", s -> {
				s.set("id", id);
				s.set("time_field", timeWitlMilliseconds);
			});

			List<ZonedDateTime> result = tx.query("select * from the_table where id = :id", s -> {
				s.set("id", id);
			}, r -> {
				return r.getDateTime("time_field");
			});

			assertEquals(Collections.singletonList(timeWitlMilliseconds), result);
		});
	}

	@Test
	public void buildCompoundKeySchema() {
		builder.addTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("id"), Clazz.of(UUID.class)));
			tb.addColumn(Property.get(Token.get("title"), Clazz.of(String.class)));
			tb.addPrimaryKey(Token.get("id"), Token.get("title"));
		});
		builder.build();

		database.doInTransaction(tx -> {
			UUID id = UUID.randomUUID();
			tx.update("INSERT INTO the_table (id, title) values (:id, :title)", s -> {
				s.set("id", id);
				s.set("title", "The title");
			});

			tx.update("INSERT INTO the_table (id, title) values (:id, :title)", s -> {
				s.set("id", id);
				s.set("title", "The title too");
			});

			List<String> result = tx.query("select * from the_table where id = :id", s -> {
				s.set("id", id);
			}, r -> {
				return r.getString("title");
			});
			assertEquals(2, result.size());
			assertEquals("The title", result.get(0));
			assertEquals("The title too", result.get(1));
		});
	}

	@Test
	public void modifyTable_dropColumn() {
		builder.addTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("id"), Clazz.of(UUID.class)));
			tb.addColumn(Property.get(Token.get("title"), Clazz.of(String.class)));
			tb.addPrimaryKey(Token.get("id"));
		});

		builder.modifyTable(Token.get("TheTable"), tb -> {
			tb.dropPrimaryKey();
			tb.modifyColumn(Property.get(Token.get("id"), Clazz.of(String.class)));
			tb.removeColumn(Token.get("title"));
		});
		builder.build();

		database.doInTransaction(tx -> {
			UUID id = UUID.randomUUID();
			try {
				tx.update("INSERT INTO the_table (id, title) VALUES (:id, :title)", s -> {
					s.set("id", id);
					s.set("title", "The title");
				});
				fail();
			} catch (OrmException e) {
				// expected
			}

			tx.update("INSERT INTO the_table (id) values (:id)", s -> {
				s.set("id", id);
			});

			tx.update("INSERT INTO the_table (id) values (:id)", s -> {
				s.set("id", id);
			});

			List<String> result = tx.query("select * from the_table where id = :id", s -> {
				s.set("id", id);
			}, r -> {
				return r.getString("id");
			});
			assertEquals(2, result.size());
			assertEquals(id.toString(), result.get(0));
			assertEquals(id.toString(), result.get(1));
		});
	}

	@Test
	public void modifyTable_addColumn() {
		builder.addTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("id"), Clazz.of(UUID.class)));
			tb.addColumn(Property.get(Token.get("title"), Clazz.of(String.class)));
			tb.addPrimaryKey(Token.get("id"));
		});
		builder.build();
		builder = new ValqueriesSchemaBuilder(executor, sqlNameFormatter);

		builder.modifyTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("CreatedAt"), Clazz.of(ZonedDateTime.class)));
		});
		builder.build();

		database.doInTransaction(tx -> {
			UUID id = UUID.randomUUID();
			ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
			tx.update("INSERT INTO the_table (id,title, created_at) values (:id, :title, :createdAt)", s -> {
				s.set("id", id);
				s.set("title", "The title");
				s.set("createdAt", now);
			});

			List<ZonedDateTime> result = tx.query("select * from the_table where id = :id", s -> {
				s.set("id", id);
			}, r -> {
				return r.getDateTime("created_at");
			});
			assertEquals(1, result.size());
			assertEquals(now, result.get(0));
		});
	}

	@Test
	public void modifyTable_addIndex() {
		builder.addTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("id"), Clazz.of(UUID.class)));
			tb.addColumn(Property.get(Token.get("title"), Clazz.of(String.class)));
			tb.addPrimaryKey(Token.get("id"));
		});
		builder.build();

		builder = new ValqueriesSchemaBuilder(executor, sqlNameFormatter);
		builder.modifyTable(Token.get("TheTable"), tb -> {
			tb.addIndex(Token.get("my_index"), Token.get("title"), Token.get("id"));
		});
		builder.build();

		database.doInTransaction(tx -> {
			UUID id = UUID.randomUUID();

			tx.update("INSERT INTO the_table (id, title) values (:id, :title)", s -> {
				s.set("id", id);
				s.set("title", "The title");
			});

			List<String> result = tx.query("select * from the_table where title = :title and id = :id", s -> {
				s.set("title", "The title");
				s.set("id", id);
			}, r -> {
				return r.getString("title");
			});
			assertEquals(1, result.size());
			assertEquals("The title", result.get(0));
		});
	}

	@Test
	public void modifyTable_addIndexWithCustomProperties() {
		builder.addTable(Token.get("TheTable"), tb -> {
			tb.addColumn(Property.get(Token.get("id"), Clazz.of(UUID.class)));
			tb.addColumn(Property.get(Token.get("title"), Clazz.of(String.class)));
			tb.addPrimaryKey(Token.get("id"));
		});

		builder.modifyTable(Token.get("TheTable"), tb -> {
			tb.addIndex(Token.get("myUnique"), ib -> {
				ib.addField(Token.get("id"));
				ib.addField(Token.get("title"));
				ib.isUnique();
			});
		});
		builder.build();

		database.doInTransaction(tx -> {
			UUID id = UUID.randomUUID();

			tx.update("INSERT INTO the_table (id, title) values (:id, :title)", s -> {
				s.set("id", id);
				s.set("title", "The title");
			});

			try {
				tx.update("INSERT INTO the_table (id, title) values (:id, :title)", s -> {
					s.set("id", id);
					s.set("title", "The title");
				});
				fail();
			} catch (OrmException e) {
				// expected
			}

			List<String> result = tx.query("select * from the_table where title = :title and id = :id", s -> {
				s.set("title", "The title");
				s.set("id", id);
			}, r -> {
				return r.getString("title");
			});
			assertEquals(1, result.size());
			assertEquals("The title", result.get(0));
		});
	}
}
