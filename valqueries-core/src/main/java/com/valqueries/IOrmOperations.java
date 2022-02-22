/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2019-08-27
 */
package com.valqueries;

import com.valqueries.OrmException.MoreThanOneRowFound;

import java.util.List;
import java.util.Optional;

public interface IOrmOperations {
	void update(String sql);

	UpdateResult update(String sql, Setter setter);

	UpdateResult save(String tableName, Setter setter);

	/**
	 * Queries data from the database and maps it to the list of objects using row mapper
	 *
	 * @param sql       query to be executed in order to fetch data. Parameters must be parametrized with named parameters like `:my_param`
	 * @param setter    setter used to populate parameters in the query
	 * @param rowMapper mapper transforming {@link OrmResultSet} to the object of your choice
	 * @param <T>       type of the object you wish database rows to be transformed to
	 * @return list of objects representing the result set rows from the database (list element per result set row)
	 */
	<T> List<T> query(String sql, Setter setter, IRowMapper<T> rowMapper);

	/**
	 * Queries for a single row from the database and maps it to the object using row mapper.
	 * Throws an exception if more than one row has been found.
	 * Returns empty optional in case if no rows were found or the row is mapped to null value.
	 *
	 * @param sql       query to be executed in order to fetch data. Parameters must be parametrized with named parameters like `:my_param`
	 * @param setter    setter used to populate parameters in the query
	 * @param rowMapper mapper transforming {@link OrmResultSet} to the object of your choice
	 * @param <T>       type of the object you wish database rows to be transformed to
	 * @return Optional of the object representing a row found in the database. Empty if no row were found or row is mapped to null value
	 * @throws MoreThanOneRowFound in case more than one row can be found for a given query
	 */
	<T> Optional<T> querySingle(String sql, Setter setter, IRowMapper<T> rowMapper) throws MoreThanOneRowFound;

	/**
	 * Deprecated because it throws a {@link NullPointerException} when the result is null instead of returning empty optional.
	 * <p>
	 * Queries for a single row from the database and maps it to the object using row mapper.
	 * Throws an exception if more than one row has been found.
	 * Returns empty optional in case if no rows were found.
	 *
	 * @param sql       query to be executed in order to fetch data. Parameters must be parametrized with named parameters like `:my_param`
	 * @param setter    setter used to populate parameters in the query
	 * @param rowMapper mapper transforming {@link OrmResultSet} to the object of your choice
	 * @param <T>       type of the object you wish database rows to be transformed to
	 * @return Optional of the object representing a row found in the database. Empty if no row were found
	 * @throws MoreThanOneRowFound  in case more than one row can be found for a given query
	 * @throws NullPointerException in case more than one row can be found for a given query
	 * @deprecated
	 */
	@Deprecated
	<T> Optional<T> queryOne(String sql, Setter setter, IRowMapper<T> rowMapper) throws MoreThanOneRowFound, NullPointerException;

	/**
	 * Overloaded version of {@link #querySingle(String, Setter, IRowMapper)}
	 * Should be used when the SQL is non-parametrized thus Setter is not needed
	 */
	default <T> Optional<T> querySingle(String sql, IRowMapper<T> rowMapper) throws MoreThanOneRowFound {
		return querySingle(sql, null, rowMapper);
	}

	@Deprecated
	default <T> Optional<T> queryOne(String sql, IRowMapper<T> rowMapper) throws MoreThanOneRowFound, NullPointerException {
		return queryOne(sql, null, rowMapper);
	}

	/**
	 * Overloaded version of {@link #query(String, Setter, IRowMapper)}
	 * Should be used when the SQL is non-parametrized thus Setter is not needed
	 */
	default <T> List<T> query(String sql, IRowMapper<T> rowMapper) {
		return query(sql, null, rowMapper);
	}
}
