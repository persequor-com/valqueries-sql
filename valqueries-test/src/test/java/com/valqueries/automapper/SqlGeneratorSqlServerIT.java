package com.valqueries.automapper;

import com.valqueries.SqlServerDataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;

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

	@Override
	protected DataSource secondaryDatasource() {
		HikariConfig config = new HikariConfig();

		config.setJdbcUrl(System.getProperty("db.url", "jdbc:sqlserver://localhost;database=sqlServerSecondary"));
		config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		config.setUsername(System.getProperty("db.user", "sa"));
		config.setPassword(System.getProperty("db.password", "7h3_s3cr3t!1s?0u7"));
		config.setMinimumIdle(10);
		config.setMaximumPoolSize(10);

		return new HikariDataSource(config);
	}

}