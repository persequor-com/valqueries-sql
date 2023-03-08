package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.automapper.schema.ValqueriesIndexToken;
import com.valqueries.automapper.schema.ValqueriesSchemaBuilder;
import com.valqueries.automapper.schema.ValqueriesSchemaExecutor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ran.*;
import io.ran.schema.TableAction;
import io.ran.token.ColumnToken;
import io.ran.token.Token;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.spy;

public abstract class SqlGeneratorITBase {
	protected SqlGenerator sqlGenerator;
	protected SqlDescriber describer;
	protected Database database;
	private Provider<ValqueriesSchemaBuilder> schemaBuilder;
	protected SqlNameFormatter sqlNameFormatter;
	protected ValqueriesSchemaExecutor schemaExecutor;
	protected SqlDescriber sqlDescriber;
	protected SqlDialect dialect;
	@Captor
	ArgumentCaptor<Collection<TableAction>> captor;

	protected abstract String textType();

	protected void update(String sql, Database databaseToUpdate) {
		databaseToUpdate.doInTransaction(tx -> {
			for(String s : sql.split(";")) {
				tx.update(s, setter -> {});
			}
		});
	}

	protected void update(String sql) {
		update(sql, database);
	}

	@Before
	public void setup() {
		database = new Database(getDataSource());
		DialectFactory dialectFactory = new DialectFactory(new SqlNameFormatter());
		describer = new SqlDescriber(dialectFactory);
		sqlNameFormatter = new SqlNameFormatter();
		schemaBuilder = () -> new ValqueriesSchemaBuilder(schemaExecutor, sqlNameFormatter);
		schemaExecutor = spy(new ValqueriesSchemaExecutor(dialectFactory, database));
		sqlGenerator = new SqlGenerator(new SqlNameFormatter(), dialectFactory, database, describer, schemaBuilder);
		sqlDescriber = new SqlDescriber(dialectFactory);
		dialect = dialectFactory.get(database);

		try {
			update(dialect.generateDropIndexStatement(dialect.getTableName(Clazz.of(SimpleTestTable.class)), new ValqueriesIndexToken(sqlNameFormatter, dialect, Token.get("created_idx")), false));
		} catch (Exception e) {
//			System.out.println(e.toString());
//			e.printStackTrace();
		}
		update(dialect.dropTableStatement(Clazz.of(SimpleTestTable.class)));
		try {
			update(dialect.generateDropIndexStatement(dialect.getTableName(Clazz.of(TestTableWithChangedKeys.class)), new ValqueriesIndexToken(sqlNameFormatter, dialect, Token.get("created_idx")), false));
		} catch (Exception e) {
//			System.out.println(e.toString());
//			e.printStackTrace();
		}
		update(dialect.dropTableStatement(Clazz.of(TestTableWithChangedKeys.class)));



	}

	protected abstract DataSource getDataSource();

	@Test
	public void generateTable_withProvidedDatabase() {
		// arrange
		HikariConfig config = new HikariConfig();

		// TODO: Move the responsability of creating a secondary database to an abstract method
		//  so that each children implements it with the corresponding drivers
		config.setJdbcUrl(System.getProperty("db.url", "jdbc:h2:file:/tmp/test2"));
		config.setDriverClassName("org.h2.Driver");
		config.setUsername(System.getProperty("db.user", "sa"));
		config.setPassword(System.getProperty("db.password", "sa"));
		config.setMinimumIdle(10);
		config.setMaximumPoolSize(10);

		final DataSource otherDataSource = new HikariDataSource(config);

		final Database databaseToUse = new Database(otherDataSource);
		final Database databaseToIgnore = database;

		update("DROP TABLE IF EXISTS "+dialect.getTableName(Clazz.of(SimpleTestTable.class)));
		update("DROP TABLE IF EXISTS "+dialect.getTableName(Clazz.of(SimpleTestTable.class)), databaseToUse);

		// act
		sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class), databaseToUse);

		// assert
		SqlDescriber.DbTable tableThatShouldBeCreated = sqlDescriber.describe(dialect.table(Token.get("simple_test_table")), databaseToUse);
		SqlDescriber.DbTable tableThatShouldNotBeCreated = sqlDescriber.describe(dialect.table(Token.get("simple_test_table")), databaseToIgnore);

		assertEquals(3, tableThatShouldBeCreated.columns.size());
		assertNotNull(tableThatShouldBeCreated.columns.get("id"));
		assertNotNull(tableThatShouldBeCreated.columns.get("title").getType());
		assertNotNull(tableThatShouldBeCreated.columns.get("created_at").getType());
		assertNull(tableThatShouldNotBeCreated);
	}

	@Test
	public void generateTable() {
		update("DROP TABLE IF EXISTS "+dialect.getTableName(Clazz.of(SimpleTestTable.class)));

		sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		SqlDescriber.DbTable table = describer.describe(dialect.table(Token.get("simple_test_table")), database);

		assertEquals(3, table.columns.size());
		assertNotNull(table.columns.get("id"));
		assertNotNull(table.columns.get("title").getType());
		assertNotNull(table.columns.get("created_at").getType());
	}

	@Test
	public void generateTable_indexOrder() {
		try {
			update(dialect.generateDropIndexStatement(dialect.getTableName(Clazz.of(IndexOrderTestTable.class)), new ValqueriesIndexToken(sqlNameFormatter, dialect, Token.get("created_idx")), false));
		} catch (Exception e) {

		}
		update("DROP TABLE IF EXISTS "+dialect.getTableName(Clazz.of(IndexOrderTestTable.class)));

		sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(IndexOrderTestTable.class));

		SqlDescriber.DbTable table = describer.describe(dialect.getTableName(Clazz.of(IndexOrderTestTable.class)), database);

		assertEquals(2, table.index.size());
		assertEquals("PRIMARY", table.index.get("PRIMARY").getKeyName());
		assertEquals("created_at", table.index.get("created_idx").getColumns().get(0));
		assertEquals("title", table.index.get("created_idx").getColumns().get(1));
	}

	@Test
	public void tableExistsWithNoChanges() {
		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		InOrder inOrder = Mockito.inOrder(schemaExecutor);
		inOrder.verify(schemaExecutor).execute(anyCollection());
		inOrder.verify(schemaExecutor).execute(captor.capture());
		assertEquals(0, captor.getValue().stream().findFirst().get().getActions().size());
	}

	@Test
	public void tableExistsWithNewColumnChanges() {
		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));
		update("ALTER TABLE simple_test_table DROP COLUMN title");

		SqlDescriber.DbTable table = describer.describe(dialect.table(Token.get("simple_test_table")), database);
		assertFalse(table.columns.containsKey("title"));

		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		table = describer.describe(dialect.table(Token.get("simple_test_table")),database);

		assertTrue(table.columns.containsKey("title"));
	}

	@Test
	public void tableExistsWithTypeChangeOnExistingColumn() {
		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));
		ColumnToken column = dialect.column(Token.get("title"));
		update("ALTER TABLE simple_test_table "+ dialect.generateAlterColumnPartStatement(column)+" TEXT");

		SqlDescriber.DbTable table = describer.describe(dialect.table(Token.get("simple_test_table")), database);
		assertEquals(textType(),table.columns.get("title").getType());

		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		table = describer.describe(dialect.table(Token.get("simple_test_table")),database);

		assertNotEquals(textType(),table.columns.get("title").getType());
	}

	@Test
	public void tableExistsWithTypeChangeOnExistingColumn_toText_fromVarchar() {
		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTableTextTitle.class));

		SqlDescriber.DbTable table = describer.describe(dialect.table(Token.get("simple_test_table")), database);

		assertEquals(textType(),table.columns.get("title").getType());
	}

	@Test
	@Ignore // Awaits fix for  "checkTypeCompatibiliy(property.getType(), table.getColumns().get(columnName.name()).getType());" in SqlGenerator
	public void tableExistsWithTypeChangeOnExistingColumn_toInvalidOtherType() {
		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));
		try {
			sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTableBrokenTitle.class));
			fail();
		} catch (InvalidTypeConversionException e) {
			// expected
		}
	}

	@Test
	public void supportsChangesPrimaryKey() {
		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(TestTableWithChangedKeys.class));
		database.doInTransaction(tx -> {
			tx.update("insert into test_table_with_changed_keys (theid, idxedCol) values ('an id', 'idxed')");
		});

	}

}
