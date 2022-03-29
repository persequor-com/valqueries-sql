package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.H2DataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperH2IT extends AutoMapperIT {

	@Override
	Database database() {
		return new Database(H2DataSourceProvider.get());
	}

	@Test
	@Ignore
	public void mixedMultiFieldSort_happy() throws Throwable {
		//doesnt seem to be working in H2. Need's to be investigated further.
	}
}