package com.valqueries.automapper;

import com.valqueries.H2DataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

	@Override
	protected DataSource secondaryDatasource() {
		HikariConfig config = new HikariConfig();

		config.setJdbcUrl(System.getProperty("db.url", "jdbc:h2:file:/tmp/test2"));
		config.setDriverClassName("org.h2.Driver");
		config.setUsername(System.getProperty("db.user", "sa"));
		config.setPassword(System.getProperty("db.password", "sa"));
		config.setMinimumIdle(10);
		config.setMaximumPoolSize(10);

		return new HikariDataSource(config);
	}


}