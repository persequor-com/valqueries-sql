package com.valqueries.automapper.schema;

import com.valqueries.Database;
import com.valqueries.MariaDbDataSourceProvider;

public class MariaDbSchemaBuilderIT extends BaseSchemaBuilderIT {

	@Override
	protected Database database() {
		return new Database(MariaDbDataSourceProvider.get());
	}
}
