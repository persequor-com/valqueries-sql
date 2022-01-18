package com.valqueries.automapper.schema;

import com.valqueries.Database;
import com.valqueries.MariaDbDataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;

public class MssqlDbSchemaBuilderIT extends BaseSchemaBuilderIT {

	@Override
	protected Database database() {
		return new Database(SqlServerDataSourceProvider.get());
	}
}
