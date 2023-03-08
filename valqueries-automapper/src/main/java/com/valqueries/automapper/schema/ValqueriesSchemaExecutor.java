package com.valqueries.automapper.schema;

import com.valqueries.Database;
import com.valqueries.IOrm;
import com.valqueries.UpdateResult;
import com.valqueries.automapper.DialectFactory;
import com.valqueries.automapper.SqlDialect;
import io.ran.schema.SchemaExecutor;
import io.ran.schema.TableAction;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Collection;

public class ValqueriesSchemaExecutor implements SchemaExecutor {
	private final DialectFactory dialectFactory;
	private final Database database;
	private String sql = "";

	@Inject
	public ValqueriesSchemaExecutor(DialectFactory dialectFactory, Database database) {
		this.dialectFactory = dialectFactory;
		this.database = database;
	}

	@Override
	public void execute(Collection<TableAction> collection) {
		execute(collection, database);
	}

	private void execute(Collection<TableAction> collection, Database databaseToExecuteOn) {
		try(IOrm orm = databaseToExecuteOn.getOrm()) {
			for (TableAction ta : collection) {
				String action = ta.getAction().apply(ta);
				String[] actions = action.split(";");
				for (String a: actions) {
					if (a.length() > 0) {
						sql += a+";\n";
						UpdateResult res = orm.update(a, s -> {	});
					}
				}

			}
		}
	}

	@Override
	public void execute(Collection<TableAction> collection, DataSource datasourceToExecuteOn) {
		execute(collection, new Database(datasourceToExecuteOn));
	}

	public SqlDialect getDialect() {
		return dialectFactory.get(database);
	}

	public String getSql() {
		return sql;
	}
}
