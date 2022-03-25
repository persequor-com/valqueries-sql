package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.H2DataSourceProvider;
import com.valqueries.MariaDbDataSourceProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperAlternateNamingH2IT extends AutoMapperAlternateNamingIT {
	@Override
	protected Database database() {
		return new Database(H2DataSourceProvider.get());
	}

	@Test
	@Ignore
	public void mixedMultiFieldSort_happy() throws Throwable {

	}
}
