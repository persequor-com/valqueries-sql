package com.valqueries.automapper.schema;

import io.ran.schema.IndexAction;
import io.ran.schema.IndexBuilder;

public class ValqueriesIndexBuilder extends IndexBuilder<ValqueriesIndexBuilder> {
	public ValqueriesIndexBuilder(IndexAction action) {
		super(action);
	}

	public void isUnique() {
		action.addProperty("isUnique", true);
	}
}
