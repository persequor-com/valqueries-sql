
package com.valqueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcHelper {
	private final Connection connection;

	public JdbcHelper(Connection connection) {
		this.connection = connection;
	}

	private Connection getConnection() {
		return connection;
	}

	public UpdateResult update(OrmStatement statement) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(statement.toJdbcQuery(), Statement.RETURN_GENERATED_KEYS)
		) {
			JdbcParameterIndex index = new JdbcParameterIndex();
			for (String parameterName : statement.getParameterNames()) {
				statement.setValue(preparedStatement, index, parameterName);
			}

			int nrOfRowsAffected = preparedStatement.executeUpdate();

			UpdateResult result = new UpdateResult();
			result.setAffectedRows(nrOfRowsAffected);
			try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					result.setLastInsertedId(generatedKeys.getLong(1));
				}
			}
			return result;
		} catch (SQLException e) {
			throw new OrmException(e);
		}
	}

	public void update(String sql) {
		try (Statement statement = getConnection().createStatement()) {
			statement.execute(sql);
		} catch (SQLException e) {
			throw new OrmException(e);
		}
	}

	public <T> List<T> query(OrmStatement statement, IRowMapper<T> rowMapper) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(statement.toJdbcQuery())
		) {
			JdbcParameterIndex index = new JdbcParameterIndex();
			for (String parameterName : statement.getParameterNames()) {
				statement.setValue(preparedStatement, index, parameterName);
			}

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				List<T> queryResult = new ArrayList<>();
				OrmResultSet resultSetWrapper = new OrmResultSet(resultSet, new Orm(this));
				while (resultSet.next()) {
					T mapped = rowMapper.mapRow(resultSetWrapper);
					queryResult.add(mapped);
				}
				return queryResult;
			}
		} catch (SQLException e) {
			throw new OrmException(e);
		}
	}

	public void close() {
		try {
			getConnection().close();
		} catch (SQLException e) {
			throw new OrmException(e);
		}
	}

	public void rollback() {
		try {
			getConnection().rollback();
		} catch (SQLException e) {
			throw new OrmException(e);
		}
	}

	public void commit() {
		try {
			getConnection().commit();
		} catch (SQLException e) {
			throw new OrmException(e);
		}
	}
}
