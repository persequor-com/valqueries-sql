package com.valqueries.automapper;

import com.valqueries.MariaDbDataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;

@RunWith(MockitoJUnitRunner.class)
public class SqlGeneratorMariaDbIT extends SqlGeneratorITBase {


	@Override
	protected String textType() {
		return "text";
	}

	@Override
	protected DataSource getDataSource() {
		return MariaDbDataSourceProvider.get();
	}


	@Override
	protected DataSource secondaryDatasource() {
		HikariConfig config = new HikariConfig();

		config.setJdbcUrl(System.getProperty("db.url", "jdbc:mariadb://localhost:3307/mariadbSecondary"));
		config.setDriverClassName("org.mariadb.jdbc.Driver");
		config.setUsername(System.getProperty("db.user", "root"));
		config.setPassword(System.getProperty("db.password", "s3cr3t"));
		config.setMinimumIdle(10);
		config.setMaximumPoolSize(10);

		return new HikariDataSource(config);
	}

}