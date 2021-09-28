/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2019-08-21
 */
package com.valqueries;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

public class DataSourceProvider extends HikariDataSource {
	private static DataSourceProvider INSTANCE;
	private final HikariDataSource dataSource;

	private DataSourceProvider() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(System.getProperty("db.url", "jdbc:mysql://localhost/valqueries"));
		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
		config.setUsername(System.getProperty("db.user", "root"));
		config.setPassword(System.getProperty("db.password", "s3cr3t"));
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
				INSTANCE = new DataSourceProvider();
			} catch (RuntimeException ex) {
				throw new RuntimeException("You should probably provide correct VM parameters. E.g.: \n -Ddb.url=jdbc:mysql://localhost:3306/saga -Ddb.user=root -Ddb.password=s3cr3t", ex);
			}
		}
		return INSTANCE.getDataSource();
	}

	public static void shutdown() {
		INSTANCE.close();
		INSTANCE = null;
	}
}
