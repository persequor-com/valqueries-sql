
package com.valqueries;

import java.sql.SQLException;

@FunctionalInterface
public interface IRowMapper<T> {
	T mapRow(OrmResultSet row) throws SQLException;
}
