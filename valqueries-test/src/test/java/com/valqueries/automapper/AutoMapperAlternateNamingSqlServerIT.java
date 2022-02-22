package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.MariaDbDataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperAlternateNamingSqlServerIT extends AutoMapperAlternateNamingIT {
	@Override
	protected Database database() {
		return new Database(SqlServerDataSourceProvider.get());
	}
}
