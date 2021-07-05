package com.valqueries.automapper;

import com.valqueries.IRowMapper;
import com.valqueries.OrmException;
import com.valqueries.Setter;
import com.valqueries.UpdateResult;

import java.util.List;
import java.util.Optional;

public class TestDoubleTransactionContext implements com.valqueries.ITransactionContext {
	@Override
	public void update(String sql) {

	}

	@Override
	public UpdateResult update(String sql, Setter setter) {
		return null;
	}

	@Override
	public UpdateResult save(String tableName, Setter setter) {
		return null;
	}

	@Override
	public <T> List<T> query(String sql, Setter setter, IRowMapper<T> rowMapper) {
		return null;
	}

	@Override
	public <T> Optional<T> querySingle(String sql, Setter setter, IRowMapper<T> rowMapper) throws OrmException.MoreThanOneRowFound {
		return Optional.empty();
	}

	@Override
	public <T> Optional<T> queryOne(String sql, Setter setter, IRowMapper<T> rowMapper) throws OrmException.MoreThanOneRowFound, NullPointerException {
		return Optional.empty();
	}
}
