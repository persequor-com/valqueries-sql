/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2019-08-21
 */
package com.valqueries;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class H2DataSourceProvider extends HikariDataSource {
	private static H2DataSourceProvider INSTANCE;
	private final HikariDataSource dataSource;

	private H2DataSourceProvider() {
		HikariConfig config = new HikariConfig();


		config.setJdbcUrl(System.getProperty("db.url", "jdbc:h2:file:/tmp/test"));
		config.setDriverClassName("org.h2.Driver");
		config.setUsername(System.getProperty("db.user", "sa"));
		config.setPassword(System.getProperty("db.password", "sa"));
		config.setMinimumIdle(10);
		config.setMaximumPoolSize(1);


		dataSource = new HikariDataSource(config);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public static DataSource get() {
		if (INSTANCE == null) {
			try {
				INSTANCE = new H2DataSourceProvider();
			} catch (RuntimeException ex) {
				throw new RuntimeException("You should probably provide correct VM parameters. E.g.: \n -Ddb.url=jdbc:h2:file:test.h2.db -Ddb.user=root -Ddb.password=s3cr3t", ex);
			}
		}
		return INSTANCE.getDataSource();
	}

	public static void shutdown() {
		if (INSTANCE != null) {
			INSTANCE.close();
			INSTANCE = null;
		}

	}
}
