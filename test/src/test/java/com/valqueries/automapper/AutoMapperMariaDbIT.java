package com.valqueries.automapper;

import com.valqueries.MariaDbDataSourceProvider;
import com.valqueries.Database;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperMariaDbIT extends AutoMapperIT {

	@Override
	Database database() {
		return new Database(MariaDbDataSourceProvider.get());
	}
}
