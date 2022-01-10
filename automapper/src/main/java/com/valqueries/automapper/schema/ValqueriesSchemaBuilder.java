package com.valqueries.automapper.schema;

import com.valqueries.automapper.SqlDialect;
import io.ran.schema.SchemaBuilder;
import io.ran.schema.TableActionDelegate;

import javax.inject.Inject;
import java.util.stream.Collectors;

class ValqueriesSchemaBuilder extends SchemaBuilder<ValqueriesSchemaBuilder, ValqueriesTableBuilder, ValqueriesColumnBuilder, ValqueriesIndexBuilder, IValqueriesTableBuilder> {
	private SqlDialect dialect;

	@Inject
	public ValqueriesSchemaBuilder(ValqueriesSchemaExecutor executor) {
		super(executor);
		this.dialect = executor.getDialect();
	}

	@Override
	protected ValqueriesTableBuilder getTableBuilder() {
		return new ValqueriesTableBuilder(dialect);
	}

	@Override
	protected TableActionDelegate create() {
		return ta -> {
			return dialect.createTableStatement()+dialect.escapeColumnOrTable(dialect.column(ta.getName()))+" ("+ String.join(", ", ta.getActions()) +");";
		};
	}

	@Override
	protected TableActionDelegate modify() {
		return ta -> {
			return ta.getActions().stream().collect(Collectors.joining(";"));
		};
	}

	@Override
	protected TableActionDelegate remove() {
		return ta -> {
			return "DROP TABLE "+dialect.escapeColumnOrTable(dialect.column(ta.getName()))+";";
		};
	}
}
