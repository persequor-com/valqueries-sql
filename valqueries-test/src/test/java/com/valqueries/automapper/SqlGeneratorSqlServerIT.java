package com.valqueries.automapper;

import com.valqueries.MariaDbDataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;
import io.ran.TypeDescriberImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SqlGeneratorSqlServerIT extends SqlGeneratorITBase {


	@Override
	protected String textType() {
		return "text(2147483647)";
	}

	@Override
	protected DataSource getDataSource() {
		return SqlServerDataSourceProvider.get();
	}
}