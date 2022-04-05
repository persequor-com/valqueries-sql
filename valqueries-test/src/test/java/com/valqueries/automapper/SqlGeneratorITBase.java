package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.automapper.schema.ValqueriesIndexToken;
import com.valqueries.automapper.schema.ValqueriesSchemaBuilder;
import com.valqueries.automapper.schema.ValqueriesSchemaExecutor;
import io.ran.Clazz;
import io.ran.TypeDescriberImpl;
import io.ran.schema.TableAction;
import io.ran.token.ColumnToken;
import io.ran.token.Token;
import org.junit.Before;
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

	protected void update(String sql) {
		database.doInTransaction(tx -> {
			for(String s : sql.split(";")) {
				tx.update(s, setter -> {});
			}
		});
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


	}

	protected abstract DataSource getDataSource();

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
	public void tableExistsWithTypeChangeOnExistingColumn_toInvalidOtherType() {
		sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));
		try {
			sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTableBrokenTitle.class));
			fail();
		} catch (InvalidTypeConversionException e) {
			// expected
		}
	}

}