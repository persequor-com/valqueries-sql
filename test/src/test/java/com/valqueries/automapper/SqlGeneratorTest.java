package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.DialectType;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqlGeneratorTest {
	SqlGenerator sqlGenerator;
	@Mock
	private SqlDescriber describer;
	private SqlDescriber.DbTable dbTable = new SqlDescriber.DbTable();
	@Mock
	private Database database;

	@Before
	public void setup() {
		when(database.getDialectType()).thenReturn(DialectType.MariaDB);

		dbTable.getColumns().put("id", new SqlDescriber.DbRow("id", "VARCHAR(255)", false));
		dbTable.getColumns().put("title", new SqlDescriber.DbRow("title", "VARCHAR(255)", false));
		dbTable.getColumns().put("created_at", new SqlDescriber.DbRow("created_at", "DATETIME", false));
		dbTable.getIndex().put("PRIMARY", new SqlDescriber.DbIndex(true, "PRIMIARY", "id"));
		dbTable.getIndex().put("created_idx", new SqlDescriber.DbIndex(false, "created_idx", "created_at"));
		sqlGenerator = new SqlGenerator(new SqlNameFormatter(), new DialectFactory(new SqlNameFormatter()), database, describer);
		when(describer.describe(any(TypeDescriber.class),anyString(), any(Database.class))).thenReturn(dbTable);
	}

	@Test
	public void generateTable() {
		when(describer.describe(any(TypeDescriber.class),anyString(), any(Database.class))).thenReturn(null);

		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		assertEquals("CREATE TABLE IF NOT EXISTS `simple_test_table` (`id` VARCHAR(255), `title` VARCHAR(255), `created_at` DATETIME, PRIMARY KEY(`id`), INDEX created_idx (`created_at`));", actual);
	}

	@Test
	public void tableExistsWithNoChanges() {
		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		assertEquals("", actual);
	}

	@Test
	public void tableExistsWithNewColumnChanges() {
		dbTable.getColumns().remove("title");
		when(describer.describe(any(TypeDescriber.class),anyString(), any(Database.class))).thenReturn(dbTable);

		String actual = sqlGenerator.generateOrModifyTable(database, TypeDescriberImpl.getTypeDescriber(SimpleTestTable.class));

		assertEquals("ALTER TABLE `simple_test_table` ADD COLUMN `title` VARCHAR(255);", actual);
	}


}