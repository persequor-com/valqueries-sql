
package com.valqueries;

import java.util.List;
import java.util.Optional;

public class Orm implements IOrm, ITransactionContext {

	private final JdbcHelper jdbc;
	private boolean autoClose;

	public Orm(JdbcHelper jdbc, boolean autoClose) {
		this.jdbc = jdbc;
		this.autoClose = autoClose;
	}

	public Orm(JdbcHelper jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void update(String sql) {
		jdbc.update(sql);
	}

	@Override
	public UpdateResult update(String sql, Setter setter) {
		OrmStatement statement = new OrmStatement(sql);
		return update(setter, statement);
	}

	@Override
	public UpdateResult save(String tableName, Setter setter) {
		return update(setter, new SaveStatement(tableName));
	}

	private UpdateResult update(Setter setter, OrmStatement statement) {
		setter.set(statement);
		return jdbc.update(statement);
	}

	@Override
	public <T> List<T> query(String sql, Setter setter, IRowMapper<T> rowMapper) {
		OrmStatement statement = new OrmStatement(sql);
		if (setter != null) {
			setter.set(statement);
		}
		return jdbc.query(statement, rowMapper);
	}

	@Override
	public <T> Optional<T> querySingle(String sql, Setter setter, IRowMapper<T> rowMapper) throws OrmException.MoreThanOneRowFound {
		sql = sql.trim();
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		List<T> query = query(sql + " LIMIT 2", setter, rowMapper);
		if (query.size() > 1) {
			throw new OrmException.MoreThanOneRowFound("querySingle returned: " + query.size() + " results. For queries that return multiple results, query() method should be used");
		} else if (query.size() == 0) {
			return Optional.empty();
		}
		return Optional.ofNullable(query.get(0));
	}

	@Override
	@Deprecated
	public <T> Optional<T> queryOne(String sql, Setter setter, IRowMapper<T> rowMapper) throws OrmException.MoreThanOneRowFound {
		sql = sql.trim();
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		List<T> query = query(sql + " LIMIT 2", setter, rowMapper);
		if (query.size() > 1) {
			throw new OrmException.MoreThanOneRowFound("queryOne returned: " + query.size() + " results. For queries that return multiple results, query() method should be used");
		} else if (query.size() == 0) {
			return Optional.empty();
		}
		return Optional.of(query.get(0));
	}

	@Override
	public void close() {
		if (autoClose) {
			jdbc.close();
		}
	}
}
