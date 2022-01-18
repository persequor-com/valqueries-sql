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
import java.util.stream.Stream;

public class ValqueriesTableBuilder extends TableModifier<ValqueriesTableBuilder, ValqueriesColumnBuilder, ValqueriesIndexBuilder> implements IValqueriesTableBuilder {
	private SqlDialect dialect;
	private SqlNameFormatter sqlNameFormatter;

	public ValqueriesTableBuilder(SqlDialect dialect, SqlNameFormatter sqlNameFormatter) {
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
			return (t.getType() == TableActionType.MODIFY ? "ALTER TABLE "+t.getName()+" "+dialect.getAddColumnStatement() : "")+ca.getName()+" "+dialect.getSqlType(ca.getProperty());
		};
	}

	@Override
	protected ColumnActionDelegate modify() {
		return (t,ca) -> {
			return "ALTER TABLE "+t.getName()+" "+dialect.generateAlterColumnPartStatement(ca.getName())+" "+dialect.getSqlType(ca.getProperty());
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
			Stream<KeySet.Field> fieldStream = ia.getFields().stream().map(f -> new KeySet.Field(Property.get(f.getToken(), Clazz.of(Object.class)), incrementor.incrementAndGet()));
			KeySet keyset = new KeySet(fieldStream.collect(Collectors.toList()));

			keyset.setPrimary(ia.isPrimary());
			keyset.setName(ia.getName().unescaped());
			if (t.getType() == TableActionType.MODIFY || !keyset.isPrimary()) {
				return dialect.generateIndexStatement(t.getName(), keyset, ia.getProperty("isUnique").equals(true));
			} else {
				return dialect.generatePrimaryKeyStatement(t.getName(), keyset, ia.getProperty("isUnique").equals(true));
			}
		};
	}

	@Override
	protected IndexActionDelegate removeIndex() {
		return (t,ia) -> {
			return dialect.generateDropIndexStatement(t.getName(), ia.getName(), ia.isPrimary());
		};
	}
}
