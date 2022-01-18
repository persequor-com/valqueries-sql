package com.valqueries.automapper;

import com.valqueries.H2DataSourceProvider;
import com.valqueries.MariaDbDataSourceProvider;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;

@RunWith(MockitoJUnitRunner.class)
public class SqlGeneratorH2IT extends SqlGeneratorITBase {


	@Override
	protected String textType() {
		return "character large object";
	}

	@Override
	protected DataSource getDataSource() {
		return H2DataSourceProvider.get();
	}



}