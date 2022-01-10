package com.valqueries.automapper.schema;

import com.valqueries.automapper.SqlDialect;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.schema.ColumnAction;
import io.ran.schema.ColumnActionDelegate;
import io.ran.schema.IndexAction;
import io.ran.schema.IndexActionDelegate;
import io.ran.schema.TableActionType;
import io.ran.schema.TableModifier;
import io.ran.token.Token;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class ValqueriesTableBuilder extends TableModifier<ValqueriesTableBuilder, ValqueriesColumnBuilder, ValqueriesIndexBuilder> implements IValqueriesTableBuilder {
	private SqlDialect dialect;

	ValqueriesTableBuilder(SqlDialect dialect) {
		this.dialect = dialect;
	}

	@Override
	protected ValqueriesColumnBuilder getColumnBuilder(ColumnAction column) {
		return new ValqueriesColumnBuilder(column);
	}

	@Override
	protected ValqueriesIndexBuilder getIndexBuilder(IndexAction indexAction) {
		return new ValqueriesIndexBuilder(indexAction);
	}

	@Override
	protected ColumnActionDelegate create() {
		return (t,ca) -> {
			return (t.getType() == TableActionType.MODIFY ? "ALTER TABLE "+dialect.escapeColumnOrTable(dialect.column(t.getName()))+" "+dialect.addColumn() : "")+dialect.escapeColumnOrTable(dialect.column(ca.getName()))+" "+dialect.getSqlType(ca.getType(), Property.get(ca.getName(), Clazz.of(ca.getType())));
		};
	}

	@Override
	protected ColumnActionDelegate modify() {
		return (t,ca) -> {
			return "ALTER TABLE "+dialect.escapeColumnOrTable(dialect.column(t.getName()))+" "+dialect.alterColumn(ca.getName())+" "+dialect.getSqlType(ca.getType(), Property.get(ca.getName(), Clazz.of(ca.getType())));
		};
	}

	@Override
	protected ColumnActionDelegate remove() {
		return (t,ca) -> {
			return "ALTER TABLE "+dialect.escapeColumnOrTable(dialect.column(t.getName()))+" DROP COLUMN "+ca.getName().snake_case();
		};
	}

	@Override
	protected IndexActionDelegate createIndex() {
		return (t,ia) -> {

			AtomicInteger incrementor = new AtomicInteger();

			KeySet keyset = new KeySet(ia.getFields().stream().map(f -> new KeySet.Field(Property.get(f, Clazz.of(Object.class)),incrementor.incrementAndGet())).collect(Collectors.toList()));

			keyset.setPrimary(ia.isPrimary());
			keyset.setName(ia.getName());
			if (t.getType() == TableActionType.MODIFY) {
				return dialect.addIndex(dialect.getTableName(t.getName()), keyset, ia.getProperty("isUnique").equals(true));
			} else {
				return dialect.addIndexOnCreate(t.getName(), keyset, ia.getProperty("isUnique").equals(true));
			}
		};
	}

	@Override
	protected IndexActionDelegate removeIndex() {
		return (t,ia) -> {
			return dialect.dropIndex(t.getName(), ia.getName(), ia.isPrimary());
		};
	}
}
