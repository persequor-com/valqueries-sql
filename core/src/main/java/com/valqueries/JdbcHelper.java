
package com.valqueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcHelper {
	private static final Set<Integer> INTTYPES = new HashSet<>(Arrays.asList(Types.INTEGER, Types.BIGINT, Types.SMALLINT, Types.TINYINT));
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
				if (generatedKeys.next() && INTTYPES.contains(generatedKeys.getMetaData().getColumnType(1))) {
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
			System.out.println(statement.toJdbcQuery());
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
