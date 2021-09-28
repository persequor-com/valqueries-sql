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

import static org.mockito.Mockito.*;

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
//		return new DataSource() {
//			@Override
//			public Connection getConnection() throws SQLException {
//				return wrap(dataSource.getConnection());
//			}
//
//			@Override
//			public Connection getConnection(String s, String s1) throws SQLException {
//				return wrap(dataSource.getConnection(s, s1));
//			}
//
//			private Connection wrap(Connection connection) {
//				System.out.println("open: "+System.identityHashCode(connection));
//				Connection s = spy(connection);
//				try {
//					doAnswer((i) -> {
//						System.out.println("close: "+System.identityHashCode(connection));
//						return i.callRealMethod();
//					}).when(s).close();
//				} catch (SQLException throwables) {
//					throw new RuntimeException(throwables);
//				}
//				return s;
//			}
//
//			@Override
//			public PrintWriter getLogWriter() throws SQLException {
//				return dataSource.getLogWriter();
//			}
//
//			@Override
//			public void setLogWriter(PrintWriter printWriter) throws SQLException {
//				dataSource.setLogWriter(printWriter);
//			}
//
//			@Override
//			public void setLoginTimeout(int i) throws SQLException {
//				dataSource.setLoginTimeout(i);
//			}
//
//			@Override
//			public int getLoginTimeout() throws SQLException {
//				return dataSource.getLoginTimeout();
//			}
//
//			@Override
//			public <T> T unwrap(Class<T> aClass) throws SQLException {
//				return dataSource.unwrap(aClass);
//			}
//
//			@Override
//			public boolean isWrapperFor(Class<?> aClass) throws SQLException {
//				return dataSource.isWrapperFor(aClass);
//			}
//
//			@Override
//			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
//				return dataSource.getParentLogger();
//			}
//		};
//		return dataSource;
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
