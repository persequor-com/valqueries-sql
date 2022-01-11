package com.valqueries.automapper.schema;

import io.ran.schema.IndexAction;
import io.ran.schema.IndexBuilder;

public class ValqueriesIndexBuilder extends IndexBuilder<ValqueriesIndexBuilder> {
	public ValqueriesIndexBuilder(ValqueriesTableBuilder tableBuilder, IndexAction action) {
		super(tableBuilder, action);
	}

	public void isUnique() {
		action.addProperty("isUnique", true);
	}
}
