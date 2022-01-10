package com.valqueries.automapper.schema;

import com.valqueries.Database;
import com.valqueries.IOrm;
import com.valqueries.automapper.DialectFactory;
import com.valqueries.automapper.SqlDialect;
import io.ran.schema.SchemaExecutor;
import io.ran.schema.TableAction;

import javax.inject.Inject;
import java.util.Collection;

public class ValqueriesSchemaExecutor implements SchemaExecutor {
	private DialectFactory dialectFactory;
	private Database database;

	@Inject
	public ValqueriesSchemaExecutor(DialectFactory dialectFactory, Database database) {
		this.dialectFactory = dialectFactory;
		this.database = database;
	}

	@Override
	public void execute(Collection<TableAction> collection) {
		try(IOrm orm = database.getOrm()) {
			for (TableAction ta : collection) {
				String action = ta.getAction().apply(ta);
				String[] actions = action.split(";");
				for (String a: actions) {
					System.out.println(a);
					orm.update(a);
				}

			}
		}
	}

	public SqlDialect getDialect() {
		return dialectFactory.get(database);
	}
}
