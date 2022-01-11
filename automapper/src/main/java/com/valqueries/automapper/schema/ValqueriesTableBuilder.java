package com.valqueries.automapper.schema;

import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.SqlNameFormatter;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.Property;
import io.ran.schema.ColumnAction;
import io.ran.schema.ColumnActionDelegate;
import io.ran.schema.IndexAction;
import io.ran.schema.IndexActionDelegate;
import io.ran.schema.TableActionType;
import io.ran.schema.TableModifier;
import io.ran.token.ColumnToken;
import io.ran.token.IndexToken;
import io.ran.token.Token;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class ValqueriesTableBuilder extends TableModifier<ValqueriesTableBuilder, ValqueriesColumnBuilder, ValqueriesIndexBuilder> implements IValqueriesTableBuilder {
	private SqlDialect dialect;
	private SqlNameFormatter sqlNameFormatter;

	ValqueriesTableBuilder(SqlDialect dialect, SqlNameFormatter sqlNameFormatter) {
		this.dialect = dialect;
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	protected ValqueriesColumnBuilder getColumnBuilder(ColumnAction column) {
		return new ValqueriesColumnBuilder(column);
	}

	@Override
	protected ValqueriesIndexBuilder getIndexBuilder(IndexAction indexAction) {
		return new ValqueriesIndexBuilder(this, indexAction);
	}

	@Override
	protected ColumnToken getColumnToken(Token token) {
		return new ValqueriesColumnToken(sqlNameFormatter, dialect, token);
	}

	@Override
	protected IndexToken getIndexToken(Token token) {
		return new ValqueriesIndexToken(sqlNameFormatter, dialect, token);
	}

	@Override
	protected ColumnActionDelegate create() {
		return (t,ca) -> {
			return (t.getType() == TableActionType.MODIFY ? "ALTER TABLE "+t.getName()+" "+dialect.addColumn() : "")+ca.getName()+" "+dialect.getSqlType(ca.getProperty());
		};
	}

	@Override
	protected ColumnActionDelegate modify() {
		return (t,ca) -> {
			return "ALTER TABLE "+t.getName()+" "+dialect.alterColumn(ca.getName())+" "+dialect.getSqlType(ca.getProperty());
		};
	}

	@Override
	protected ColumnActionDelegate remove() {
		return (t,ca) -> {
			return "ALTER TABLE "+t.getName()+" DROP COLUMN "+ca.getName();
		};
	}

	@Override
	protected IndexActionDelegate createIndex() {
		return (t,ia) -> {

			AtomicInteger incrementor = new AtomicInteger();

			KeySet keyset = new KeySet(ia.getFields().stream().map(f -> new KeySet.Field(Property.get(f.getToken(), Clazz.of(Object.class)),incrementor.incrementAndGet())).collect(Collectors.toList()));

			keyset.setPrimary(ia.isPrimary());
			keyset.setName(ia.getName().unescaped());
			if (t.getType() == TableActionType.MODIFY) {
				return dialect.addIndex(t.getName(), keyset, ia.getProperty("isUnique").equals(true));
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
