package com.valqueries.automapper.schema;

import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import io.ran.schema.ColumnAction;
import io.ran.schema.IndexAction;
import io.ran.schema.SchemaBuilder;
import io.ran.schema.TableActionDelegate;
import io.ran.token.TableToken;
import io.ran.token.Token;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class ValqueriesSchemaBuilder extends SchemaBuilder<ValqueriesSchemaBuilder, ValqueriesTableBuilder, ValqueriesColumnBuilder, ValqueriesIndexBuilder, IValqueriesTableBuilder> {
	private SqlDialect dialect;
	private SqlNameFormatter sqlNameFormatter;

	@Inject
	public ValqueriesSchemaBuilder(ValqueriesSchemaExecutor executor, SqlNameFormatter sqlNameFormatter) {
		super(executor);
		this.dialect = executor.getDialect();
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	protected ValqueriesTableBuilder getTableBuilder() {
		return new ValqueriesTableBuilder(dialect, sqlNameFormatter);
	}

	@Override
	protected TableToken getTableToken(Token token) {
		return new ValqueriesTableToken(sqlNameFormatter, dialect, token);
	}

	@Override
	protected TableActionDelegate create() {
		return ta -> {
			String createTable = "CREATE TABLE "+ta.getName()+" ("+ ta.getColumns().stream().filter(ota -> {
				return (ota instanceof ColumnAction) || (ota instanceof IndexAction && ((IndexAction) ota).isPrimary());
			}).map(ota -> ota.apply(ta, ota)).collect(Collectors.joining(", ")) +");";
			String createIndexes = ta.getColumns().stream().filter(ota -> {
				return (ota instanceof IndexAction && !((IndexAction) ota).isPrimary());
			}).map(ota -> ota.apply(ta, ota)).collect(Collectors.joining(";"))+";";
			return createTable+createIndexes;
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
			return "DROP TABLE "+ta.getName()+";";
		};
	}
}
