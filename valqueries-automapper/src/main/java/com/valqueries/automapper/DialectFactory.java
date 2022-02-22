package com.valqueries.automapper;

import com.valqueries.Database;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;

public class DialectFactory {
	private SqlNameFormatter sqlNameFormatter;

	@Inject
	public DialectFactory(SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
	}

	public SqlDialect get(Database database) {
		String dialectName = database.getDialectType().getDialect();
		try {
			return (SqlDialect)Class.forName(dialectName)
					.getConstructor(SqlNameFormatter.class).newInstance(sqlNameFormatter);
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Could not find dialect: "+dialectName,e);
		}
	}
}
