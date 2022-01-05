package com.valqueries.automapper;

import com.valqueries.MariaDbDataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;
import io.ran.TypeDescriberImpl;
import org.junit.Test;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SqlGeneratorSqlServerIT extends SqlGeneratorITBase {


	@Override
	protected DataSource getDataSource() {
		return SqlServerDataSourceProvider.get();
	}

	@Test
	public void generateTable() {
		update("DROP TABLE IF EXISTS simple_test_table");

		String actual = sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		update(actual);
	}

	@Test
	public void  updateTable_simple() {
		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));
		update(actual);
		actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		assertEquals("", actual);
	}


	@Test
	public void generateTable_indexOrder() {
		update("DROP TABLE IF EXISTS index_order_test_table");

		String actual = sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(IndexOrderTestTable.class));

		update(actual);
	}



	@Test
	public void tableExistsWithNoChanges() {
		update(sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class)));

		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		assertEquals("", actual);
	}

	@Test
	public void tableExistsWithNewColumnChanges() {
		database.doInTransaction(tx -> tx.update(sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class))));
		database.doInTransaction(tx -> tx.update("ALTER TABLE simple_test_table DROP COLUMN title"));

		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		update(actual);
	}
}