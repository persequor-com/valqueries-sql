package com.valqueries.automapper.schema;

import com.valqueries.Database;
import com.valqueries.H2DataSourceProvider;
import com.valqueries.SqlServerDataSourceProvider;

public class H2DbSchemaBuilderIT extends BaseSchemaBuilderIT {

	@Override
	protected Database database() {
		return new Database(H2DataSourceProvider.get());
	}
}
