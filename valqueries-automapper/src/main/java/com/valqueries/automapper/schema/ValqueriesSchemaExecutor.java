package com.valqueries.automapper.schema;

import com.valqueries.Database;
import com.valqueries.IOrm;
import com.valqueries.UpdateResult;
import com.valqueries.automapper.DialectFactory;
import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import io.ran.schema.SchemaExecutor;
import io.ran.schema.TableAction;

import javax.inject.Inject;
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

	public static ValqueriesSchemaExecutor forDatabase(Database database) {
		return new ValqueriesSchemaExecutor(new DialectFactory(new SqlNameFormatter()), database);
	}


	@Override
	public void execute(Collection<TableAction> collection) {
		try(IOrm orm = database.getOrm()) {
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

	public SqlDialect getDialect() {
		return dialectFactory.get(database);
	}

	public String getSql() {
		return sql;
	}
}
