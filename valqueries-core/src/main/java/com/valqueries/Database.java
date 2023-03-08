
package com.valqueries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.time.Duration;

public class Database {
	private static final Logger log = LoggerFactory.getLogger(Database.class);

	private final DataSource dataSource;
	private DatabaseConfig config;

	@Inject
	public Database(DataSource dataSource, DatabaseConfig config) {
		this.dataSource = dataSource;
		this.config = config;
	}

	public Database(DataSource dataSource) {
		this(dataSource, new DatabaseConfig());
	}

	public DataSource datasource() {
		return dataSource;
	}

	public DialectType getDialectType() {
		try(Connection connection = dataSource.getConnection()) {
			String url = connection.getMetaData().getURL();
			int firstIndex = url.indexOf(":")+1;
			String jdbcName = url.substring(firstIndex, url.indexOf(":", firstIndex));
			return DialectType.from(jdbcName);
		} catch (SQLException throwables) {
			throw new RuntimeException(throwables);
		}
	}

	private JdbcHelper getJdbc(boolean withTransaction) {
		try {
			final Connection connection = dataSource.getConnection();
			connection.setAutoCommit(!withTransaction);
			return new JdbcHelper(connection);
		} catch (SQLException e) {
			throw new OrmException(e);
		}
	}

	public IOrm getOrm() {
		return new Orm(getJdbc(false), true);
	}


	public void doInTransaction(ITransaction tx) {
		obtainInTransaction(orm -> {
			tx.execute(orm);
			return null;
		});
	}

	public void doInTransaction(int retryCount, Duration retryDelay, ITransaction tx) {
		obtainInTransaction(retryCount, retryDelay, orm -> {
			tx.execute(orm);
			return null;
		});
	}

	public void doRetryableInTransaction(ITransaction tx) {
		doInTransaction(config.getDefaultRetryCount(), config.getDefaultRetryWait(), tx);
	}

	public <T> T obtainInTransaction(ITransactionWithResult<T> tx) {
		final JdbcHelper jdbc = getJdbc(true);
		try {
			Orm orm = new Orm(jdbc, false);
			T result = tx.execute(orm);
			jdbc.commit();
			return result;
		} catch (OrmException anyException) {
			try {
				jdbc.rollback();
			} catch (OrmException e) {}
			throw anyException;
		} catch (Exception anyException) {
			jdbc.rollback();
			throw new OrmException(anyException);
		} finally {
			jdbc.close();
		}
	}

	public <T> T obtainRetryableInTransaction(ITransactionWithResult<T> tx) {
		return obtainInTransaction(config.getDefaultRetryCount(), config.getDefaultRetryWait(), tx);
	}

	public <T> T obtainInTransaction(int retryCount, Duration retryDelay, ITransactionWithResult<T> tx) {
		OrmException ormException = new OrmException("Retry count was not set correctly: " + retryCount);
		for (int i = 0; i < retryCount; i++) {
			try {
				return obtainInTransaction(tx);
			} catch (OrmException anyException) {
				if (isRetryableException(anyException.getCause())) {
					log.debug("Sql exception has happened which we can probably recover from", anyException);
					try {
						ormException = anyException;
						Thread.sleep(retryDelay.toMillis());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw anyException;
					}
				} else {
					throw anyException;
				}
			}
		}
		throw ormException;
	}

	private boolean isRetryableException(Throwable cause) {
		if (cause instanceof SQLTransactionRollbackException) {
			SQLTransactionRollbackException ex = (SQLTransactionRollbackException) cause;
			//following validation will check if the error is a deadlock according to https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html
			return ex.getSQLState().equals("40001") && ex.getErrorCode() == 1213;
		}
		return false;
	}
}
