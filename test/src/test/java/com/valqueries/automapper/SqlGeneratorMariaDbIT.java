package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.MariaDbDataSourceProvider;
import io.ran.TypeDescriberImpl;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

public class SqlGeneratorMariaDbIT extends SqlGeneratorITBase {


	@Override
	protected DataSource getDataSource() {
		return MariaDbDataSourceProvider.get();
	}


	@Test
	public void generateTable() {
		update("DROP TABLE IF EXISTS simple_test_table");

		String actual = sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		update(actual);
		assertEquals("CREATE TABLE IF NOT EXISTS `simple_test_table` (`id` VARCHAR(255), `title` VARCHAR(255), `created_at` DATETIME, PRIMARY KEY(`id`), INDEX created_idx (`created_at`));", actual);
	}

	@Test
	public void  updateTable_simple() {
		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		update(actual);
		assertEquals("CREATE TABLE IF NOT EXISTS `simple_test_table` (`id` VARCHAR(255), `title` VARCHAR(255), `created_at` DATETIME, PRIMARY KEY(`id`), INDEX created_idx (`created_at`));", actual);
	}


	@Test
	public void generateTable_indexOrder() {
		update("DROP TABLE IF EXISTS index_order_test_table");

		String actual = sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(IndexOrderTestTable.class));

		update(actual);
		assertEquals("CREATE TABLE IF NOT EXISTS `index_order_test_table` (`id` VARCHAR(255), `title` VARCHAR(255), `created_at` DATETIME, PRIMARY KEY(`id`, `title`), INDEX created_idx (`created_at`, `title`));", actual);
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
		assertEquals("ALTER TABLE `simple_test_table` ADD COLUMN `title` VARCHAR(255);", actual);
	}
}