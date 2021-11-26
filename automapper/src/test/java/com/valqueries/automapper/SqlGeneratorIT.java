package com.valqueries.automapper;

import com.valqueries.DataSourceProvider;
import com.valqueries.Database;
import com.valqueries.UpdateResult;
import io.ran.TypeDescriberImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlGeneratorIT {
	SqlGenerator sqlGenerator;
	private SqlDescriber describer;
	private Database database;

	private void update(String sql) {
		database.doInTransaction(tx -> {
			for(String s : sql.split(";")) {
				UpdateResult r = tx.update(s, setter -> {});
				int a = 0;
			}
		});
	}

	@Before
	public void setup() {
		database = new Database(DataSourceProvider.get());
		describer = new SqlDescriber();
		sqlGenerator = new SqlGenerator(new SqlNameFormatter(), describer);
		update("DROP TABLE IF EXISTS simple_test_table");

	}

	@Test
	public void generateTable() {
		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		update(actual);
		assertEquals("CREATE TABLE IF NOT EXISTS simple_test_table (`id` VARCHAR(255), `title` VARCHAR(255), `created_at` DATETIME, PRIMARY KEY(`id`), INDEX created_idx (created_at));", actual);
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