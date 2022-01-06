package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.TypeDescriberImpl;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

public abstract class SqlGeneratorITBase {
	protected SqlGenerator sqlGenerator;
	protected SqlDescriber describer;
	protected Database database;

	protected void update(String sql) {
		database.doInTransaction(tx -> {
			for(String s : sql.split(";")) {
				tx.update(s, setter -> {});
			}
		});
	}

	@Before
	public void setup() {
		database = new Database(getDataSource());
		DialectFactory dialectFactory = new DialectFactory(new SqlNameFormatter());
		describer = new SqlDescriber(dialectFactory);
		sqlGenerator = new SqlGenerator(new SqlNameFormatter(), dialectFactory, database, describer);

		update("DROP TABLE IF EXISTS simple_test_table");

	}

	protected abstract DataSource getDataSource();


}