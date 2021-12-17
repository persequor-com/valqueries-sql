package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.MariaDbDataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperSqlServerIT extends AutoMapperIT {

	@Override
	Database database() {
		return new Database(SqlServerDataSourceProvider.get());
	}
}
