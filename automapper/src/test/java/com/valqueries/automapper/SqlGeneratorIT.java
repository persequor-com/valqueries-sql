package com.valqueries.automapper;

import com.valqueries.DataSourceProvider;
import com.valqueries.Database;
import com.valqueries.UpdateResult;
import io.ran.TypeDescriberImpl;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlGeneratorIT {

	SqlGenerator sqlGenerator;
	private Database database;

	private void update(String sql) {
		database.doInTransaction(tx -> {
			for(String s : sql.split(";")) {
				tx.update(s, setter -> {});
			}
		});
	}

	@Before
	public void setup() {
		database = new Database(DataSourceProvider.get());
		sqlGenerator = new SqlGenerator(new SqlNameFormatter());
	}

	@Test
	public void generateTable() {
		update("DROP TABLE IF EXISTS simple_test_table");

		String actual = sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		update(actual);
		assertEquals("CREATE TABLE IF NOT EXISTS simple_test_table (`id` VARCHAR(255), `title` VARCHAR(255), `created_at` DATETIME, PRIMARY KEY(`id`), INDEX created_idx (`created_at`));", actual);
	}

	@Test
	public void generateTable_indexOrder() {
		update("DROP TABLE IF EXISTS index_order_test_table");

		String actual = sqlGenerator.generateCreateTable(TypeDescriberImpl.getTypeDescriber(IndexOrderTestTable.class));

		update(actual);
		assertEquals("CREATE TABLE IF NOT EXISTS index_order_test_table (`id` VARCHAR(255), `title` VARCHAR(255), `created_at` DATETIME, PRIMARY KEY(`id`, `title`), INDEX created_idx (`created_at`, `title`));", actual);
	}

}